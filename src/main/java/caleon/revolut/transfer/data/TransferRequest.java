package caleon.revolut.transfer.data;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public class TransferRequest {

    private long sourceAcctId;

    private long targetAcctId;

    private double amount;

    private String currency;

    public TransferRequest() {
    }

    public TransferRequest(long sourceAcctId, long targetAcctId, double amount, String currency) {
        this.sourceAcctId = sourceAcctId;
        this.targetAcctId = targetAcctId;
        this.amount = amount;
        this.currency = currency;
    }

    public long getSourceAcctId() {
        return sourceAcctId;
    }

    public void setSourceAcctId(long sourceAcctId) {
        this.sourceAcctId = sourceAcctId;
    }

    public long getTargetAcctId() {
        return targetAcctId;
    }

    public void setTargetAcctId(long targetAcctId) {
        this.targetAcctId = targetAcctId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
