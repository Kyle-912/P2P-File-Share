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
        _peerId = peerId;
        log.createLog(peerId);
        readCommonConfig();
        readPeerInfoConfig();
        startServer();
        connectToPeers();
        // TODO: CREATE SCHEDULER AND AT GIVEN INTERVALS RECOMPUTE PREFERRED PEERS AND OPTIMISTICALLY UNCHOKED PEER
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
                    PeerInfo pInfo = new PeerInfo(parts[0], parts[1], parts[2], parts[3], _numPieces);
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

    // TODO
    public synchronized Message handleMessage(Integer peerId, Message message) throws IOException {
        // Handle message
        Message responseMessage = null;

        switch (message.getTypeName()) {
            case BITFIELD:
                //add peer bitfield
                _peers.get(peerId)._bitfield = message._mdata;
                for (byte b : _peers.get(peerId)._bitfield) {
                    System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
                }
                //respond if interested 
                if(decideInterestInPeer(peerId)){
                    System.out.println("PEER "+_peerId +" interested in peer " + peerId);
                    responseMessage = new Message(Message.TYPES.INTERESTED, null);
                } else {
                    responseMessage = new Message(Message.TYPES.NOT_INTERESTED, null);
                }
                break;
            case CHOKE:
                //remove all requests from connected peer
                //implement removePendingRequests()
                try {
                    log.LogChoked(_peerId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case UNCHOKE:
                try {
                    log.LogUnchoked(_peerId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case INTERESTED:
                try {
                    log.LogReceivedInterested(_peerId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case NOT_INTERESTED:
                try {
                    log.LogReceivedNotInterested(_peerId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case HAVE:
                try {
                    log.LogReceivedHave(_peerId, -1); // FIXME: doesn't log proper pieceIndex
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case REQUEST:

                break;

            case PIECE:

                break;

            default:
                break;
        }
        return responseMessage;
    }

    // TODO
    public synchronized void updatePreferredPeers() {
        System.out.println("Updating preferred peers");
    }

    // TODO
    public synchronized void updateOptimisticallyUnchokedPeer() {
        System.out.println("Updating optimistically unchoked peer");
    }

    //TODO
    public boolean decideInterestInPeer(int peerId) {
        int numPieces = getNeededPiecesFromPeer(peerId).size();
        System.out.println("Peer "+ _peerId +" is deciding interest in peer " + peerId +": number of pieces needed: " + numPieces);
        return (numPieces>0);
    }

    public ArrayList<Integer> getNeededPiecesFromPeer(Integer peerId) {
        ArrayList<Integer> neededPieceNums = new ArrayList<>();

        // add index if local bit 0 and other bit 1 because they have it and we dont 
        for (int i = 0; i < (_fileSize / _pieceSize); i++) {
            boolean localZero = (_peers.get(_peerId)._bitfield[i / 8] & (1 << (7 - (i % 8)))) == 0;
            boolean passedOne = (_peers.get(peerId)._bitfield[i / 8] & (1 << (7 - (i % 8)))) != 0;
            if (localZero && passedOne) {
                neededPieceNums.add(i);
            } 
        }
        return neededPieceNums;
    }
}
