import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class PeerNode {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter output;
    private BufferedReader input;

    public void startServer(int port) throws IOException {

        serverSocket = new ServerSocket(port);
        System.out.println("Node started");
        System.out.println("Waiting for a peer ...");

        clientSocket = serverSocket.accept();
        System.out.println("A Peer has Connected");

        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        output = new PrintWriter(clientSocket.getOutputStream(),true);

        String message = "";

        while(!"stop".equals(message)) {
            message = input.readLine();
            System.out.println(message);
            output.println(message);
        }
        stopServer();
    }

    public void stopServer() throws IOException {
        clientSocket.close();
        serverSocket.close();
        input.close();
        output.close();
    }

    public void startConnection(String ipAddress, int port) throws UnknownHostException, IOException {
        clientSocket = new Socket(ipAddress, port);
        System.out.println("You are Connected!");
        input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        output = new PrintWriter(clientSocket.getOutputStream(), true);
        try (Scanner sc = new Scanner(System.in)) {
            String message = "";
            while (!"stop".equals(message)) {
                message = sc.nextLine();
                sendMessage(message);
            }
        }
        closeConnection();
    }

    public static byte[] hexStringParser(String hexString) {
        String byteCode = hexString.substring(hexString.indexOf("x") + 1, hexString.length());
        byte[] byteArray = new byte[byteCode.length() / 2];

        for(int i = 0, j = 0; i < byteCode.length() / 2; byteArray[i++] = (byte) Integer.parseInt(byteCode.substring(j++, ++j), 16));

        return byteArray;
    }

    public void closeConnection() throws IOException {
        input.close();
        output.close();
        clientSocket.close();
    }

    public String sendMessage(String message) throws IOException {
        output.println(message);
        String response = input.readLine();
        return response;
    }

    public static void main(String[] args) throws IOException {
        
        String hex = "0x0001020304090801";

        byte[] byteArray = hexStringParser(hex);

        for (byte b : byteArray) {
            System.out.println(b);
        }
    }


}