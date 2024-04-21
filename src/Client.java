import java.net.*;
import java.io.*;

public class Client implements Runnable{
	peerProcess _peerProcess; //parentPeerProcess object will be used with synchronized methods to delegate tasks when messages sent and received
	Socket _requestSocket; // socket connect to the server
	ObjectOutputStream _out; // stream write to the socket
	ObjectInputStream _in; // stream read from the socket
	byte[] _messageOut; // message send to the server
	byte[] _messageIn; // capitalized message read from the server
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
			// create a socket to connect to the server
			_requestSocket = new Socket(_hostName, _portNum);
			// initialize inputStream and outputStream
			_out = new ObjectOutputStream(_requestSocket.getOutputStream());
			_out.flush();
			_in = new ObjectInputStream(_requestSocket.getInputStream());

			// Send Handshake Message
			sendMessage(Message.getHandshakeMsg(_peerProcess._peerId));
			System.out.println("LOG: Peer " + _peerProcess._peerId + " makes a connection to " + _serverId);

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

	// send a message to the output stream
	void sendMessage(byte[] msg) {
		try {
			// stream write the message
			synchronized (_out) {
				_out.writeObject(msg);
				_out.flush();
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
