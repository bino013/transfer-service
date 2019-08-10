package ph.caleon.transfer.configuration.server;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public interface ServerProperties {

    String getHost();

    int getPort();

    int getWorkerThread();

    int getIoThread();

}
