import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class peerProcess {
    // File locations
    static final String COMMON_FILENAME = "Common.cfg";
    static final String PEER_INFO_FILENAME = "PeerInfo.cfg";

    // Config variables
    int _numPreferredNeighbors;
    int _unchokingInterval;
    int _optimisticUnchokingInterval;
    String _fileName;
    int _fileSize;
    int _pieceSize;
    int _numPieces;

    // Member Variables
    int _peerId;
    Integer _optimisticallyUnchokedPeerId;
    byte[] _bitfield;
    ConcurrentHashMap<Integer, byte[]> _peerBitFields = new ConcurrentHashMap<>(); // Key: Peer ID, Value: Bitfield
    ArrayList<Integer> _preferredPeerIds = new ArrayList<>(); // List of preferred peer IDs
    ArrayList<Integer> _interestedPeerIds = new ArrayList<>(); // List of interested peer IDs
    ArrayList<Integer> _requests = new ArrayList<>(); // List of requested piece indices
    Log log = new Log(_peerId);

    Server _server;
    ConcurrentHashMap<Integer, Client> _clients = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, PeerInfo> _peers = new ConcurrentHashMap<>();

    public static void main(String args[]) throws Exception {
        System.out.println("Peer " + args[0] + " is starting");
        peerProcess peerProcess = new peerProcess(Integer.parseInt(args[0]));
        System.out.println("Peer " + peerProcess._peerId + " is running");
    }

    public peerProcess(int peerId) throws Exception {
        this._peerId = peerId;
        readCommonConfig();
        readPeerInfoConfig();
        _server = new Server(peerId, _peers.get(peerId)._listenerPort);
        Thread serverThread = new Thread(_server);
        serverThread.start();
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
                            _numPreferredNeighbors = Integer.parseInt(value);
                            break;

                        case "UnchokingInterval":
                            _unchokingInterval = Integer.parseInt(value);
                            break;

                        case "OptimisticUnchokingInterval":
                            _optimisticUnchokingInterval = Integer.parseInt(value);
                            break;

                        case "FileName":
                            _fileName = value;
                            break;

                        case "FileSize":
                            _fileSize = Integer.parseInt(value);
                            break;

                        case "PieceSize":
                            _pieceSize = Integer.parseInt(value);
                            break;

                        default:
                            // Handle unknown keys or invalid configurations
                            break;
                    }
                }
            }
            _numPieces = Math.ceilDiv(_fileSize, _pieceSize);
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
                    PeerInfo pInfo = new PeerInfo(parts[0], parts[1], parts[2], parts[3]);
                    _peers.put(pInfo._pid, pInfo);
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
        for (int i = 1001; i < _peerId; i++) {
            System.out.println("Connecting to peer " + i);
            Client client = new Client(_peers.get(i)._hostname, _peers.get(i)._listenerPort);
            _clients.put(i, client);
            Thread clientThread = new Thread(client);
            clientThread.start();
            try {
                log.LogTCPTo(_peers.get(i)._pid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
