package ph.caleon.transfer.service.data;

import ph.caleon.transfer.handler.data.TransferRequest;
import lombok.Getter;

import java.util.UUID;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class TransactionInfo {

    private final Participant source;

    private final Participant target;

    @Getter
    private final String currency;

    @Getter
    private final String transactionId;

    public TransactionInfo(final TransferRequest request) {
        this(request, UUID.randomUUID().toString());
    }

    public TransactionInfo(final TransferRequest request, final String transactionId) {
        source = new Participant(request.getSourceAcctId(), request.getAmount(), false);
        target = new Participant(request.getTargetAcctId(), request.getAmount(), true);
        currency = request.getCurrency();
        this.transactionId = transactionId;
    }

    public long getSourceAcctId() {
        return source.getAccountId();
    }

    public long getTargetAcctId() {
        return target.getAccountId();
    }

    public double getSourceAmount() {
        return source.getAmount();
    }

    public double getTargetAmount() {
        return target.getAmount();
    }

    public boolean getSourceIsCredit() {
        return source.isCredit();
    }

    public boolean getTargetIsCredit() {
        return target.isCredit();
    }

}
