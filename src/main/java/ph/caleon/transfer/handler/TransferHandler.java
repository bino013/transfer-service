package ph.caleon.transfer.handler;

import ph.caleon.transfer.handler.data.TransferRequest;
import ph.caleon.transfer.util.JSONUtil;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public class TransferHandler implements HttpHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferHandler.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getRequestReceiver().receiveFullString(this::processHttpRequest, this::processErrorCallback);
    }

    private void processHttpRequest(HttpServerExchange exchange, String request) {
        LOGGER.info("Transfer request received. Request: {}", request);
        final TransferRequest transferRequest = JSONUtil.toObject(request, TransferRequest.class);
        LOGGER.info("Request: {}", JSONUtil.toString(transferRequest));
    }

    private void processErrorCallback(HttpServerExchange exchange, IOException e) {

    }

    private static void sendResponse(HttpServerExchange exchange, String response, String contentType) {
        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
        exchange.getResponseSender().send(response);
        exchange.endExchange();
    }
}
