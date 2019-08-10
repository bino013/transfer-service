package caleon.revolut.transfer;

import caleon.revolut.transfer.configuration.ApplicationProperties;
import caleon.revolut.transfer.configuration.HttpPathManager;
import caleon.revolut.transfer.configuration.ServerConfiguration;
import caleon.revolut.transfer.configuration.properties.ServerFileProperties;
import caleon.revolut.transfer.handler.TransferHandler;
import io.undertow.util.Methods;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public class TransferApplication {

    public static void main(String[] args) {
        ApplicationProperties properties = new ApplicationProperties();
        HttpPathManager pathManager = new HttpPathManager();
        pathManager.addHandler(Methods.POST, "/transfer", new TransferHandler());
        ServerConfiguration serverConfiguration = new ServerConfiguration(new ServerFileProperties(properties),
                pathManager.getPathHandler());
        serverConfiguration.startServer();

        Runtime.getRuntime().addShutdownHook(new Thread(serverConfiguration::stopServer));
    }

}
