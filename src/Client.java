import java.net.*;
import java.io.*;

public class Client implements Runnable{
	peerProcess _peerProcess; //parentPeerProcess object will be used with synchronized methods to delegate tasks when messages sent and received
	Socket _requestSocket; // socket connect to the server
	ObjectOutputStream _out; // stream write to the socket
	ObjectInputStream _in; // stream read from the socket
	String message; // message send to the server
	String MESSAGE; // capitalized message read from the server
	String _hostName;
	int _portNum;

	public Client(String host, int portNum, peerProcess parent) {
		_peerProcess = parent;
		_hostName = host;
		_portNum = portNum;
		
	}

	public void run() {
		try {
			// create a socket to connect to the server
			_requestSocket = new Socket(_hostName, _portNum);
			// initialize inputStream and outputStream
			_out = new ObjectOutputStream(_requestSocket.getOutputStream());
			_out.flush();
			_in = new ObjectInputStream(_requestSocket.getInputStream());

			// get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print("Hello, please input a sentence: ");
				// read a sentence from the standard input
				message = bufferedReader.readLine();
				// Send the sentence to the server
				sendMessage(message);
				// Receive the upperCase sentence from the server
				MESSAGE = (String) _in.readObject();
				// show the message to the user
				System.out.println("Receive message: " + MESSAGE);
			}
		} catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found");
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
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
	void sendMessage(String msg) {
		try {
			// stream write the message
			_out.writeObject(msg);
			_out.flush();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
