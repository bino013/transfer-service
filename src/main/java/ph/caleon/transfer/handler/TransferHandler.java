package ph.caleon.transfer.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.caleon.transfer.exeception.TransferException;
import ph.caleon.transfer.handler.data.Balance;
import ph.caleon.transfer.handler.data.TransferRequest;
import ph.caleon.transfer.handler.data.TransferResponse;
import ph.caleon.transfer.service.TransferService;
import ph.caleon.transfer.service.data.TransactionInfo;
import ph.caleon.transfer.service.data.UpdatedBalance;
import ph.caleon.transfer.util.JSONUtil;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static ph.caleon.transfer.handler.data.ResponseCode.*;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public class TransferHandler implements HttpHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferHandler.class);

    private static final String APPLICATION_JSON = "application/json";

    private final TransferService service;

    public TransferHandler(TransferService service) {
        this.service = service;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getRequestReceiver().receiveFullString(this::processHttpRequest, this::processErrorCallback);
    }

    private void processHttpRequest(HttpServerExchange exchange, String request) {
        LOGGER.info("Transfer request received. Request: {}", request);
        String transactionId = UUID.randomUUID().toString();
        String response = null;
        try {
            TransferRequest transferRequest = validateRequest(request);
            Balance balance = new Balance(transferRequest);
            balance.setUpdatedBalances(service.transfer(new TransactionInfo(transferRequest, transactionId)));
            response = JSONUtil.toString(new TransferResponse(SUCCESSFUL, transactionId, balance));
        } catch (TransferException e) {
            response = JSONUtil.toString(new TransferResponse(e.getResponseCode().getCode(), e.getMessage(), transactionId));
            LOGGER.error("Error occurred while processing transfer request", e);
        } catch (Exception e) {
            response = JSONUtil.toString(new TransferResponse(GENERIC_ERROR, transactionId));
            LOGGER.error("Error occurred", e);
        }
        sendResponse(exchange, response);
    }

    private TransferRequest validateRequest(final String request) {
        try {
            final TransferRequest transferRequest = JSONUtil.toObject(request, TransferRequest.class);
            if (Objects.isNull(transferRequest.getSourceAcctId()) || Objects.isNull(transferRequest.getTargetAcctId())
                    || Objects.isNull(transferRequest.getAmount()) || Objects.isNull(transferRequest.getCurrency())) {
                throw new TransferException("`source_acct_id`, `target_acct_id`, `amount` and `currency` are required fields", REQUEST_VALIDATION_ERROR);
            } else if (transferRequest.getSourceAcctId().equals(transferRequest.getTargetAcctId())) {
                throw new TransferException("You cannot transfer to the same account", REQUEST_VALIDATION_ERROR);
            }
            return transferRequest;
        } catch (TransferException e) {
            throw e;
        } catch (Exception e) {
            throw new TransferException("Request cannot be parsed", REQUEST_VALIDATION_ERROR);
        }
    }

    private void processErrorCallback(HttpServerExchange exchange, IOException e) {
        LOGGER.error("Error occurred while processing http request", e);
        sendResponse(exchange, JSONUtil.toString(new TransferResponse(REQUEST_VALIDATION_ERROR)));
    }

    private static void sendResponse(HttpServerExchange exchange, String response) {
        LOGGER.info("Response: {}", response );
        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, APPLICATION_JSON);
        exchange.getResponseSender().send(response);
        exchange.endExchange();
    }
}
