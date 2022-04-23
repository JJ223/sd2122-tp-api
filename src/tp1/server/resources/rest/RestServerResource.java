package tp1.server.resources.rest;

import jakarta.ws.rs.core.Response;
import tp1.api.service.util.Result;

public class RestServerResource {
	protected static Response.Status getErrorException(Result.ErrorCode err) {
        switch(err) {
            case CONFLICT:
                return Response.Status.CONFLICT;
            case NOT_FOUND:
                return Response.Status.NOT_FOUND;
            case BAD_REQUEST:
                return Response.Status.BAD_REQUEST;
            case FORBIDDEN:
                return Response.Status.FORBIDDEN;
            case INTERNAL_ERROR:
                return Response.Status.INTERNAL_SERVER_ERROR;
            case NOT_IMPLEMENTED:
                return Response.Status.NOT_IMPLEMENTED;
            default:
                return Response.Status.BAD_REQUEST;
        }
    }
}
