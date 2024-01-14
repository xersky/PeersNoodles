import java.util.Optional;

public class ExecuteResult extends Result<Optional<Integer>, String>{

    public ExecuteResult(Optional<Integer> result, String error) {
        super(result, error);
    }

    public static ExecuteResult fromResult(Optional<Integer> result){
        return new ExecuteResult(result, null);
    }

    public static ExecuteResult fromError(String error){
        return new ExecuteResult(null, error);
    }

}
