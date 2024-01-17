public class DeployResult extends Union<Integer, String>{

    int id;
    String transactionType;

    public DeployResult(int id, Integer result, String error) {
        super(result, error);
        this.id = id;
        this.transactionType = "Deploy";
    }

    public static DeployResult fromResult(int id, Integer result){
        return new DeployResult(id, result, null);
    }

    public static DeployResult fromError(int id, String error){
        return new DeployResult(id, null, error);
    }

    public int getId() {
        return this.id;
    }

    public String getTransactionType() {
        return this.transactionType;
    }

    public Integer getResult() {
        return this.left;
    }

    public String getError() {
        return this.right;
    }

    public Boolean isSuccess() {
        return this.left != null;
    }

}
