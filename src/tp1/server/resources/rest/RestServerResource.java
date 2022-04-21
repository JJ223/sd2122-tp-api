package tp1.server.resources.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.service.util.Result;

public class RestServerResource {
	protected void getErrorException(Result.ErrorCode err) {
		switch(err) {
		case CONFLICT:
			throw new WebApplicationException(Status.CONFLICT);
		case NOT_FOUND:
			throw new WebApplicationException(Status.NOT_FOUND);
		case BAD_REQUEST:
			throw new WebApplicationException(Status.BAD_REQUEST);
		case FORBIDDEN:
			throw new WebApplicationException(Status.FORBIDDEN);
		case INTERNAL_ERROR:
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		case NOT_IMPLEMENTED:
			throw new WebApplicationException(Status.NOT_IMPLEMENTED);
		default:
		}
	}
}
