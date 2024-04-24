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
Randomly chooses a peer to exchange the file with to give it a chance and to measure it's download rate
#### *handleMessage*
Builds a response message and takes action based on the message which was received.
#### *decideInterestInPeer*
Determines if a peer has pieces that the current peer needs.
#### *getNeededPiecesFromPeer*
Compares the current peers bitmap to another peers bitmap to find how many pieces are needed from them.
#### *addRequest*
Adds piece request to internal data structure.
#### *getNotRequestedRandomPieceNeededfromPeer*

### PeerInfo.java
#### *PeerInfo*

#### *updateBitfield*

#### *hasCompleteFile*


### Client.java
#### *Client*

#### *run*

#### *sendMessage*

#### *receiveMessage*

#### *sendNotIntMessage*


### Server.java
#### *Server*

#### *run*

#### *start*

#### *Handler*

#### *run*

#### *sendMessage*

#### *receiveMessage*

#### *sendHaveMessage*

#### *unchoke*

#### *choke*


### Log.java
#### *Log*

#### *createLog*

#### *LogTCPTo*

#### *LogTCPFrom*

#### *LogChangeNeighbors*

#### *LogOptimisticChange*

#### *LogUnchoked*

#### *LogChoked*

#### *LogReceivedHave*

#### *LogReceivedInterested*

#### *LogReceivedNotInterested*

#### *LogDownloadedPiece*

#### *LogDownloadComplete*


### Message.java
#### *getTypeName*

#### *Message*

#### *Message*

#### *getMessageBytes*

#### *getHandshakeMsg*

#### *readHandshakeMsg*


## How to Run
1. Compile all .java files in the working directory.
2. Put PeerInfo.cfg and Common.cfg into the working directory.
3. Prepare whatever folder(s) start with the file by putting the file in a folder/folders called peer_#### where #### is the peer ID of the peer(s) with the file.
4. Run "java peerProcess.java ####" on a server for each peer in PeerInfo.cfg in ascending order where #### is the peer ID of the peer being started.
5. The file will then be shared among all peers and appear in their folder.