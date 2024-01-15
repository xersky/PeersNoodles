public class DeployResult extends Union<Integer, String>{

    public DeployResult(Integer result, String error) {
        super(result, error);
    }

    public static DeployResult fromResult(Integer result){
        return new DeployResult(result, null);
    }

    public static DeployResult fromError(String error){
        return new DeployResult(null, error);
    }
}
