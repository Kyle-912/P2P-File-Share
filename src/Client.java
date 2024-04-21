import java.net.*;
import java.io.*;

public class Client implements Runnable {
	peerProcess _peerProcess; // ParentPeerProcess object will be used with synchronized methods to delegate tasks when messages sent and received
	Socket _requestSocket; // Socket connected to the server
	ObjectOutputStream _out; // Stream written to the socket
	ObjectInputStream _in; // Stream read from the socket
	byte[] _messageOut; // Message sent to the server
	byte[] _messageIn; // Message read from the server
	String _hostName;
	int _portNum;
	int _serverId;

	public Client(String host, int portNum, int serverId, peerProcess parent) {
		_peerProcess = parent;
		_hostName = host;
		_portNum = portNum;
		_serverId = serverId;
	}

	public void run() {
		try {
			// Create a socket to connect to the server
			_requestSocket = new Socket(_hostName, _portNum);
			// Initialize input and output streams
			_out = new ObjectOutputStream(_requestSocket.getOutputStream());
			_out.flush();
			_in = new ObjectInputStream(_requestSocket.getInputStream());

			// Send handshake message
			sendMessage(Message.getHandshakeMsg(_peerProcess._peerId));

			// Log connection
			System.out.println("LOG: Peer " + _peerProcess._peerId + " makes a connection to " + _serverId);
			try {
				_peerProcess.log.LogTCPTo(_serverId);
			} catch (IOException e) {
				e.printStackTrace();
			}

			while (true) {
				// Loop
			}

		} catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			// Close connections
			try {
				_in.close();
				_out.close();
				_requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	// Send a message to the output stream
	void sendMessage(byte[] msg) {
		try {
			// Stream write the message
			synchronized (_out) {
				_out.writeObject(msg);
				_out.flush();
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
