import java.net.*;
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
	private static class Handler extends Thread {
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
		}

		public void run() {
			try {
				// Initialize input and output streams
				_out = new ObjectOutputStream(_connection.getOutputStream());
				_out.flush();
				_in = new ObjectInputStream(_connection.getInputStream());
				try {
					_messageIn = (byte[]) _in.readObject();
					_clientId = Message.readHandshakeMsg(_messageIn);

					// Log connection
					System.out.println("LOG: Peer " + _peerProcess._peerId + " is connected from Peer " + _clientId);
					try {
						_peerProcess.log.LogTCPTo(_clientId);
					} catch (IOException e) {
						e.printStackTrace();
					}

					if (_clientId > _peerProcess._peerId) {
						_peerProcess.connectToPeer(_clientId);
					}

					while (true) {
						// Loop
					}

				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				} catch (Exception e) {
					System.err.println(e);
				}
			} catch (IOException ioException) {
				System.out.println("Disconnect with Client " + _clientId);
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
		public void sendMessage(String msg) {
			try {
				_out.writeObject(msg);
				_out.flush();
				System.out.println("Send message: " + msg + " to Client " + _clientId);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}
