package ph.caleon.transfer.service.data;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import ph.caleon.transfer.exeception.ServiceException;

import static ph.caleon.transfer.handler.data.ResponseCode.INSUFFICIENT_BALANCE;

/**
 * @author arvin.caleon on 2019-08-10
 **/
@Setter
@Getter
public class UpdatedBalance {

    private Double sourceUpdatedBalance;

    private Double targetUpdatedBalance;

    public void setSourceUpdatedBalance(Double sourceUpdatedBalance) {
        if (Double.compare(sourceUpdatedBalance, 0) < 0) {
            throw new ServiceException(INSUFFICIENT_BALANCE);
        }
        this.sourceUpdatedBalance = sourceUpdatedBalance;
    }
}
