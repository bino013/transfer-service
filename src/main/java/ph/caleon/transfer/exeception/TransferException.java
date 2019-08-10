package ph.caleon.transfer.exeception;

import lombok.Getter;
import ph.caleon.transfer.handler.data.ResponseCode;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class TransferException extends RuntimeException {

    @Getter
    private final ResponseCode responseCode;

    public TransferException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public TransferException(ResponseCode responseCode) {
        this(responseCode.getDescription(), null, responseCode);
    }

    public TransferException(String message, ResponseCode responseCode) {
        this(message, null, responseCode);
    }
}
