import java.util.Random;

public class ChatRoom {
    public static void main(String[] args) throws Exception {
        PeerNode client = new PeerNode();
        Thread threadServer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.startServer();
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });  
        threadServer.start();

        Thread threadClient = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.startConnection("127.0.0.1", 395, client.serverPort);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });  
        threadClient.start();

        //client.startConnection("127.0.0.1", 42069);
    }    
}
