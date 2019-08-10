package ph.caleon.transfer.exeception;

import lombok.Getter;
import ph.caleon.transfer.handler.data.ResponseCode;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class ServiceException extends TransferException {

    public ServiceException(String message, Throwable cause, ResponseCode responseCode) {
        super(message, cause, responseCode);
    }

    public ServiceException(ResponseCode responseCode) {
        super(responseCode.getDescription(), null, responseCode);
    }
}
