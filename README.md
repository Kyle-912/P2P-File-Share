# P2P-File-Share
Group project for CNT4007 Computer Networking Fundamentals.

## Group Members
- Alexander Vargas - @Alex48Vargas
- Martin Kent - @martinkent2003
- Kyle Thompson - @Kyle-912

## Implimentation
### peerProcess.java
#### *peerProcess*
Creates the log and scheduler and calls all functions necessary to start a peer.
#### *readCommonConfig*
Reads in the Common.cfg file and stores it's values to internal data structures.
#### *readPeerInfoConfig*
Reads in the PeerInfo.cfg file and stores it's values to internal data structures.
#### *startServer*
Starts up thread for the server.
#### *initializeFileData*
Creates file for the peer to write to once it gets all the pieces.
#### *connectToPeers*
Loops through all peers calling ConnectToPeer for them.
#### *connectToPeer*
Starts up a client thread for each peer.
#### *updatePreferredPeers*
Changes which peers the file is being exchanged with based on download rate.
#### *updateOptimisticallyUnchokedPeer*
Randomly chooses a peer to exchange the file with to give it a chance and to measure it's download rate.
#### *handleMessage*
Builds a response message and takes action based on the message which was received.
#### *decideInterestInPeer*
Determines if a peer has pieces that the current peer needs.
#### *getNeededPiecesFromPeer*
Compares the current peers bitmap to another peers bitmap to find how many pieces are needed from them.
#### *addRequest*
Adds piece request to internal data structure.
#### *getNotRequestedRandomPieceNeededfromPeer*
Picks a piece to request from a peer.

### PeerInfo.java
#### *PeerInfo*
Constructor for a PeerInfo object. Takes as parameters the peerId, hostname, listenerPort, and boolean hasFile as stores as member variables. It also initializes the peer's bitfield to 1's if it has the file or 0's if it does not have the file. A PeerInfo object is created for each peer in the torrent.
#### *updateBitfield*
Called after downloading a piece. Takes as a parameter the pieceIndex received, then uses bitwise operations to update the corresponding 0 to a 1 in the bitfield.
#### *hasCompleteFile*
Loops through the bitfield and checks that each bit in the range (0, numPieces) is equal to 1. If any 0's are found, return false. Otherwise, peer has the complete file, return true.

### Client.java
#### *Client*
In the constructor we pass the host string, the port number, the serverId (peerId's server which we are connecting to) and the peerProcess parent object we initialize all these into class variables, then establish a handshake connection and log it.
#### *run*
In our run for the Client, similar to the Server, we are running an endless loop that receives a message and then passes the message synchronously to the parent peerProcess object. Then depending on what the return value is sends a message back to the server.
#### *sendMessage*
Takes in a byte array and writes to the object output stream.
#### *receiveMessage*
Is called from the run() and takes in a byte array message through the object input stream, which is then initiliazed as a Message object from the byte array.
#### *sendNotIntMessage*
Sends a notInterested message to another Peer's Server. This is called from the PeerProcess class.

### Server.java
#### *Server*
In our Server constructor we pass in the port number, and the parent process as parameters and initialize them into class variables. This also acts as a listener for when our handler is not accepting messages.
#### *run*
Run calls start with exception checking.
#### *start*
Start creates a new server socket port and listens for incoming tcp requests, for which it creates a new handler when it does receive one.
#### *Handler*
A handler passed a socket connection and a peerProcess Parent object. It also creates the out and in object streams, establishes a handshake, and sends the bitfield of the peer to the client.
#### *run*
In our run for the Server, similar to the Client, we are running an endless loop that receives a message and then passes the message synchronously to the parent peerProcess object. Then depending on what the return value is sends a message back to the client.
#### *sendMessage*
Takes in a byte array and writes to the object output stream.
#### *receiveMessage*
Is called from the run() and takes in a byte array message through the object input stream, which is then initiliazed as a Message object from the byte array.
#### *sendHaveMessage*
Sends a have message to the client that we have a specific piece.
#### *unchoke*
Sends an unchoke to the connected client.
#### *choke*
Sends a choke to the connected client.

### Log.java
#### *Log*
Constructor that creates a log for the peer running peerProcess. Does nothing.
#### *createLog*
Called for setting member variables. Takes in the peerId as a parameter and saves it as a member variable. Also instantiates member variables of type File and FileWriter, which are used in the subsequent methods to write to the log file. All of the log methods are called by peerProcess when necessary.
#### *LogTCPTo*
Called when peer makes a TCP connection to another peer as a client. Writes log to log_peer_####.log.
#### *LogTCPFrom*
Called when peer's server receives a TCP connection request from another client. Writes log to log_peer_####.log.
#### *LogChangeNeighbors*
Called periodically when neighbors are updated. Timing is determined by the scheduler. Takes as parameter an arrayList of peerId's that are now preferred. Writes log to log_peer_####.log.
#### *LogOptimisticChange*
Called periodically when the optimistically unchoked neighbor is updates. Timing is determined by the scheduler. Takes the peerId of the optimistically unchoked neighbor. Writes log to log_peer_####.log.
#### *LogUnchoked*
Called when the peer receives a unchoked message from another peer. Takes the other peer's peerId as parameter. Writes log to log_peer_####.log.
#### *LogChoked*
Called when the peer receives a choked message from another peer. Takes the other peer's peerId as parameter. Writes log to log_peer_####.log.
#### *LogReceivedHave*
Called when the peer receives a have message from another peer. Takes as parameters the other peer's peerId and the piece it has. Writes log to log_peer_####.log.
#### *LogReceivedInterested*
Called when the peer receives an interested message from another peer. Takes as parameter the other peer's peerId. Writes log to log_peer_####.log.
#### *LogReceivedNotInterested*
Called when the peer receives a not interested message from another peer. Takes as parameter the other peer's peerId. Writes log to log_peer_####.log.
#### *LogDownloadedPiece*
Called when the peer downloads a piece. Takes the peer it received it from and the piece Index as parameters. Writes log to log_peer_####.log.
#### *LogDownloadComplete*
Called when the peerProcess determines the file was completely downloaded. Writes log to log_peer_####.log.

### Message.java
The Message class is used by the peerProcess, server, and client to abstract the sending and reading of messages. It stores the message's length, type, and data.
#### *Message*
First constructor, used when sending a message. Takes the message type and a byte[] of the data to be sent. Sets the member variables to reflect this.
#### *Message*
Second Constructor, used when receiving a message. Takes the byte[] of the raw message, and reads the length, type, and data into member variables.
#### *getTypeName*
Enum is used for typenames. This method is used for returning the type as an enum (ex. BITFIELD) rather than an integer.
#### *getMessageBytes*
Using a ByteBuffer, adds the length, type, and data to a single message. Returns it as a byte[].
#### *getHandshakeMsg*
Static method that takes in a peerId and returns the handshake message that corresponds to that peerId as a byte[].
#### *readHandshakeMsg*
Static method that takes in a raw handshake message and returns the peerId. Also verifies that it did in fact receive a handshake method.

## How to Run
1. Compile all .java files in the working directory.
2. Put PeerInfo.cfg and Common.cfg into the working directory.
3. Prepare whatever folder(s) start with the file by putting the file in a folder/folders called peer_#### where #### is the peer ID of the peer(s) with the file.
4. Run "java peerProcess.java ####" on a server for each peer in PeerInfo.cfg in ascending order where #### is the peer ID of the peer being started.
5. The file will then be shared among all peers and appear in their folder.
