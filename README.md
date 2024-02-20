# PeersNoodles

A Java Peer-to-Peer Node Syncing and Consensus Application inspired by Ethereum. 

This project simulates a peer-to-peer chat network with a Master Node to broadcast connecting nodes, orchestrates consensus among connected nodes in order to sync the State and using the `GenesisVM` as a Submodule for interpreting and executing bytecode.

## Components

### Master Node (PeerNode class)
The master node serves as the central point for connecting peers and maintaining consensus. It listens on a randomly assigned port, accepts incoming connections, and handles various message types:

`ping`: Responds with the count of transactions and the state root.

`sync`: Sends transactions to synchronize the state with connected nodes.

**Custom Transactions:** Executes bytecode transactions using the `GenesisVM` submodule and maintains consensus on the global state.

The master node broadcasts the list of connected nodes to new peers and ensures synchronization by comparing state roots and transaction counts.

### Chat Room Client (ChatRoom class)
The chat room client simulates a peer connecting to the network. It connects to the master node, exchanges node information, and checks for synchronization. It retrieves missing transactions from other nodes to achieve consensus.

### Configuration Singleton (ConfigSingeleton class)
The ConfigSingeleton class provides a singleton instance for configuration settings. It defines filenames for `transactions`, `state`, `database`, and `receipts`.

### Global State Manager (GlobalState class)
The GlobalState class manages the global state of the application, including `transactions`, `receipts`, `state`, and `database` information. It initializes the state from JSON files and parsing its content and calculates the state root based on the stored data.

### [GenesisVM](https://github.com/xersky/GenesisVM) Submodule 

The GenesisVM is an implementation of a Virtual Machine using Java and acts in our current repo as a submodule to interpret and execute bytecode transactions.

### Transactions and Receipts
The `Transactions.json` file contains a sample of transactions with unique identifiers, bytecode, and transaction types ("Deploy" to deploy/store the transaction's bytecode in the `Database.json` file OR "Execute" to execute the transaction's bytecode and store its result in the `Receipts.json` file). 

The `Receipts.json` file records the results of transaction execution, including "transactionType", "result" and a "isSuccess" status.

## Execution

Let's start the master node by running the main method of the `PeerNode` Class. It generates the server port so that other nodes can connect to.

```java
public static void main(String[] args) throws Exception {
    PeerNode masterNode = new PeerNode();
    masterNode.startServer();
}
```

Output result of the Master Node:

```
Node started
Node serverPort: 8551
Waiting for a peer ...
```

### Case of Valid/Synced Nodes (Sharing the same State)

Then, we will try to connect to this broadcasting master node by running Nodes in the `ChatRoom` class.
We will be using threads for each node instance, because every node acts as client and a server at the same time (P2P Architecture).

```java
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
                    client.startConnection("127.0.0.1", 8551, client.serverPort);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });  
        threadClient.start();
    }    
}
```

Output result of the newly connected Node:

```
Node started
You are Connected!
My node info: {address=/127.0.0.1, port=7258}
Node serverPort: 7258
Waiting for a peer ...
Lists of Nodes to connect to: []
Ping Response: {txCount=2, stateRoot=281079501}
Synced Node!
```

Now, let's look at how the Master Node handled this new connection:

```
Node started
Node serverPort: 8551
Waiting for a peer ...
A Peer has Connected
List of connected Nodes: [{address=/127.0.0.1, port=7258}]
Sending Transactions Count and State Root...
Transactions Count & State Root Received from Node 7258
```

We will try to connect more nodes.

Master Node Output Result:

```
Node started
Node serverPort: 8551
Waiting for a peer ...
A Peer has Connected
List of connected Nodes: [{address=/127.0.0.1, port=7258}]
Sending Transactions Count and State Root...
Transactions Count & State Root Received from Node 7258
A Peer has Connected
List of connected Nodes: [{address=/127.0.0.1, port=7258}, {address=/127.0.0.1, port=4098}]
Sending Transactions Count and State Root...
Transactions Count & State Root Received from Node 4098
A Peer has Connected
List of connected Nodes: [{address=/127.0.0.1, port=7258}, {address=/127.0.0.1, port=4098}, {address=/127.0.0.1, port=2250}]
Sending Transactions Count and State Root...
Transactions Count & State Root Received from Node 2250
A Peer has Connected
List of connected Nodes: [{address=/127.0.0.1, port=7258}, {address=/127.0.0.1, port=4098}, {address=/127.0.0.1, port=2250}, {address=/127.0.0.1, port=3278}]
Sending Transactions Count and State Root...
Transactions Count & State Root Received from Node 3278
```

Result of the last node to connect to this P2P Network:

```
Node started
You are Connected!
My node info: {address=/127.0.0.1, port=3278}
Node serverPort: 3278
Waiting for a peer ...
Lists of Nodes to connect to: [{address=/127.0.0.1, port=7258}, {address=/127.0.0.1, port=4098}, {address=/127.0.0.1, port=2250}]
Ping Response: {txCount=2, stateRoot=281079501}
Synced Node!
You are Connected!
My node info: {address=/127.0.0.1, port=3278}
Lists of Nodes to connect to: [{address=/127.0.0.1, port=4098}, {address=/127.0.0.1, port=2250}]
Ping Response: {txCount=2, stateRoot=281079501}
Synced Node!
You are Connected!
My node info: {address=/127.0.0.1, port=3278}
Lists of Nodes to connect to: [{address=/127.0.0.1, port=2250}]
Ping Response: {txCount=2, stateRoot=281079501}
Synced Node!
You are Connected!
My node info: {address=/127.0.0.1, port=3278}
Lists of Nodes to connect to: []
Ping Response: {txCount=2, stateRoot=281079501}
Synced Node!
```

### Case of Faulty / Not Synced Nodes (Not sharing the same state)

We start again by running the orchestrator node:
```
Node started
Node serverPort: 2108
Waiting for a peer ...
```

Now we will to run nodes with none or different state and try to sync them.

Output result the newly connected node and syncing it.

```
Node started
You are Connected!
My node info: {address=/127.0.0.1, port=1833}
Node serverPort: 1833
Waiting for a peer ...
Lists of Nodes to connect to: []
Ping Response: {txCount=2, stateRoot=281079501}
Faulty Node!
Syncing...
Sync Transactions Response: [{"bytecode":"0x09","messageType":"Deploy","id":"0"},{"bytecode":"0x1B000000451B0000001700000000170308","messageType":"Execute","id":"1"}]
Transaction count: 2
0x09
Deploy
Deploying
0x1B000000451B0000001700000000170308
Execute
Executing
EXEC
PUSH
PUSH
MUL
STOP
EXEC
PUSH
PUSH
ADD
RETURN
PUSH
MUL
RETURN
Running Transactions Result (StateRoot): 281079500
```

The result of the Master node that is responsible of sharing the transactions and verifying the correctness of the state to achieve a consensus.

```
Node started
Node serverPort: 2108
Waiting for a peer ...
A Peer has Connected
List of connected Nodes: [{address=/127.0.0.1, port=1833}]
Sending Transactions Count and State Root...
Transactions Count & State Root Received from Node 1833
Sending Transactions...
Transactions Received from Node 53212
```

### Syncing Part

`PeerNode.java`
```java

    Map<String, String> pingResponse = Utils.jsonParser(sendMessage(input, output, "ping"));
        
    if(!pingResponse.isEmpty()){
        output.println("Transactions Count & State Root Received from Node " + serverPort);
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
    } 
```

### Running Transactions
**Function running one transaction**

`PeerNode.java`
```java
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
```

**Function running all transactions**

`PeerNode.java`
```java
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
```