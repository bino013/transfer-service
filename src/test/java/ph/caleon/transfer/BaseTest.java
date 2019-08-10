package ph.caleon.transfer;

import ph.caleon.transfer.service.data.TransactionInfo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.TestCase.assertEquals;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class BaseTest {

    private static final String CONNECTION_STR = "jdbc:h2:~/test;SCHEMA=TRANSFER_APP_TEST";
    protected static final long SOURCE_ACCT_ID = 2100001;
    protected static final long TARGET_ACCT_ID = 2100002;
    protected static final long INSUFFICIENT_ACCT_ID = 2100003;
    protected static final double TXN_AMOUNT = 10;
    protected static final String CURRENCY = "PHP";
    protected static final int INITIAL_BALANCE = 100;

    protected void dropAcct() throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        connection.createStatement().execute("DROP TABLE IF EXISTS accounts;\n" +
                "DROP TABLE IF EXISTS accounts_1;");
        connection.close();
    }

    protected void dropMoneyMovement() throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        connection.createStatement().execute("DROP TABLE IF EXISTS money_movement;");
        connection.close();
    }

    protected void dropTransactionState() throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        connection.createStatement().execute("DROP TABLE IF EXISTS transaction_state;");
        connection.close();
    }

    protected void assertAcctTable(final double expectedBalance, final long accountId) throws SQLException {
        Connection connection = DriverManager.getConnection(CONNECTION_STR);
        Statement statement = connection.createStatement();
        statement.execute("SELECT balance FROM accounts where account_id = " + accountId);
        final ResultSet resultSet = statement.getResultSet();
        resultSet.next();
        final double availableBalance = resultSet.getDouble("balance");
        connection.close();
        assertEquals(expectedBalance, availableBalance);
    }

    protected void assertTransactionStateTable(final TransactionInfo info, final String responseCode, final String state) throws SQLException {
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

    protected void assertMoneyMovementTable(final int expectedEntries) throws SQLException {
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
