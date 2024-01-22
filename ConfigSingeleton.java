public class ConfigSingeleton {
    
    public static ConfigSingeleton instance;
    private String transactionsFilename;
    private String stateFilename;
    private String databaseFilename;
    private String receiptsFilename;

    private ConfigSingeleton(){
        this.transactionsFilename = "Transactions.json";
        this.stateFilename = "State.json";
        this.databaseFilename = "Database.json";
        this.receiptsFilename = "Receipts.json";
    }

    public static synchronized ConfigSingeleton getInstance() {
        if(instance == null) {
            instance = new ConfigSingeleton();
        }
        return instance;
    }

    public String getTransactionsFilename() {
        return transactionsFilename;
    }

    public String getStateFilename() {
        return stateFilename;
    }

    public String getDatabaseFilename() {
        return databaseFilename;
    }

    public String getReceiptsFilename() {
        return receiptsFilename;
    }
}
