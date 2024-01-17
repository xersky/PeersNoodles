import java.util.Optional;

public class ExecuteResult extends Union<Optional<Integer>, String>{

    int id;
    String transactionType;

    public ExecuteResult(int id, Optional<Integer> result, String error) {
        super(result, error);
        this.id = id;
        this.transactionType = "Execute";
    }

    public static ExecuteResult fromResult(int id, Optional<Integer> result){
        return new ExecuteResult(id, result, null);
    }

    public static ExecuteResult fromError(int id, String error){
        return new ExecuteResult(id, null, error);
    }

    public int getId() {
        return this.id;
    }

    public String getTransactionType() {
        return this.transactionType;
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
