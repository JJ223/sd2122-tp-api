package tp1.clients;

import java.net.URI;
import java.util.function.Supplier;
import java.util.logging.Logger;

import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import tp1.api.service.util.Result;

public class Client {
	private static Logger Log = Logger.getLogger(Client.class.getName());

	protected static final int READ_TIMEOUT = 10000;
	protected static final int CONNECT_TIMEOUT = 10000;

	protected static final int RETRY_SLEEP = 1000;
	protected static final int MAX_RETRIES = 3;

	final URI serverURI;
	protected final jakarta.ws.rs.client.Client client;
	final ClientConfig config;

	protected Client(URI serverURI) {
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
			} catch (ProcessingException x) {
				Log.fine("ProcessingException: " + x.getMessage());
				sleep(RETRY_SLEEP);
			} catch (Exception x) {
				Log.fine("Exception: " + x.getMessage());
				x.printStackTrace();
				break;
			}
		return null;
	}

	private void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException x) { // nothing to do...
		}
	}

	protected <T> Result<T> getResultError(Response.Status r) {
		try {
			Result.ErrorCode code = Result.ErrorCode.valueOf(r.toString());
			return Result.error(code);

		} catch(IllegalArgumentException e) {
			return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
		}
	}
}
