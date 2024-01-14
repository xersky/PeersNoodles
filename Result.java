public class Result<TResult, TError> {
    
    TResult result;
    TError error;

    public Result(TResult result, TError error){
        this.result = result;
        this.error = error;
    }

    public static <TResult, TError> Result<TResult, TError> fromResult(TResult result){
        return new Result<TResult,TError>(result, null);
    }

    public static <TResult, TError> Result<TResult, TError> fromError(TError error){
        return new Result<TResult,TError>(null, error);
    }

    
    public TResult getResult() {
        return this.result;
    }

    public TError getError() {
        return this.error;
    }

    public Boolean isError() {
        return error != null;
    }

    public Boolean isResult() {
        return result != null;
    }
}
