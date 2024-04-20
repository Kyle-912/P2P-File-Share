import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class peerProcess {
    // File locations
    static final String COMMON_FILENAME = "Common.cfg";
    static final String PEER_INFO_FILENAME = "PeerInfo.cfg";

    // Config variables
    int numPreferredNeighbors;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    String fileName;
    int fileSize;
    int pieceSize;
    int numPieces;

    // Member Variables
    int peerId;
    Server server;
    HashMap<Integer, Client> clients;
    ArrayList<String[]> peers;

    public static void main(String args[]) throws Exception {
        System.out.println("Peer " + args[0] + " is starting");
        peerProcess peerProcess = new peerProcess(Integer.parseInt(args[0]));
        System.out.println("Peer " + peerProcess.peerId + " is running");
    }

    public peerProcess(int peerId) throws Exception {
        this.peerId = peerId;
        peers = new ArrayList<>();
        server = new Server(peerId, 7000 + peerId);
        Thread t = new Thread(server);
        t.start();
        startProcess();
    }

    private void startProcess() {
        readCommonConfig();
        readPeerInfoConfig();
        connectToPeers();
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
            numPieces = Math.ceilDiv(fileSize, pieceSize);
            System.out.println("Success");
        } catch (IOException e) {
            // Handle file read error
            System.out.println("Failure");
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
                    peers.add(parts);
                }
            }
            System.out.println("Success");
        } catch (IOException e) {
            // Handle file read error
            System.out.println("Failure");
            e.printStackTrace();
        }
    }

    private void connectToPeers() {
        System.out.println("Connecting to peers");
        for (int i = 1001; i<peerId; i++) {
            System.out.println("Connecting to peer " + i);
            Client client = new Client("localhost", 7000 + i);
            Thread t = new Thread(client);
            t.start();
        }
    }
}
