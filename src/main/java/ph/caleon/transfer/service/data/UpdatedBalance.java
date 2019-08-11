package ph.caleon.transfer.service.data;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import ph.caleon.transfer.exeception.ServiceException;

import java.util.HashMap;
import java.util.Map;

import static ph.caleon.transfer.handler.data.ResponseCode.INSUFFICIENT_BALANCE;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class UpdatedBalance {

    private Map<Long, Double> accountIdBalance = new HashMap<>();

    public UpdatedBalance(final long srcAcctId, final long tgtAcctId) {
        accountIdBalance.put(srcAcctId, (double) 0);
        accountIdBalance.put(tgtAcctId, (double) 0);
    }

    public void updatedBalance(final Long accountId, final Double balance) {
        if (Double.compare(balance, 0) < 0) {
            throw new ServiceException(INSUFFICIENT_BALANCE);
        }
        accountIdBalance.put(accountId, balance);
    }

    public Double getBalance(final Long accountId) {
        return accountIdBalance.get(accountId);
    }
}
