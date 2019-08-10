package ph.caleon.transfer.handler.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * @author arvin.caleon on 2019-08-03
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {

    private String code;

    private String message;

    private String transactionId;

    public TransferResponse(final ResponseCode code, final String transactionId) {
        this(code);
        this.transactionId = transactionId;
    }

    public TransferResponse(final ResponseCode code) {
        this.code = code.getCode();
        this.message = code.getDescription();
    }
}