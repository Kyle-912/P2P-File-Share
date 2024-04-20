public class PeerInfo {

    public int _pid, _listenerPort;
    public String _hostname;
    public boolean _hasFile;

    PeerInfo(String pid, String hostname, String listenerPort, String hasFile) {
        _pid = Integer.parseInt(pid);
        _listenerPort = Integer.parseInt(listenerPort);
        _hostname = hostname;
        _hasFile = Boolean.parseBoolean(hasFile);
    }

}