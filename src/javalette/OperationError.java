package javalette;

public class OperationError extends RuntimeException {
    public OperationError(String error) {
        super(error);
    }
}
