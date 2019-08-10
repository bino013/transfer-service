package ph.caleon.transfer.configuration.server;

import ph.caleon.transfer.configuration.ApplicationProperties;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public final class ServerFileProperties implements ServerProperties {

    private final ApplicationProperties properties;

    public ServerFileProperties(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getHost() {
        return properties.getStringOrDefault("server.host", "0.0.0.0");
    }

    @Override
    public int getPort() {
        return properties.getIntOrDefault("server.port", 8080);
    }

    @Override
    public int getWorkerThread() {
        return properties.getIntOrDefault("server.worker-thread", 0);
    }

    @Override
    public int getIoThread() {
        return properties.getIntOrDefault("server.io-thread", 0);
    }
}
