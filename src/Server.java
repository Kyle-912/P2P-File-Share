import java.net.*;
import java.io.*;

public class Server implements Runnable{

	int _peerId;
	int _sPort;

	public Server(int peerId, int sPort) throws Exception {
		_peerId = peerId;
		_sPort = sPort;
	}

	public void run() {
		try {
			start();
		} catch (Exception e){
			System.out.println(e);
		}
	}

	public void start() throws IOException{
		System.out.println("Peer " + _peerId + "'s server listening on port " + _sPort);
		ServerSocket listener = new ServerSocket(_sPort);
		int peerNum = 1001;
		try {
			while (true) {
				new Handler(listener.accept(), peerNum).start();
				System.out.println("Client is connected!");
				peerNum++;
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
		private String message; // message received from the client
		private String MESSAGE; // uppercase message send to the client
		private Socket _connection;
		private ObjectInputStream _in; // stream read from the socket
		private ObjectOutputStream _out; // stream write to the socket
		private int _clientId; // The index number of the client

		public Handler(Socket connection, int clientPeerId) {
			_connection = connection;
			_clientId = clientPeerId;
		}

		public void run() {
			try {
				// initialize Input and Output streams
				_out = new ObjectOutputStream(_connection.getOutputStream());
				_out.flush();
				_in = new ObjectInputStream(_connection.getInputStream());
				try {
					while (true) {
						// receive the message sent from the client
						message = (String) _in.readObject();
						// show the message to the user
						System.out.println("Receive message: " + message + " from client " + _clientId);
						// Capitalize all letters in the message
						MESSAGE = message.toUpperCase();
						// send MESSAGE back to the client
						sendMessage(MESSAGE);
					}
				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
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

		// send a message to the output stream
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
