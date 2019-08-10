package ph.caleon.transfer.service.exception;

import ph.caleon.transfer.handler.data.ResponseCode;
import lombok.Getter;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class ServiceException extends RuntimeException {

    @Getter
    private final ResponseCode responseCode;

    public ServiceException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause);
        this.responseCode = responseCode;
    }

    public ServiceException(ResponseCode responseCode) {
        this(responseCode.getDescription(), null, responseCode);
    }
}
