public class DeployResult extends Union<Integer, String>{

    int id;
    String transactionType;

    public DeployResult(Integer result, String error) {
        super(result, error);
    }

    public static DeployResult fromResult(Integer result){
        return new DeployResult(result, null);
    }

    public static DeployResult fromError(String error){
        return new DeployResult(null, error);
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
