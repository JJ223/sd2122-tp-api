package tp1.clients;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;

import java.net.URI;

public class RestFileClient extends RestClient implements Files {

    final WebTarget target;

    public RestFileClient(URI serverURI) {
        super( serverURI );
        target = client.target( serverURI ).path( RestFiles.PATH );
    }

    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        return super.reTry( () -> {
            return clt_writeFile( fileId, data, token );
        });
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
        return super.reTry( () -> {
            return clt_deleteFile( fileId, token );
        });
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        return super.reTry( () -> {
            return clt_getFile( fileId, token );
        });
    }

    private Result<Void> clt_writeFile( String fileId, byte[] data, String token) {

        Response r = target.path(fileId)
        		.queryParam("token", token)
                .request()
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(Result.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;

    }

    private Result<Void> clt_deleteFile( String fileId, String token) {

        Response r = target.path(fileId)
        			.queryParam("token", token)
                	.request()
                	.delete();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(Result.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;

    }

    private Result<byte[]> clt_getFile( String fileId, String token) {

        Response r = target.path(fileId)
        		.queryParam("token", token)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(Result.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }

}

