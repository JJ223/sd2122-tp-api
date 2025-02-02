package tp1.clients.soap;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import tp1.api.service.util.Result;
import tp1.clients.rest.Client;

import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class SoapClient {

    private static Logger Log = Logger.getLogger(Client.class.getName());

    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;

    protected static final int RETRY_SLEEP = 1000;
    protected static final int MAX_RETRIES = 3;

    protected static String WSDL = "?wsdl";

    final URI serverURI;
    protected final jakarta.ws.rs.client.Client client;
    final ClientConfig config;

    protected SoapClient(URI serverURI) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);
    }

    protected <T> T reTry(Supplier<T> func) {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (WebApplicationException x) {
                Log.fine("ProcessingException: " + x.getMessage());
                sleep(RETRY_SLEEP);
            } catch (Exception x) {
                Log.fine("Exception: " + x.getMessage());
                sleep(RETRY_SLEEP);
            }
        return (T) Result.error(Result.ErrorCode.TIMEOUT);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException x) { // nothing to do...
        }
    }

    protected <T> Result<T> getResultError(Response.Status r) {
        try {
            Result.ErrorCode code = Result.ErrorCode.valueOf(r.name());
            return Result.error(code);

        } catch(IllegalArgumentException e) {
            return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
        }
    }
}