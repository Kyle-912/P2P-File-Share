public class PeerInfo {
    public int _pid, _listenerPort;
    public String _hostname;
    public boolean _hasFile; //TODO: update whenever bitfield is updated
    public byte[] _bitfield;

    PeerInfo(String pid, String hostname, String listenerPort, String hasFile, int numPieces) {
        _pid = Integer.parseInt(pid);
        _listenerPort = Integer.parseInt(listenerPort);
        _hostname = hostname;

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
    }
}