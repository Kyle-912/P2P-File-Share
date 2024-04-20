import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class peerProcess {
    public static void main(String args[]) throws Exception {
        System.out.println("The peer is running.");
        peerProcess peerProcess = new peerProcess();
        peerProcess.start();
        ServerSocket listener = new ServerSocket(sPort);
        int clientNum = 1;
        // System.out.println(peerProcess.peers.get(0).ID);
        try {
            while (true) {
                new Handler(listener.accept(), clientNum).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } finally {
            listener.close();
        }
    }

    private static final int sPort = 8000; // The server will be listening on this port number

    static final String COMMON_FILENAME = "src/Common.cfg";
    static final String PEER_INFO_FILENAME = "src/PeerInfo.cfg";

    int numPreferredNeighbors;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    String fileName;
    int fileSize;
    int pieceSize;
    int numPieces;

    ArrayList<Peers> peers;

    public class Peers {
        int ID;
        String hostName;
        int portNum;
        boolean hasFile;
    }

    public peerProcess() {
        peers = new ArrayList<>();
    }

    private void start() {
        readCommonConfig();
        numPieces = Math.ceilDiv(fileSize, pieceSize);
        readPeerInfoConfig();
    }

    private void readCommonConfig() {
        String filePath = System.getProperty("user.dir") + "/" + COMMON_FILENAME;

        System.out.println("Attempting to read file: " + filePath); // Debug information

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");

                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    switch (key) {
                        case "NumberOfPreferredNeighbors":
                            numPreferredNeighbors = Integer.parseInt(value);
                            break;

                        case "UnchokingInterval":
                            unchokingInterval = Integer.parseInt(value);
                            break;

                        case "OptimisticUnchokingInterval":
                            optimisticUnchokingInterval = Integer.parseInt(value);
                            break;

                        case "FileName":
                            fileName = value;
                            break;

                        case "FileSize":
                            fileSize = Integer.parseInt(value);
                            break;

                        case "PieceSize":
                            pieceSize = Integer.parseInt(value);
                            break;

                        default:
                            // Handle unknown keys or invalid configurations
                            break;
                    }
                }
            }
            System.out.println("Successful");
        } catch (IOException e) {
            // Handle file read error
            e.printStackTrace();
        }
    }

    private void readPeerInfoConfig() {
        String filePath = System.getProperty("user.dir") + "/" + PEER_INFO_FILENAME;
        System.out.println("Attempting to read file: " + filePath); // Debug information
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");

                if (parts.length == 4) {
                    Peers peer = new Peers();
                    peer.ID = Integer.parseInt(parts[0]);
                    peer.hostName = parts[1];
                    peer.portNum = Integer.parseInt(parts[2]);
                    peer.hasFile = Boolean.parseBoolean(parts[3]);
                    peers.add(peer);
                }
            }
            System.out.println("Successful");
        } catch (IOException e) {
            // Handle file read error
            e.printStackTrace();
        }
    }

    /**
     * A handler thread class. Handlers are spawned from the listening
     * loop and are responsible for dealing with a single client's requests.
     */
    static class Handler extends Thread {
        private String message; // message received from the client
        private String MESSAGE; // uppercase message send to the client
        private Socket connection;
        private ObjectInputStream in; // stream read from the socket
        private ObjectOutputStream out; // stream write to the socket
        private int no; // The index number of the client

        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
        }

        public void run() {
            try {
                // initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try {
                    while (true) {
                        // receive the message sent from the client
                        message = (String) in.readObject();
                        // show the message to the user
                        System.out.println("Receive message: " + message + " from client " + no);
                        // Capitalize all letters in the message
                        MESSAGE = message.toUpperCase();
                        // send MESSAGE back to the client
                        sendMessage(MESSAGE);
                    }
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client " + no);
            } finally {
                // Close connections
                try {
                    in.close();
                    out.close();
                    connection.close();
                } catch (IOException ioException) {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        // send a message to the output stream
        public void sendMessage(String msg) {
            try {
                out.writeObject(msg);
                out.flush();
                System.out.println("Send message: " + msg + " to Client " + no);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
