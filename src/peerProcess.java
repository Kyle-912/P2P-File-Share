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
    Server _server;
    ConcurrentHashMap<Integer, Client> _clients = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, PeerInfo> _peers = new ConcurrentHashMap<>();
    Log log = new Log();

    public static void main(String args[]) throws Exception {
        peerProcess peerProcess = new peerProcess(Integer.parseInt(args[0]));
        System.out.println("Peer " + peerProcess._peerId + " is running");
    }

    public peerProcess(int peerId) throws Exception {
        this._peerId = peerId;
        log.createLog(peerId);
        readCommonConfig();
        readPeerInfoConfig();
        startServer();
        connectToPeers();
    }

    private void readCommonConfig() {
        String filePath = System.getProperty("user.dir") + "/" + COMMON_FILENAME;
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
                            break;
                    }
                }
            }
            _numPieces = Math.ceilDiv(_fileSize, _pieceSize);
        } catch (IOException e) {
            System.out.println("Failure to read file: " + filePath);
            e.printStackTrace();
        }
    }

    private void readPeerInfoConfig() {
        String filePath = System.getProperty("user.dir") + "/" + PEER_INFO_FILENAME;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 4) {
                    PeerInfo pInfo = new PeerInfo(parts[0], parts[1], parts[2], parts[3]);
                    _peers.put(pInfo._pid, pInfo);
                }
            }
        } catch (IOException e) {
            System.out.println("Failure to read file: " + filePath);
            e.printStackTrace();
        }
    }

    private void startServer() throws Exception {
        _server = new Server(_peers.get(_peerId)._listenerPort, this);
        Thread serverThread = new Thread(_server);
        serverThread.start();
    }

    private void connectToPeers() {
        for (int i = 1001; i < _peerId; i++) {
            connectToPeer(i);
        }
    }

    public void connectToPeer(int i) {
        Client client = new Client(_peers.get(i)._hostname, _peers.get(i)._listenerPort, i, this);
        _clients.put(i, client);
        Thread clientThread = new Thread(client);
        clientThread.start();
    }
}
