package ph.caleon.transfer.handler.data;

import lombok.Value;

import java.math.BigDecimal;

/**
 * @author arvin.caleon on 2019-08-03
 **/
@Value
public class TransferRequest {

    private final long sourceAcctId;

    private final long targetAcctId;

    private final double amount;

    private final String currency;

}
