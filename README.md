# P2P-File-Share
Group project for CNT4007 Computer Networking Fundamentals.

## Group Members
- Alexander Vargas - @Alex48Vargas
- Martin Kent - @martinkent2003
- Kyle Thompson - @Kyle-912

## Implimentation
### peerProcess.java
#### *peerProcess*

#### *readCommonConfig*

#### *readPeerInfoConfig*

#### *startServer*

#### *initializeFileData*

#### *connectToPeers*

#### *connectToPeer*

#### *updatePreferredPeers*

#### *updateOptimisticallyUnchokedPeer*

#### *handleMessage*

#### *decideInterestInPeer*

#### *getNeededPiecesFromPeer*

#### *addRequest*

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