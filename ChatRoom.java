import java.io.IOException;
import java.net.UnknownHostException;

public class ChatRoom {
    public static void main(String[] args) throws UnknownHostException, IOException {
        PeerNode client = new PeerNode();
        client.startConnection("127.0.0.1", 42069);
    }   
}
