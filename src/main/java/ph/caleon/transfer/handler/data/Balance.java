package ph.caleon.transfer.handler.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import ph.caleon.transfer.service.data.UpdatedBalance;

/**
 * @author arvin.caleon on 2019-08-11
 **/
@NoArgsConstructor
public class Balance {

    @JsonIgnore
    private long srcAcctId;

    @JsonIgnore
    private long tgtAcctId;

    @Getter
    private Double sourceUpdatedBalance;

    @Getter
    private Double targetUpdatedBalance;

    public Balance(TransferRequest request) {
        this.srcAcctId = request.getSourceAcctId();
        this.tgtAcctId = request.getTargetAcctId();
    }

    public void setUpdatedBalances(UpdatedBalance balances) {
        this.sourceUpdatedBalance = balances.getBalance(srcAcctId);
        this.targetUpdatedBalance = balances.getBalance(tgtAcctId);
    }
}
