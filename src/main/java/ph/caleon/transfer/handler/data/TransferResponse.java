package ph.caleon.transfer.handler.data;

import lombok.Value;

/**
 * @author arvin.caleon on 2019-08-03
 **/
@Value
public class TransferResponse {

    private final String code;

    private final String message;

    private final String referenceId;
}