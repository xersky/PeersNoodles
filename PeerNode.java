import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
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

    public String ping(String transactionsFilename, String stateFilename, String databaseFilename) throws FileNotFoundException {
        
        String transactionsParsed = Utils.readFromFile(transactionsFilename);
        String stateParsed  = Utils.readFromFile(stateFilename);
        String databaseParsed = Utils.readFromFile(databaseFilename);

        int transactionCount = Utils.jsonArrayParser(transactionsParsed).size();
        int hashOfState = stateParsed.hashCode();
        int hashOfDatabase = databaseParsed.hashCode();

        Map<String,String> mapResult = new HashMap<String,String>();

        mapResult.put("transactionCount", String.valueOf(transactionCount));
        mapResult.put("hashOfSatte", String.valueOf(hashOfState));
        mapResult.put("hashOfDatabase", String.valueOf(hashOfDatabase));

        return Utils.jsonSerializer(mapResult);
    }

    public static Union<ExecuteResult, DeployResult> transactionRunner(Map<String,String> transaction, String databaseFilename) throws Exception {
        VirtualMachine vm = new VirtualMachine();
        String id = transaction.get("id");
        String messageType = transaction.get("messageType");
        String byteCode = transaction.get("bytecode");

        System.out.println(byteCode);
        System.out.println(messageType);

        byte[] byteArray = Utils.hexStringParser(byteCode);
        String databaseString = new String(Utils.readFromFile(databaseFilename));
        Map<String,String> databaseMap = Utils.jsonParser(databaseString);

        switch (messageType) { 
            case "Execute":
                System.out.println("Executing");
                try {
                    return Union.fromLeft(ExecuteResult.fromResult(Integer.parseInt(id), vm.byteInterpreter(byteArray)));
                } catch (Exception e) {
                    return Union.fromLeft(ExecuteResult.fromError(Integer.parseInt(id), e.getMessage()));
                }

            case "Deploy":
                System.out.println("Deploying");
                try {
                    databaseMap.put(String.valueOf(byteCode.hashCode()), byteCode);
                    databaseString = Utils.jsonSerializer(databaseMap);
                    PrintWriter out = new PrintWriter(databaseFilename); 
                    out.println(databaseString);
                    out.close();
                    return Union.fromRight(DeployResult.fromResult(Integer.parseInt(id), byteArray.hashCode()));
                } catch (Exception e) {
                    return Union.fromRight(DeployResult.fromError(Integer.parseInt(id), e.getMessage()));
                }

            default:
                throw new Exception("No transaction found!");
        }
    }

    public static int allTransactionsRunner(String transactionsFilename, String databaseFilename, String receiptsFilename) throws Exception {
        String transactionJson = Utils.readFromFile(transactionsFilename);
        List<Map<String,String>> transactions = Utils.jsonArrayParser(transactionJson);
        var receipts = new ArrayList<Union<ExecuteResult,DeployResult>>();
        List<Map<String,String>> listOfReceipts = new ArrayList<Map<String,String>>();

        for(Map<String,String> transaction : transactions) {
            receipts.add(transactionRunner(transaction, databaseFilename));
        }

        for(Union<ExecuteResult,DeployResult> union : receipts) {
            Map<String,String> mapOfReceipt = new HashMap<String,String>();
            if(union.isLeft()) {
                mapOfReceipt.put("id", String.valueOf(union.getLeft().getId()));
                mapOfReceipt.put("transactionType", union.getLeft().getTransactionType());
                mapOfReceipt.put("isSuccess", union.getLeft().isSuccess().toString());
                if(union.getLeft().isSuccess()) mapOfReceipt.put("result", union.getLeft().getResult().toString());
                else mapOfReceipt.put("result", union.getLeft().getError());
            } else {
                mapOfReceipt.put("id", String.valueOf(union.getRight().getId()));
                mapOfReceipt.put("transactionType", union.getRight().getTransactionType());
                mapOfReceipt.put("isSuccess", union.getRight().isSuccess().toString());
                if(union.getRight().isSuccess()) mapOfReceipt.put("result", union.getRight().getResult().toString()); 
                else mapOfReceipt.put("result", union.getRight().getError());
            }
            listOfReceipts.add(mapOfReceipt);
        }

        String receiptsString = Utils.jsonArraySerializer(listOfReceipts);

        try {
            PrintWriter out = new PrintWriter(receiptsFilename); 
            out.println(receiptsString);
            out.close();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        System.out.println(receiptsString);
        
        return Utils.readFromFile(databaseFilename).hashCode();
    }

    public static void main(String[] args) throws Exception {

/*         byte[] bytecode = {00,00,00,00,04,0x1C,0x08};
        VirtualMachine vm = new VirtualMachine();

        System.out.println(vm.byteInterpreter(bytecode)); */
        
/*         String json = "{\"transactionType\":\"Deploy\",\"result\":\"189568618\",\"id\":\"0\",\"isSuccess\":\"true\"}";

        Map<String,String> keyValueMap = Utils.jsonParser(json);

        System.out.println(keyValueMap); */
        

        System.out.println(allTransactionsRunner("Transactions.json", "Database.json", "Receipts.json"));
    }
}