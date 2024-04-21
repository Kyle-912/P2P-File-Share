import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class Message {
    public int _mlength;
    public byte _mtype;
    public byte[] _mdata;

    //messagetypes
    public enum TYPES {
        CHOKE,
        UNCHOKE,
        INTERESTED,
        NOT_INTERESTED,
        HAVE,
        BITFIELD,
        REQUEST,
        PIECE
    }
    
    //create message constructor (requires type to be passed)
    public Message(TYPES msgType, byte[] mdata){
        if(!mdata.equals(null)){
            _mlength = mdata.length + 1;
        }else {
            _mlength = 1;
        }

        if(msgType == TYPES.CHOKE){
            _mtype = 0;
        }
        else if(msgType == TYPES.UNCHOKE){
            _mtype = 1;
        }
        else if(msgType == TYPES.INTERESTED){
            _mtype = 2;
        }
        else if(msgType == TYPES.NOT_INTERESTED){
            _mtype = 3;
        }
        else if(msgType == TYPES.HAVE){
            _mtype = 4;
        }
        else if(msgType == TYPES.BITFIELD){
            _mtype = 5;
        }
        else if(msgType == TYPES.REQUEST){
            _mtype = 6;
        }
        else if(msgType == TYPES.PIECE){
            _mtype = 7;
        }
        else{
            System.out.println("Invalid message type");
        }
        _mdata = mdata;
    }

    //create message from byte array constructor(used when receiving messages)

    public Message(byte[] msg){
        try{
            ByteBuffer wrappedMessage = ByteBuffer.wrap(msg);
            _mlength = wrappedMessage.getInt();
            _mtype = wrappedMessage.get();
            if(_mlength > 1){
                _mdata = new byte[_mlength - 1];
                wrappedMessage.get(_mdata, 0, _mlength - 1);
            }
        }
        catch(Exception e){
            System.out.println("Error creating message from byte array");
        }
    }

    public byte[] getMessageBytes(){
        ByteBuffer bytes = ByteBuffer.allocate(_mlength + 4);
        bytes.putInt(_mlength);
        bytes.put(_mtype);
        if(_mlength > 1){
            bytes.put(_mdata);
        }
        return bytes.array();
    }
    
    //getting the type as a constant value since enum is local to the class
    public TYPES getTypeName() {
		return TYPES.values()[_mtype];
	}

    public static byte[] getHandshakeMsg(int peerId) {
        byte[] header = "P2PFILESHARINGPROJ".getBytes();
        byte[] zeroBits = new byte[10];

        ByteBuffer message = ByteBuffer.allocate(32);
        message.put(header);
        message.put(zeroBits);
        message.putInt(peerId);
        return message.array();
    }

    public static int readHandshakeMsg(byte[] msg) throws Exception {
        String header = new String(msg, StandardCharsets.UTF_8).substring(0, 18);
        if (!header.equals("P2PFILESHARINGPROJ")) {
            throw new Exception("Expected Handshake Message but received other message type");
        }

        int clientId = ByteBuffer.wrap(msg, msg.length - 4, 4).getInt();
        return clientId;
    }
}
