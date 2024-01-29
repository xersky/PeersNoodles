public class ChatRoom {
    public static void main(String[] args) throws Exception {
        PeerNode client = new PeerNode();
/*         Thread threadServer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.startServer(3333);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });  
        threadServer.start();
        Thread threadClient = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.startConnection("127.0.0.1", 42069);
                    client.sendMessage("ping");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });  
        threadClient.start();*/

        client.startConnection("127.0.0.1", 42069);
    }    
}
