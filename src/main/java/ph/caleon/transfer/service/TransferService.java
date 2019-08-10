package ph.caleon.transfer.service;

import ph.caleon.transfer.service.data.TransactionInfo;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public interface TransferService {

    void transfer(TransactionInfo info);

}
