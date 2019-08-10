package ph.caleon.transfer;

import ph.caleon.transfer.configuration.ApplicationProperties;
import ph.caleon.transfer.configuration.server.HttpPathManager;
import ph.caleon.transfer.configuration.server.ServerConfiguration;
import ph.caleon.transfer.configuration.server.ServerFileProperties;
import ph.caleon.transfer.handler.TransferHandler;
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
