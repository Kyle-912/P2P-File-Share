public class PeerInfo {
    public int _pid, _listenerPort, _numPieces;
    public String _hostname;
    public boolean _hasFile;
    public byte[] _bitfield;

    PeerInfo(String pid, String hostname, String listenerPort, String hasFile, int numPieces) {
        _pid = Integer.parseInt(pid);
        _listenerPort = Integer.parseInt(listenerPort);
        _hostname = hostname;
        _numPieces = numPieces;

        if (hasFile.equals("1")) {
            _hasFile = true;
        } else {
            _hasFile = false;
        }

        _bitfield = new byte[(numPieces + 7) / 8];
        if (_hasFile) {
            for (int i = 0; i < numPieces; i++) {
                _bitfield[i / 8] |= (1 << (7 - (i % 8)));
            }
        }

        hasCompleteFile();
    }

    public void updateBitfield(int pieceIndex) {
        _bitfield[pieceIndex / 8] = (byte) (_bitfield[pieceIndex / 8] | (1 << (7 - (pieceIndex % 8))));
    }

    public boolean hasCompleteFile() {
        for (int i = 0; i < _numPieces; i++) {
            if ((_bitfield[i / 8] & (1 << (7 - (i % 8)))) == 0) {
                return false;
            }
        }
        _hasFile = true;
        return true;
    }
}