import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
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

    public static Result<ExecuteResult, DeployResult> transactionRunner(Map<String,String> transaction) throws Exception{
        VirtualMachine vm = new VirtualMachine();
        String messageType = transaction.get("messageType");

        String byteCode = transaction.get("bytecode");
        System.out.println(byteCode);
        System.out.println(messageType);
        byte[] byteArray = Utils.hexStringParser(byteCode);

        switch (messageType) { 
            case "Execute":
                try {
                    return Result.fromResult(ExecuteResult.fromResult(vm.byteInterpreter(byteArray)));
                } catch (Exception e) {
                    return Result.fromResult(ExecuteResult.fromError(e.getMessage()));
                }

            case "Deploy":
                return null;
        
            default:
                throw new Exception("No transaction found!");
        }
    }

    public static int transactionsRunner(String transactionsFilename) throws Exception {
        String transactionJson = Utils.readFromFile(transactionsFilename);
        List<Map<String,String>> transactions = Utils.jsonParser(transactionJson);

        for (Map<String,String> transaction : transactions) {
            transactionRunner(transaction);
        }

        return Utils.readFromFile("State.json").hashCode();
    }

    public static void main(String[] args) throws Exception {

/*         byte[] bytecode = {00,00,00,00,04,0x1C,0x08};
        VirtualMachine vm = new VirtualMachine();

        System.out.println(vm.byteInterpreter(bytecode)); */

        System.out.println(transactionsRunner("Transactions.json"));
    }
}