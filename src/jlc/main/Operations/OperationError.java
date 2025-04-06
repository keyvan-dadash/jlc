package jlc.main.Operations;

// OperationError trhows an error that indicate what is went wrong with the operation. 
public class OperationError extends RuntimeException {
    public OperationError(String error) {
        super(error);
    }
}
