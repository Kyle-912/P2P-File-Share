import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

// LOG MESSAGE TYPES:
// 0.  TCP Connection (to)
// 1.  TCP Connection (from)
// 2.  Neighbor change
// 3.  Optimistically unchoked peer change
// 4.  Unchoked
// 5.  Choked
// 6.  Received "Have"
// 7.  Received "Interested"
// 8.  Received "Not Interested"
// 9.  Downloaded piece
// 10. Completely downloaded

public class Log {
	private File _peerLog;
	private FileWriter _writer;
	private int _pid;
	private int _numPieces;

	public Log() {
	}

	public void createLog(int PeerID) throws IOException {
		_pid = PeerID;
		_peerLog = new File("log_peer_" + _pid + ".log");
		_peerLog.createNewFile();
		_writer = new FileWriter(_peerLog, false);
	}

	// Log Messages :

	// 0. TCP Connection (to)
	public void LogTCPTo(int peer) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " makes a connection to Peer " + peer + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 1. TCP Connection (from)
	public void LogTCPFrom(int peer) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " is connected from Peer " + peer + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 2. Neighbor change
	public void LogChangeNeighbors(ArrayList<Integer> peerList) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " has the preferred neighbors " + peerList.toString() + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 3. Optimistically unchoked peer change
	public void LogOptimisticChange(int peer) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " has the optimistically unchoked neighbor " + peer + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 4. Unchoked
	public void LogUnchoked(int peer) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " is unchoked by " + peer + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 5. Choked
	public void LogChoked(int peer) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " is choked by " + peer + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 6. Received "Have"
	public void LogReceivedHave(int peer, int pieceIndex) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " received the 'have' message from " + peer + " for the piece "
					+ pieceIndex + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 7. Received "Interested"
	public void LogReceivedInterested(int peer) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " received the 'interested' message from " + peer + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 8. Received "Not Interested"
	public void LogReceivedNotInterested(int peer) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " received the 'not interested' message from " + peer + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 9. Downloaded piece
	public void LogDownloadedPiece(int peer, int pieceIndex) throws IOException {
		LocalDateTime now = LocalDateTime.now();
		_numPieces += 1;
		try {
			_writer.write(now + ": Peer " + _pid + " has downloaded the piece " + pieceIndex + " from " + peer
					+ ". Now the number of pieces it has is " + _numPieces + ".\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}

	// 10. Completely downloaded
	public void LogDownloadComplete() throws IOException {
		LocalDateTime now = LocalDateTime.now();
		try {
			_writer.write(now + ": Peer " + _pid + " has downloaded the complete file.\n");
			_writer.flush();
		} catch (IOException e) {
			System.out.print("Error writing to peer log");
		}
	}
}
