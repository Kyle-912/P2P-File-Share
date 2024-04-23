import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

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
    ConcurrentHashMap<Integer, ArrayList<Integer>> _recentRequests = new ConcurrentHashMap<>(); // Last requested piece by each peer
    Server _server;
    ConcurrentHashMap<Integer, Client> _clients = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Server.Handler> _servers = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, PeerInfo> _peers = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, Integer> _downloadRates = new ConcurrentHashMap<>(); // Tracks download rates for peers
    Log _log = new Log();
    ScheduledExecutorService _scheduler = Executors.newScheduledThreadPool(2);

    public static void main(String args[]) throws Exception {
        peerProcess peerProcess = new peerProcess(Integer.parseInt(args[0]));
        System.out.println("Peer " + peerProcess._peerId + " is running");
    }

    public peerProcess(int peerId) throws Exception {
        _peerId = peerId;
        _log.createLog(peerId);
        readCommonConfig();
        readPeerInfoConfig();
        startServer();
        connectToPeers();
        _scheduler.scheduleAtFixedRate(this::updatePreferredPeers, 0, _unchokingInterval, TimeUnit.SECONDS);
        _scheduler.scheduleAtFixedRate(this::updateOptimisticallyUnchokedPeer, 1, _optimisticUnchokingInterval,
                TimeUnit.SECONDS);
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

    public synchronized void updatePreferredPeers() {
        ArrayList<Integer> oldPreferredPeerIds = new ArrayList<>(_preferredPeerIds);
        if (!_peers.get(_peerId)._hasFile) {
            _preferredPeerIds = new ArrayList<Integer>(_downloadRates.entrySet().stream()
                    .filter(entry -> _interestedPeerIds.contains(entry.getKey())).sorted((e1, e2) -> {
                        int valueCompare = e2.getValue().compareTo(e1.getValue());
                        if (valueCompare == 0) {
                            Random random = new Random();
                            return random.nextBoolean() ? 1 : -1;
                        }
                        return valueCompare;
                    }).limit(_numPreferredNeighbors).map(Map.Entry::getKey).collect(Collectors.toList()));
        } else {
            _preferredPeerIds = new ArrayList<Integer>(_downloadRates.entrySet().stream()
                    .filter(entry -> _interestedPeerIds.contains(entry.getKey())).sorted((e1, e2) -> {
                        Random random = new Random();
                        return random.nextBoolean() ? 1 : -1;
                    }).limit(_numPreferredNeighbors).map(Map.Entry::getKey).collect(Collectors.toList()));
        }

        ArrayList<Integer> toChoke = new ArrayList<>(oldPreferredPeerIds);
        toChoke.removeAll(_preferredPeerIds);
        toChoke.remove((Integer) _optimisticallyUnchokedPeerId);

        ArrayList<Integer> toUnchoke = new ArrayList<>(_preferredPeerIds);
        toUnchoke.removeAll(oldPreferredPeerIds);
        toUnchoke.remove((Integer) _optimisticallyUnchokedPeerId);

        _servers.forEach((key, value) -> {
            if (toUnchoke.contains(key)) {
                value.unchoke();
                System.out.println("Unchoked " + key + " due to updatePreferredPeers");
            } else if (toChoke.contains(key)) {
                value.choke();
                System.out.println("Choked " + key + " due to updatePreferredPeers");
            }
        });

        for (Integer otherPeerID : _downloadRates.keySet()) {
            _downloadRates.replace(otherPeerID, 0);
        }

        try {
            _log.LogChangeNeighbors(_preferredPeerIds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void updateOptimisticallyUnchokedPeer() {
        ArrayList<Integer> optimisticallyUnchokedCandidates = new ArrayList<>(_interestedPeerIds);
        optimisticallyUnchokedCandidates.removeAll(_preferredPeerIds);
        optimisticallyUnchokedCandidates.remove(_optimisticallyUnchokedPeerId);

        if (!optimisticallyUnchokedCandidates.isEmpty()) {
            Random random = new Random();
            Integer newOptimisticallyUnchokedPeerId = optimisticallyUnchokedCandidates
                    .get(random.nextInt(optimisticallyUnchokedCandidates.size()));

            _servers.forEach((key, value) -> {
                if (key.equals(_optimisticallyUnchokedPeerId)
                        && !_preferredPeerIds.contains(_optimisticallyUnchokedPeerId)) {
                    value.choke();
                    System.out.println("Choked " + key + " due to updateOptimisticallyUnchokedPeer");
                } else if (key.equals(newOptimisticallyUnchokedPeerId)) {
                    value.unchoke();
                    System.out.println("Unchoked " + key + " due to updateOptimisticallyUnchokedPeer");
                }
            });

            _optimisticallyUnchokedPeerId = newOptimisticallyUnchokedPeerId;

            try {
                _log.LogOptimisticChange(newOptimisticallyUnchokedPeerId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Optimistically unchoked candidate list is empty.");
        }
    }

    public synchronized Message handleMessage(Integer otherPeerId, Message message) throws IOException {
        Message responseMessage = null;
        switch (message.getTypeName()) {
            case BITFIELD:
                // Add peer bitfield
                _peers.get(otherPeerId)._bitfield = message._mdata;
                // Respond if interested
                if (decideInterestInPeer(otherPeerId)) {
                    System.out.println("PEER " + _peerId + " interested in peer " + otherPeerId);
                    responseMessage = new Message(Message.TYPES.INTERESTED, null);
                } else {
                    responseMessage = new Message(Message.TYPES.NOT_INTERESTED, null);
                }
                break;

                case CHOKE:
                if (_recentRequests.get(otherPeerId) != null) {
                    _requests.removeAll(_recentRequests.get(otherPeerId));
                }
                _recentRequests.put(otherPeerId, new ArrayList<Integer>());

                try {
                    _log.LogChoked(otherPeerId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case UNCHOKE:
                try {
                    _log.LogUnchoked(otherPeerId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                //see if interested in unchoked peer
                if(decideInterestInPeer(_peerId)){
                    //get random number of interesting pieces
                    int pieceNum = getNotRequestedRandomPieceNeededfromPeer(otherPeerId);
                    //add to total requests
                    addRequest(pieceNum);

                    //modify peer specific requests
                    ArrayList<Integer> newPeerRequestList = new ArrayList<>();
                    if(_recentRequests.get(otherPeerId) != null){
                        newPeerRequestList = _recentRequests.get(otherPeerId);
                    }

                    newPeerRequestList.add(pieceNum);
                    _recentRequests.put(otherPeerId, newPeerRequestList);

                    //modify response message
                    responseMessage = new Message(Message.TYPES.REQUEST, ByteBuffer.allocate(4).putInt(pieceNum).array());
                }else{
                    System.out.println("Not interested in unchoked peer " + otherPeerId);
                }
                break;

                case INTERESTED:
                if (!_interestedPeerIds.contains(otherPeerId)) {
                    _interestedPeerIds.add(otherPeerId);
                }
                try {
                    _log.LogReceivedInterested(otherPeerId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case NOT_INTERESTED:
                if (_interestedPeerIds.contains(otherPeerId)) {
                    _interestedPeerIds.remove(otherPeerId);
                }
                try {
                    _log.LogReceivedNotInterested(otherPeerId);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;

            case HAVE:
                try {
                    _log.LogReceivedHave(_peerId, -1); // FIXME: doesn't log proper pieceIndex
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

    public boolean decideInterestInPeer(int otherPeerId) {
        int numPieces = getNeededPiecesFromPeer(otherPeerId).size();
        return (numPieces > 0);
    }

    public ArrayList<Integer> getNeededPiecesFromPeer(Integer otherPeerId) {
        ArrayList<Integer> neededPieceNums = new ArrayList<>();
        // Add index if local bit 0 and other bit 1 because they have it and we don't
        for (int i = 0; i < (_fileSize / _pieceSize); i++) {
            boolean localZero = (_peers.get(_peerId)._bitfield[i / 8] & (1 << (7 - (i % 8)))) == 0;
            boolean passedOne = (_peers.get(otherPeerId)._bitfield[i / 8] & (1 << (7 - (i % 8)))) != 0;
            if (localZero && passedOne) {
                neededPieceNums.add(i);
            }
        }
        return neededPieceNums;
    }

    //following two methods used in UNCHOKING and PIECE 
    public void addRequest(Integer pieceNum) {
        if (!_requests.contains(pieceNum)) {
            _requests.add(pieceNum);
        } else {
            System.out.println("Request already pending for piece " + pieceNum);
        }
    }

    public int getNotRequestedRandomPieceNeededfromPeer(Integer otherPeerId) {
        ArrayList<Integer> interestingPieceNums = getNeededPiecesFromPeer(otherPeerId);
        // do not include pieces that have already been requested
        interestingPieceNums.removeAll(_requests);

        if (interestingPieceNums.size() == 0) {
            System.out.println("Cannot find interesting piece from peer that has not already been requested.");
            return -1;
        }
        //else get random piece from interesting pieces
        Random random = new Random();
        int pieceNum = interestingPieceNums.get(random.nextInt(interestingPieceNums.size()));
        return pieceNum;
    }
}
