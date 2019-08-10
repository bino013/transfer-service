package ph.caleon.transfer.service;

import org.junit.Before;
import org.junit.Test;
import ph.caleon.transfer.configuration.ApplicationProperties;
import ph.caleon.transfer.configuration.DatabaseConfiguration;
import ph.caleon.transfer.handler.data.TransferRequest;
import ph.caleon.transfer.service.data.TransactionInfo;
import ph.caleon.transfer.service.exception.ServiceException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static ph.caleon.transfer.handler.data.ResponseCode.*;
import static ph.caleon.transfer.service.TransferServiceImpl.TransactionState.DECLINE;
import static ph.caleon.transfer.service.TransferServiceImpl.TransactionState.POSTED;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class TransferServiceTest {

    private static final String CONNECTION_STR = "jdbc:h2:~/test;SCHEMA=TRANSFER_APP";
    private static final long SOURCE_ACCT_ID = 2100001;
    private static final long TARGET_ACCT_ID = 2100002;
    private static final double TXN_AMOUNT = 10;
    private static final String CURRENCY = "PHP";
    private static final int INITIAL_BALANCE = 100;

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
        transferService.transfer(info);
        assertAcctTable(INITIAL_BALANCE - TXN_AMOUNT, SOURCE_ACCT_ID);
        assertAcctTable(INITIAL_BALANCE + TXN_AMOUNT, TARGET_ACCT_ID);
        assertTransactionStateTable(info, SUCCESSFUL.getCode(), POSTED.name());
        assertMoneyMovementTable(2);
    }

    @Test
    public void testTransfer_insufficientFunds() throws SQLException {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, TARGET_ACCT_ID, (TXN_AMOUNT * 100), CURRENCY);
        final TransactionInfo info = new TransactionInfo(request);
        try{
            transferService.transfer(info);
            fail("Test should not reach here");
        } catch (ServiceException e) {
            System.out.println("Message: " + e.getMessage());
            assertEquals(INSUFFICIENT_BALANCE, e.getResponseCode());
            assertAcctTable(INITIAL_BALANCE, SOURCE_ACCT_ID);
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

    private void dropAcct() throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        connection.createStatement().execute("DROP TABLE IF EXISTS accounts;\n" +
                "DROP TABLE IF EXISTS accounts_1;");
        connection.close();
    }

    private void dropMoneyMovement() throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        connection.createStatement().execute("DROP TABLE IF EXISTS money_movement;");
        connection.close();
    }

    private void dropTransactionState() throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        connection.createStatement().execute("DROP TABLE IF EXISTS transaction_state;");
        connection.close();
    }

    private void assertAcctTable(final double expectedBalance, final long accountId) throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        Statement statement = connection.createStatement();
        statement.execute("SELECT balance FROM accounts where account_id = " + accountId);
        final ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        final double availableBalance = resultSet.getDouble("balance");
        connection.close();
        assertEquals(expectedBalance, availableBalance);
    }

    private void assertTransactionStateTable(final TransactionInfo info, final String responseCode, final String state) throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        Statement statement = connection.createStatement();
        statement.execute("SELECT * FROM transaction_state WHERE transaction_id = '" + info.getTransactionId() + "'");
        final ResultSet resultSet = statement.getResultSet();
        int count = 0;
        while (resultSet.next()) {
            assertEquals(state, resultSet.getString("state"));
            assertEquals(responseCode, resultSet.getString("response_code"));
            assertEquals(info.getSourceAcctId(), resultSet.getLong("initiator_account_id"));
            assertEquals(info.getSourceAmount(), resultSet.getDouble("transaction_amount"));
        }
        resultSet.close();
        connection.close();
    }

    private void assertMoneyMovementTable(final int expectedEntries) throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        Statement statement = connection.createStatement();
        statement.execute("SELECT amount, is_credit FROM money_movement");
        final ResultSet resultSet = statement.getResultSet();
        int count = 0;
        BigDecimal adder = new BigDecimal(0);
        while (resultSet.next()) {
            BigDecimal amount = new BigDecimal(resultSet.getDouble("amount"));
            final boolean isCredit = resultSet.getBoolean("is_credit");
            amount = isCredit ? amount : amount.negate();
            adder = adder.add(amount);
            count++;
        }
        assertEquals(expectedEntries, count);
        assertEquals(0D, adder.doubleValue());
        resultSet.close();
        connection.close();
    }

}
