public class Union<TLeft, TRight> {
    
    TLeft left;
    TRight right;

    public Union(TLeft result, TRight right){
        this.left = result;
        this.right = right;
    }

    public static <TLeft, TRight> Union<TLeft, TRight> fromLeft(TLeft left){
        return new Union<TLeft,TRight>(left, null);
    }

    public static <TLeft, TRight> Union<TLeft, TRight> fromRight(TRight right){
        return new Union<TLeft,TRight>(null, right);
    }

    
    public TLeft getLeft() {
        return this.left;
    }

    public TRight getRight() {
        return this.right;
    }

    public Boolean isRight() {
        return right != null;
    }

    public Boolean isLeft() {
        return left != null;
    }
}
