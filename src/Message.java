import java.nio.ByteBuffer;

public class Message {

    public static byte[] getHandshakeMsg(int peerId) {

        byte[] header = "P2PFILESHARINGPROJ".getBytes();
        byte[] zeroBits = new byte[10];

        ByteBuffer message = ByteBuffer.allocate(32);
        message.put(header);
        message.put(zeroBits);
        message.putInt(peerId);

        return message.array();
    }
}
