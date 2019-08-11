package ph.caleon.transfer.handler;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.undertow.util.StatusCodes;
import org.junit.BeforeClass;
import org.junit.Test;
import ph.caleon.transfer.BaseTest;
import ph.caleon.transfer.TransferApplication;
import ph.caleon.transfer.handler.data.TransferRequest;
import ph.caleon.transfer.handler.data.TransferResponse;
import ph.caleon.transfer.util.JSONUtil;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;
import static ph.caleon.transfer.handler.data.ResponseCode.*;

/**
 * @author arvin.caleon on 2019-08-10
 **/
public class TransferHandlerTest extends BaseTest {

    private static final String TRANSFER_ENDPOINT = "http://127.0.0.1:8082/v1/transfer";

    @BeforeClass
    public static void setUp() {
        TransferApplication.main(new String[]{});
    }

    @Test
    public void testTransferApi() {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertEquals(new Double(INITIAL_BALANCE + TXN_AMOUNT), transferResponse.getUpdatedBalance().getTargetUpdatedBalance());
        assertEquals(new Double(INITIAL_BALANCE - TXN_AMOUNT), transferResponse.getUpdatedBalance().getSourceUpdatedBalance());
        assertEquals(SUCCESSFUL.getCode(), transferResponse.getCode());
        assertEquals(SUCCESSFUL.getDescription(), transferResponse.getMessage());
        assertFalse(transferResponse.getTransactionId().isEmpty());

    }

    @Test
    public void testTransferApi_sourceIsNull() {
        TransferRequest request = new TransferRequest(null, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(REQUEST_VALIDATION_ERROR.getCode(), transferResponse.getCode());
        assertFalse(transferResponse.getMessage().isEmpty());
        assertFalse(transferResponse.getTransactionId().isEmpty());
    }

    @Test
    public void testTransferApi_targetIsNull() {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, null, TXN_AMOUNT, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(REQUEST_VALIDATION_ERROR.getCode(), transferResponse.getCode());
        assertFalse(transferResponse.getMessage().isEmpty());
        assertFalse(transferResponse.getTransactionId().isEmpty());
    }

    @Test
    public void testTransferApi_amountIsNull() {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, TARGET_ACCT_ID, null, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(REQUEST_VALIDATION_ERROR.getCode(), transferResponse.getCode());
        assertFalse(transferResponse.getMessage().isEmpty());
        assertFalse(transferResponse.getTransactionId().isEmpty());
    }

    @Test
    public void testTransferApi_currencyIsNull() {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, null);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(REQUEST_VALIDATION_ERROR.getCode(), transferResponse.getCode());
        assertFalse(transferResponse.getMessage().isEmpty());
        assertFalse(transferResponse.getTransactionId().isEmpty());
    }

    @Test
    public void testTransferApi_insufficientBalance() {
        TransferRequest request = new TransferRequest(INSUFFICIENT_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(INSUFFICIENT_BALANCE.getCode(), transferResponse.getCode());
        assertEquals(INSUFFICIENT_BALANCE.getDescription(), transferResponse.getMessage());
        assertFalse(transferResponse.getMessage().isEmpty());
    }

    @Test
    public void testTransferApi_invalidSourceAcct() {
        TransferRequest request = new TransferRequest(INVALID_SRC_ACCT_ID, TARGET_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(INVALID_ACCOUNT.getCode(), transferResponse.getCode());
        assertEquals(INVALID_ACCOUNT.getDescription(), transferResponse.getMessage());
        assertFalse(transferResponse.getMessage().isEmpty());
    }

    @Test
    public void testTransferApi_invalidTargetAcct() {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, INVALID_TGT_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(INVALID_ACCOUNT.getCode(), transferResponse.getCode());
        assertEquals(INVALID_ACCOUNT.getDescription(), transferResponse.getMessage());
        assertFalse(transferResponse.getMessage().isEmpty());
    }

    @Test
    public void testTransferApi_invalidSourceAndTargetAcct() {
        TransferRequest request = new TransferRequest(INVALID_SRC_ACCT_ID, INVALID_TGT_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(INVALID_ACCOUNT.getCode(), transferResponse.getCode());
        assertEquals(INVALID_ACCOUNT.getDescription(), transferResponse.getMessage());
        assertFalse(transferResponse.getMessage().isEmpty());
    }

    @Test
    public void testTransferApi_transferToSameAcct() {
        TransferRequest request = new TransferRequest(SOURCE_ACCT_ID, SOURCE_ACCT_ID, TXN_AMOUNT, CURRENCY);
        final String requestStr = JSONUtil.toString(request);
        final Response response = callTransferApi(requestStr);
        final TransferResponse transferResponse = JSONUtil.toObject(response.asString(), TransferResponse.class);
        assertEquals(StatusCodes.OK, response.statusCode());
        assertNull(transferResponse.getUpdatedBalance());
        assertEquals(REQUEST_VALIDATION_ERROR.getCode(), transferResponse.getCode());
        assertFalse(transferResponse.getMessage().isEmpty());
        assertFalse(transferResponse.getMessage().isEmpty());
    }

    private Response callTransferApi(String request) {
        return given()
                    .contentType(ContentType.JSON)
                    .body(request)
                .when()
                    .post(TRANSFER_ENDPOINT);
    }

}
