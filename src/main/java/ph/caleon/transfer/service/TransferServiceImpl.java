package ph.caleon.transfer.service;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.caleon.transfer.exeception.ServiceException;
import ph.caleon.transfer.handler.data.ResponseCode;
import ph.caleon.transfer.service.data.TransactionInfo;
import ph.caleon.transfer.service.data.UpdatedBalance;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ph.caleon.transfer.handler.data.ResponseCode.*;
import static ph.caleon.transfer.service.TransferServiceImpl.TransactionState.DECLINE;
import static ph.caleon.transfer.service.TransferServiceImpl.TransactionState.POSTED;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class TransferServiceImpl implements TransferService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferServiceImpl.class);

    private static final String VALIDATE_ACCTS_QUERY = "SELECT src.account_id, (src.BALANCE - :srcAmount) AS balance " +
            "FROM ACCOUNTS src WHERE src.account_id = :srcAcctId " +
            "UNION ALL SELECT tgt.account_id, (tgt.BALANCE + :tgtAmount) AS balance " +
            "FROM ACCOUNTS tgt WHERE tgt.ACCOUNT_ID = :tgtAcctId FOR UPDATE;";
    private static final String SOURCE_UPDATE_BALANCE_QUERY = "UPDATE accounts set balance = (balance - :amount) WHERE account_id = :accountId";
    private static final String TARGET_UPDATE_BALANCE_QUERY = "UPDATE accounts set balance = (balance + :amount) WHERE account_id = :accountId";
    private static final String INSERT_EVENTS_QUERY = "INSERT INTO money_movement(transaction_id, account_id, amount, currency, is_credit) " +
            "VALUES (:transactionId, :accountId, :amount, :currency, :isCredit)";
    private static final String INSERT_TXN_STATE_QUERY = "INSERT INTO transaction_state(transaction_id, initiator_account_id, transaction_amount, currency, response_code, state) " +
            "VALUES(:transactionId, :initiatorAccountId, :amount, :currency, :responseCode, :state)";

    private static final String AMOUNT_FIELD = "amount";
    private static final String ACCOUNT_ID_FIELD = "accountId";
    private static final String TRANSACTION_ID_FIELD = "transactionId";
    private static final String IS_CREDIT_FIELD = "isCredit";
    private static final String INITIATOR_ACCT_ID_FIELD = "initiatorAccountId";
    private static final String STATE_FIELD = "state";
    private static final String RESPONSE_CODE_FIELD = "responseCode";
    private static final String CURRENCY = "currency";
    private static final String SRC_AMOUNT = "srcAmount";
    private static final String SRC_ACCT_ID = "srcAcctId";
    private static final String TGT_AMOUNT = "tgtAmount";
    private static final String TGT_ACCT_ID = "tgtAcctId";
    private static final String ACCOUNT_ID = "account_id";
    private static final String BALANCE = "balance";

    private final Jdbi jdbi;

    public TransferServiceImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public UpdatedBalance transfer(TransactionInfo info) {
        return jdbi.inTransaction(handle -> startTransaction(handle, info));
    }

    private UpdatedBalance startTransaction(final Handle handle, final TransactionInfo info) {
        LOGGER.info("Beginning transaction...");
        try {
            final Handle transaction = handle.begin();
            final UpdatedBalance balance = validateTransaction(transaction, info);
            updateBalances(transaction, info);
            insertEventsEntries(transaction, info);
            insertTransactionState(transaction, info);
            transaction.commit();
            return balance;
        } catch (ServiceException e) {
          throw  e;
        } catch (Exception ex) {
            throw new ServiceException("Error occurred in transfer service.", ex, TRANSFER_ERROR);
        }
    }

    private UpdatedBalance validateTransaction(final Handle transaction, final TransactionInfo info) {
        LOGGER.info("Validating transaction...");
        try (final Query srcQuery = transaction.select(VALIDATE_ACCTS_QUERY)){
            final List<Map<String, Object>> maps = srcQuery.bind(SRC_AMOUNT, info.getSourceAmount())
                    .bind(SRC_ACCT_ID, info.getSourceAcctId())
                    .bind(TGT_AMOUNT, info.getTargetAmount())
                    .bind(TGT_ACCT_ID, info.getTargetAcctId())
                    .mapToMap().list();

            return getUpdatedBalance(info, maps);
        } catch (ServiceException e) {
            insertErrorTransactionState(info, e.getResponseCode());
            throw e;
        } catch (Exception e) {
            insertErrorTransactionState(info, TRANSFER_ERROR);
            throw new ServiceException("Error occurred while validating the accounts.", e, TRANSFER_ERROR);
        }
    }

    private UpdatedBalance getUpdatedBalance(TransactionInfo info, List<Map<String, Object>> maps) {
        if (maps.size() != 2) {
            throw new ServiceException(INVALID_ACCOUNT);
        } else {
            UpdatedBalance updatedBalance = new UpdatedBalance(info.getSourceAcctId(), info.getTargetAcctId());
            for (Map<String, Object> mapValue : maps) {
                final Long account_id = (Long) mapValue.get(ACCOUNT_ID);
                final Double balance = (Double) mapValue.get(BALANCE);
                updatedBalance.updatedBalance(account_id, balance);
            }
            return updatedBalance;
        }
    }

    private void updateBalances(final Handle transaction, final TransactionInfo info) {
        LOGGER.info("Updating account balances...");
        try (final Update sourceBalUpdate = transaction.createUpdate(SOURCE_UPDATE_BALANCE_QUERY);
             final Update targetBalUpdate = transaction.createUpdate(TARGET_UPDATE_BALANCE_QUERY)) {
            LOGGER.debug("Debiting source account...");
            sourceBalUpdate.bind(AMOUNT_FIELD, info.getSourceAmount())
                    .bind(ACCOUNT_ID_FIELD, info.getSourceAcctId()).execute();

            LOGGER.debug("Crediting target account...");
            targetBalUpdate.bind(AMOUNT_FIELD, info.getTargetAmount())
                    .bind(ACCOUNT_ID_FIELD, info.getTargetAcctId()).execute();
        } catch (Exception e) {
            insertErrorTransactionState(info, TRANSFER_ERROR);
            throw new ServiceException("Error occurred while updating the balances.", e, TRANSFER_ERROR);
        }
    }

    private void insertEventsEntries(final Handle transaction, final TransactionInfo info) {
        LOGGER.info("Saving events entries...");
        try(final PreparedBatch batch = transaction.prepareBatch(INSERT_EVENTS_QUERY)) {
            batch.bind(TRANSACTION_ID_FIELD, info.getTransactionId())
                    .bind(ACCOUNT_ID_FIELD, info.getSourceAcctId())
                    .bind(AMOUNT_FIELD, info.getSourceAmount())
                    .bind(CURRENCY, info.getCurrency())
                    .bind(IS_CREDIT_FIELD, info.getSourceIsCredit()).add();

            batch.bind(TRANSACTION_ID_FIELD, info.getTransactionId())
                    .bind(ACCOUNT_ID_FIELD, info.getTargetAcctId())
                    .bind(AMOUNT_FIELD, info.getTargetAmount())
                    .bind(CURRENCY, info.getCurrency())
                    .bind(IS_CREDIT_FIELD, info.getTargetIsCredit()).add();

            batch.execute();
        } catch (Exception e) {
            insertErrorTransactionState(info, TRANSFER_ERROR);
            throw new ServiceException("Error occurred while inserting money movement entries", e, TRANSFER_ERROR);
        }
    }

    private void insertTransactionState(final Handle transaction, final TransactionInfo info) {
        LOGGER.info("Saving success transaction...");
        try(final Update insertTxnState = transaction.createUpdate(INSERT_TXN_STATE_QUERY)) {
            insertTxnState.bind(TRANSACTION_ID_FIELD, info.getTransactionId())
                    .bind(INITIATOR_ACCT_ID_FIELD, info.getSourceAcctId())
                    .bind(AMOUNT_FIELD, info.getSourceAmount())
                    .bind(CURRENCY, info.getCurrency())
                    .bind(RESPONSE_CODE_FIELD, SUCCESSFUL.getCode())
                    .bind(STATE_FIELD, POSTED.name()).execute();
        } catch (Exception e) {
            insertErrorTransactionState(info, TRANSFER_ERROR);
            throw new ServiceException("Error occurred while inserting transaction state", e, TRANSFER_ERROR);
        }
    }

    private void insertErrorTransactionState(final TransactionInfo info, final ResponseCode code) {
        LOGGER.info("Saving error transaction with error: {}...", code.getCode());
        try(final Handle handle = jdbi.open();
            final Update insertTxnState = handle.createUpdate(INSERT_TXN_STATE_QUERY)) {
            insertTxnState.bind(TRANSACTION_ID_FIELD, info.getTransactionId())
                    .bind(INITIATOR_ACCT_ID_FIELD, info.getSourceAcctId())
                    .bind(AMOUNT_FIELD, info.getSourceAmount())
                    .bind(CURRENCY, info.getCurrency())
                    .bind(RESPONSE_CODE_FIELD, code.getCode())
                    .bind(STATE_FIELD, DECLINE.name()).execute();
        } catch (Exception e) {
            throw new ServiceException("Error occurred while inserting transaction state", e, TRANSFER_ERROR);
        }
    }


    public enum TransactionState {
        POSTED,
        DECLINE
    }
}
