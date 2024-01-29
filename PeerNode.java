import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerNode {
    private ServerSocket serverSocket;
    private List<Socket> clientSockets = new ArrayList<>();;
    private List<PrintWriter> outputs = new ArrayList<>();;
    private List<BufferedReader> inputs = new ArrayList<>();;
    private GlobalState globalState = new GlobalState();

    public void startServer(int port) throws Exception {

        serverSocket = new ServerSocket(port);
        System.out.println("Node started");
        System.out.println("Waiting for a peer ...");

        while(true) {
            Socket clientSocket = serverSocket.accept();
            clientSockets.add(clientSocket);

            System.out.println("A Peer has Connected");

            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            inputs.add(input);

            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(),true);
            outputs.add(output);

            output.println(clientSockets);

            Thread clientHandlingThread = new Thread(() -> {
                try {
                    handleClient(clientSocket, input, output);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            clientHandlingThread.start();
        }
    }

    public void handleClient(Socket clientSocket, BufferedReader input, PrintWriter output) throws Exception {
        String message = "";
        while(true) {
            message = input.readLine();
            switch (message) {
                case "stop":
                    stopServer();
                    return;
                case "ping":
                    System.out.println("Sending Transactions Count and State Root...");
                    output.println(pingResponse());
                    break;
                case "transaction":
                    //sendMessage(input, output, transactionResponse());
                    break;
                case "sync":
                    System.out.println("Sending Transactions...");
                    output.println(Utils.jsonArraySerializer(globalState.getTransactions()));
                    break;
                default:
                    if(!message.isEmpty()) {
                        System.out.println(message);
                    }
                    break;
            }
        }
    }

    public void stopServer() throws IOException {
        serverSocket.close();
    }

    public void startConnection(String ipAddress, int port) throws Exception {
        Socket clientSocket = new Socket(ipAddress, port);
        System.out.println("You are Connected!");

        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

        String listOfNodes = input.readLine();
        System.out.println("List of Nodes: " + listOfNodes);

        Map<String, String> pingResponse = Utils.jsonParser(sendMessage(input, output, "ping"));
        
        if(!pingResponse.isEmpty()){
            output.println("Transactions Count & State Root Received from Node " + clientSocket.getLocalPort());
            System.out.println("Ping Response: " + pingResponse);
        }

        Boolean isMatchingStateRoot = String.valueOf(this.globalState.calculateStateRoot()).equals(pingResponse.get("stateRoot"));
        Boolean isMatchingTxCount = Integer.parseInt(pingResponse.get("txCount")) == globalState.getTransactions().size();
        
        if(isMatchingStateRoot && isMatchingTxCount) System.out.println("Synced Node!");   
        else {
            System.out.println("Faulty Node!");
            System.out.println("Syncing...");
            String transactionsResponse = sendMessage(input, output, "sync");
            if(!transactionsResponse.isEmpty()){
                output.println("Transactions Received from Node " + clientSocket.getLocalPort());
                System.out.println("Sync Transactions Response: " + transactionsResponse);
                List<Map<String,String>> transactions = Utils.jsonArrayParser(transactionsResponse);
                System.out.println("Transaction count: " + transactions.size());
                System.out.println("Running Transactions Result (StateRoot): " + allTransactionsRunner(transactions));
            }
            //closeConnection(clientSocket, input, output);
            //output.println("stop");
        } 
        Thread.sleep(Integer.MAX_VALUE);
    }

    public void closeConnection(Socket clientSocket, BufferedReader input, PrintWriter output) throws IOException{
        clientSocket.close();
        input.close();
        output.close();
    }

    public String sendMessage(BufferedReader input, PrintWriter output, String message) throws IOException {
        output.println(message);
        String response = input.readLine();
        return response;
    }

    public String pingResponse(){
        int transactionCount = globalState.getTransactions().size();
        int stateRoot = globalState.calculateStateRoot();

        Map<String,String> mapResult = new HashMap<String,String>();

        mapResult.put("txCount", String.valueOf(transactionCount));
        mapResult.put("stateRoot", String.valueOf(stateRoot));

        return Utils.jsonSerializer(mapResult);
    }

/*     private String transactionResponse() throws Exception {
        String txBatch = sendMessage("Awaiting...");
        List<Map<String, String>> txObjects = Utils.jsonArrayParser(txBatch);
        return String.valueOf(allTransactionsRunner(txObjects));
    }

    public String transactionRequest() throws Exception {
        String responseAwating = sendMessage("Sending Transactions...");
        if("Awating...".equals(responseAwating)){
            String transactionsSerialized = Utils.jsonArraySerializer(globalState.getTransactions());
            String nodeStateRoot = sendMessage(transactionsSerialized);
            return (String.valueOf(this.globalState.calculateStateRoot()).equals(nodeStateRoot) ? "Valid Node" : "Faulty Node");
        }
        throw new Exception("Invalid Response");
    } */

    public Union<ExecuteResult, DeployResult> transactionRunner(Map<String,String> transaction) throws Exception {
        VirtualMachine vm = new VirtualMachine();
        String id = transaction.get("id");
        String messageType = transaction.get("messageType");
        String byteCode = transaction.get("bytecode");

        System.out.println(byteCode);
        System.out.println(messageType);

        byte[] byteArray = Utils.hexStringParser(byteCode);
        Map<String,String> databaseMap = globalState.getDatabase();

        switch (messageType) { 
            case "Execute":
                System.out.println("Executing");
                try {
                    return Union.fromLeft(ExecuteResult.fromResult(Integer.parseInt(id), vm.byteInterpreter(byteArray)));
                } catch (Exception e) {
                    return Union.fromLeft(ExecuteResult.fromError(Integer.parseInt(id), e.getMessage()));
                }

            case "Deploy":
                try {
                    System.out.println("Deploying");
                    databaseMap.put(String.valueOf(byteCode.hashCode()), byteCode);
                    return Union.fromRight(DeployResult.fromResult(Integer.parseInt(id), byteArray.hashCode()));
                } catch (Exception e) {
                    return Union.fromRight(DeployResult.fromError(Integer.parseInt(id), e.getMessage()));
                }

            default:
                throw new Exception("Invalid message type!");
        }
    }

    public int allTransactionsRunner(List<Map<String,String>> transactions) throws Exception{
        var receipts = new ArrayList<Union<ExecuteResult,DeployResult>>();
        List<Map<String,String>> listOfReceipts = globalState.getReceipts();

        for(Map<String,String> transaction : transactions) {
            receipts.add(transactionRunner(transaction));
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

        globalState.getReceipts().addAll(listOfReceipts);

        return globalState.calculateStateRoot();
    }

    public static void main(String[] args) throws Exception {
        PeerNode masterNode = new PeerNode();

        masterNode.startServer(42069);

    }
}