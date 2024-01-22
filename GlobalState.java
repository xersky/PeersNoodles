import java.util.List;
import java.util.Map;

public class GlobalState {
    private List<Map<String,String>> transactions;
    private List<Map<String,String>> receipts;
    private Map<String,String> state;
    private Map<String,String> database;

    private GlobalState() {
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
}
