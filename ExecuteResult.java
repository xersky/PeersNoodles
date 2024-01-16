import java.util.Optional;

public class ExecuteResult extends Union<Optional<Integer>, String>{

    int id;
    String transactionType;

    public ExecuteResult(Optional<Integer> result, String error) {
        super(result, error);
    }

    public static ExecuteResult fromResult(Optional<Integer> result){
        return new ExecuteResult(result, null);
    }

    public static ExecuteResult fromError(String error){
        return new ExecuteResult(null, error);
    }

    public Optional<Integer> getResult() {
        return this.left;
    }

    public String getError() {
        return this.right;
    }

    public Boolean isSuccess() {
        return this.left != null;
    }

}
