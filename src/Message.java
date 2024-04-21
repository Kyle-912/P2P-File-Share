import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    public static int readHandshakeMsg(byte[] msg) throws Exception {
        String header = new String(msg, StandardCharsets.UTF_8).substring(0, 18);
        if (!header.equals("P2PFILESHARINGPROJ")) {
            throw new Exception("Expected Handshake Message but received other message type");
        }

        int clientId = ByteBuffer.wrap(msg, msg.length - 4, 4).getInt();
        return clientId;
    }
}
