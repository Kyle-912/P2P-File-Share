import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;

public class Server implements Runnable {
	peerProcess _peerProcess; // ParentPeerProcess object will be used with synchronized methods to delegate tasks when messages sent and received
	int _peerId;
	int _sPort;

	public Server(int sPort, peerProcess parent) throws Exception {
		_sPort = sPort;
		_peerProcess = parent;
	}

	public void run() {
		try {
			start();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void start() throws IOException {
		ServerSocket listener = new ServerSocket(_sPort);
		try {
			while (true) {
				new Handler(listener.accept(), _peerProcess).start();
			}
		} finally {
			listener.close();
		}
	}

	/**
	 * A handler thread class. Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	public static class Handler extends Thread {
		peerProcess _peerProcess; // ParentPeerProcess object will be used with synchronized methods to delegate tasks when messages sent and received
		private byte[] _messageIn; // Message received from the client
		//private byte[] _messageOut; // Message sent to the client
		private Socket _connection;
		private ObjectInputStream _in; // Stream read from the socket
		private ObjectOutputStream _out; // Stream written to the socket
		private int _clientId; // The index number of the client

		public Handler(Socket connection, peerProcess parent) {
			_connection = connection;
			_peerProcess = parent;
			// Initialize input and output streams
			try {
				_out = new ObjectOutputStream(_connection.getOutputStream());
				_out.flush();
				_in = new ObjectInputStream(_connection.getInputStream());
			} catch (Exception e) {
				System.out.println("Error setting up input and output streams");
			}

			try {
				// Handshake from client
				_messageIn = (byte[]) _in.readObject();
				_clientId = Message.readHandshakeMsg(_messageIn);

				// Add to this peer's server map
				_peerProcess._servers.put(_clientId, this);

				// Log connection
				try {
					_peerProcess._log.LogTCPFrom(_clientId);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Start connection as client if not already established
				if (_clientId > _peerProcess._peerId) {
					_peerProcess.connectToPeer(_clientId);
				}

				// Respond to client with server peer's bitfield
				byte[] msg = new Message(Message.TYPES.BITFIELD,
						_peerProcess._peers.get(_peerProcess._peerId)._bitfield).getMessageBytes();
				sendMessage(msg);

			} catch (Exception e) {
				System.out.println("Error receiving handshake message");
			}
		}

		public void run() {
			try {
				while (true) {
					// Get message from socket
					Message currMsg = receiveMessage();

					// Pass to peer and store response message
					Message respMsg = null;
					try {
						respMsg = _peerProcess.handleMessage(_clientId, currMsg);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

					// Send response message if applicable
					if (respMsg != null) {
						sendMessage(respMsg.getMessageBytes());
					}
				}
			} catch (Exception e) {
				System.err.println(e);
			} finally {
				// Close connections
				try {
					_in.close();
					_out.close();
					_connection.close();
				} catch (IOException ioException) {
					System.out.println("Disconnect with Client " + _clientId);
				}
			}
		}

		// Send a message to the output stream
		public void sendMessage(byte[] msg) {
			try {
				synchronized (_out) {
					_out.writeObject(msg);
					_out.flush();
				}
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		private Message receiveMessage() {
			try {
				byte[] msg;
				synchronized (_in) {
					msg = (byte[]) _in.readObject();
				}
				return new Message(msg);
			} catch (Exception e) {
				System.out.println("Error receiving message in server thread.");
				return null;
			}
		}

		public void sendHaveMessage(int pieceNum) {
			sendMessage(
					new Message(Message.TYPES.HAVE, ByteBuffer.allocate(4).putInt(pieceNum).array()).getMessageBytes());
		}

		public void unchoke() {
			sendMessage(new Message(Message.TYPES.UNCHOKE, null).getMessageBytes());
		}

		public void choke() {
			sendMessage(new Message(Message.TYPES.CHOKE, null).getMessageBytes());
		}
	}
}
