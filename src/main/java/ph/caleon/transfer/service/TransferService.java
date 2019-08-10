package ph.caleon.transfer.service;

import ph.caleon.transfer.service.data.TransactionInfo;
import ph.caleon.transfer.service.data.UpdatedBalance;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public interface TransferService {

    UpdatedBalance transfer(TransactionInfo info);

}
