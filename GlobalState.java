import java.util.List;
import java.util.Map;

public class GlobalState {
    private List<Map<String,String>> transactions;
    private List<Map<String,String>> receipts;
    private Map<String,String> state;
    private Map<String,String> database;

    public GlobalState(){
        initState();
    }

    public List<Map<String, String>> getTransactions() {
        return transactions;
    }

    public List<Map<String, String>> getReceipts() {
        return receipts;
    }
    public Map<String, String> getState() {
        return state;
    }

    public Map<String, String> getDatabase() {
        return database;
    }

    public void initState() {
        ConfigSingeleton config = ConfigSingeleton.getInstance();

        String transactionsString = Utils.readFromFile(config.getTransactionsFilename());
        String receiptsString = Utils.readFromFile(config.getReceiptsFilename());
        String stateString = Utils.readFromFile(config.getStateFilename());
        String databaseString = Utils.readFromFile(config.getDatabaseFilename());

        this.transactions = Utils.jsonArrayParser(transactionsString);
        this.receipts = Utils.jsonArrayParser(receiptsString);
        this.state = Utils.jsonParser(stateString);
        this.database = Utils.jsonParser(databaseString);
    }

    public int calculateStateRoot() {
        return state.hashCode() ^ database.hashCode();
    }
}
