package ph.caleon.transfer.service;

import org.junit.Before;
import org.junit.Test;
import ph.caleon.transfer.BaseTest;
import ph.caleon.transfer.configuration.ApplicationProperties;
import ph.caleon.transfer.configuration.DatabaseConfiguration;
import ph.caleon.transfer.exeception.ServiceException;
import ph.caleon.transfer.exeception.TransferException;
import ph.caleon.transfer.handler.data.TransferRequest;
import ph.caleon.transfer.service.data.TransactionInfo;
import ph.caleon.transfer.service.data.UpdatedBalance;

import java.sql.SQLException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static ph.caleon.transfer.handler.data.ResponseCode.*;
import static ph.caleon.transfer.service.TransferServiceImpl.TransactionState.DECLINE;
import static ph.caleon.transfer.service.TransferServiceImpl.TransactionState.POSTED;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class TransferServiceTest extends BaseTest {

    private TransferService transferService;

    @Before
    public void setUpDB() {
        ApplicationProperties properties = new ApplicationProperties();
        DatabaseConfiguration dbConfig = new DatabaseConfiguration(properties);
        transferService = new TransferServiceImpl(dbConfig.getJdbi());
    }

    @Test
    public void testTransfer() throws SQLException {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        final UpdatedBalance updatedBalance = transferService.transfer(info);
        assertEquals(INITIAL_BALANCE - TXN_AMOUNT, updatedBalance.getBalance(SOURCE_ACCT_ID));
        assertEquals(INITIAL_BALANCE + TXN_AMOUNT, updatedBalance.getBalance(TARGET_ACCT_ID));
        assertAcctTable(INITIAL_BALANCE - TXN_AMOUNT, SOURCE_ACCT_ID);
        assertAcctTable(INITIAL_BALANCE + TXN_AMOUNT, TARGET_ACCT_ID);
        assertTransactionStateTable(info, SUCCESSFUL.getCode(), POSTED.name());
        assertMoneyMovementTable(2);
    }

    @Test
    public void testTransfer_insufficientFunds() throws SQLException {
        TransferRequest request = new TransferRequest(INSUFFICIENT_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        try{
            transferService.transfer(info);
            fail("Test should not reach here");
        } catch (ServiceException e) {
            System.out.println("Message: " + e.getMessage());
            assertEquals(INSUFFICIENT_BALANCE, e.getResponseCode());
            assertAcctTable(0D, INSUFFICIENT_ACCT_ID);
            assertAcctTable(INITIAL_BALANCE, TARGET_ACCT_ID);
            assertTransactionStateTable(info, INSUFFICIENT_BALANCE.getCode(), DECLINE.name());
            assertMoneyMovementTable(0);
        }
    }

    @Test
    public void testTransfer_validationError() throws SQLException {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        try{
            dropAcct();
            transferService.transfer(info);
            fail("Test should not reach here");
        } catch (ServiceException e) {
            System.out.println("Message: " + e.getMessage());
            assertEquals(TRANSFER_ERROR, e.getResponseCode());
            assertTransactionStateTable(info, TRANSFER_ERROR.getCode(), DECLINE.name());
            assertMoneyMovementTable(0);
        }
    }

    @Test
    public void testTransfer_moneyMovementSqlError() throws SQLException {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        try{
            dropMoneyMovement();
            transferService.transfer(info);
            fail("Test should not reach here");
        } catch (ServiceException e) {
            System.out.println("Message: " + e.getMessage());
            assertEquals(TRANSFER_ERROR, e.getResponseCode());
            assertAcctTable(INITIAL_BALANCE, SOURCE_ACCT_ID);
            assertAcctTable(INITIAL_BALANCE, TARGET_ACCT_ID);
            assertTransactionStateTable(info, TRANSFER_ERROR.getCode(), DECLINE.name());
        }
    }

    @Test
    public void testTransfer_transactionStateSqlError() throws SQLException {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        try{
            dropTransactionState();
            transferService.transfer(info);
            fail("Test should not reach here");
        } catch (ServiceException e) {
            System.out.println("Message: " + e.getMessage());
            assertEquals(TRANSFER_ERROR, e.getResponseCode());
            assertAcctTable(INITIAL_BALANCE, SOURCE_ACCT_ID);
            assertAcctTable(INITIAL_BALANCE, TARGET_ACCT_ID);
            assertMoneyMovementTable(0);
        }
    }

    @Test
    public void testTransfer_invalidSourceAcct() throws SQLException {
        TransferRequest request = new TransferRequest(INVALID_SRC_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        try{
            transferService.transfer(info);
            fail("Test should not reach here");
        } catch (ServiceException e) {
            System.out.println("Message: " + e.getMessage());
            assertEquals(INVALID_ACCOUNT, e.getResponseCode());
            assertTransactionStateTable(info, INVALID_ACCOUNT.getCode(), DECLINE.name());
            assertMoneyMovementTable(0);
        }
    }

    @Test
    public void testTransfer_invalidTargetAcct() throws SQLException {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, INVALID_TGT_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        try{
            transferService.transfer(info);
            fail("Test should not reach here");
        } catch (ServiceException e) {
            System.out.println("Message: " + e.getMessage());
            assertEquals(INVALID_ACCOUNT, e.getResponseCode());
            assertTransactionStateTable(info, INVALID_ACCOUNT.getCode(), DECLINE.name());
            assertMoneyMovementTable(0);
        }
    }

    @Test
    public void testTransfer_invalidSourceAndTargetAcct() throws SQLException {
        TransferRequest request = new TransferRequest(INVALID_SRC_ACCT_ID, INVALID_TGT_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        try{
            transferService.transfer(info);
            fail("Test should not reach here");
        } catch (ServiceException e) {
            System.out.println("Message: " + e.getMessage());
            assertEquals(INVALID_ACCOUNT, e.getResponseCode());
            assertTransactionStateTable(info, INVALID_ACCOUNT.getCode(), DECLINE.name());
            assertMoneyMovementTable(0);
        }
    }
}
