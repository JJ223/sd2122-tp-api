package tp1.clients;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.rest.RestUsers;

import java.net.URI;
import java.util.List;

public class RestFileClient extends RestClient implements RestFiles {

    final WebTarget target;

    public RestFileClient(URI serverURI) {
        super( serverURI );
        target = client.target( serverURI ).path( RestFiles.PATH );
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) {
        super.reTry( () -> {
            clt_writeFile( fileId, data, token );
            return null;
        });

    }

    @Override
    public void deleteFile(String fileId, String token) {
        super.reTry( () -> {
            clt_deleteFile( fileId, token );
            return null;
        });
    }

    @Override
    public byte[] getFile(String fileId, String token) {
        return super.reTry( () -> {
            return clt_getFile( fileId, token );
        });
    }

    private void clt_writeFile( String fileId, byte[] data, String token) {

        Response r = target.path(fileId)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        if( r.getStatus() != Response.Status.OK.getStatusCode() || !r.hasEntity() )
            System.out.println("Error, HTTP error status: " + r.getStatus() );

    }

    private void clt_deleteFile( String fileId, String token) {

        Response r = target.path(fileId)
                .request()
                .delete();

        if( r.getStatus() != Response.Status.OK.getStatusCode() || !r.hasEntity() )
            System.out.println("Error, HTTP error status: " + r.getStatus() );

    }

    private byte[] clt_getFile( String fileId, String token) {

        Response r = target.path(fileId)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(new GenericType<byte[]>() {});
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }

}

