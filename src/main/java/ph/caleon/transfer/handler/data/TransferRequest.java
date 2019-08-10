package ph.caleon.transfer.handler.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author arvin.caleon on 2019-08-03
 **/
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    private Long sourceAcctId;

    private Long targetAcctId;

    private Double amount;

    private String currency;

}
