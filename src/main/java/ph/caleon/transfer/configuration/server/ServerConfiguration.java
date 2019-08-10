package ph.caleon.transfer.configuration.server;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public final class ServerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);

    private final Undertow server;

    public ServerConfiguration(ServerProperties properties, HttpHandler handler) {
        Undertow.Builder builder = Undertow.builder()
                .addHttpListener(properties.getPort(), properties.getHost())
                .setHandler(handler);
        if (properties.getWorkerThread() > 0) {
            builder.setWorkerThreads(properties.getWorkerThread());
        }
        if (properties.getIoThread() > 0) {
            builder.setIoThreads(properties.getIoThread());
        }
        this.server = builder.build();
    }

    public void startServer() {
        LOGGER.info("Starting server...");
        server.start();
    }

    public void stopServer() {
        LOGGER.info("Stopping server...");
        server.stop();
    }
}
