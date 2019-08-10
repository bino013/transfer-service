package ph.caleon.transfer.service.data;

import lombok.Value;

import java.math.BigDecimal;

/**
 * @author arvin.caleon on 2019-08-10
 **/
@Value
public class Participant {

    private final long accountId;

    private final double amount;

    private final boolean isCredit;

}
