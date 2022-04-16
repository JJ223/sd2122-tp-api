package tp1.clients;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;

import java.net.URI;
import java.util.List;

public class RestDirectoryClient extends RestClient implements RestDirectory {

    final WebTarget target;

    public RestDirectoryClient( URI serverURI ) {
        super( serverURI );
        target = client.target( serverURI ).path( RestDirectory.PATH );
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
    	return super.reTry( () -> {
            return clt_writeFile(filename, data, userId, password);
        });
    }

    @Override
    public void deleteFile(String filename, String userId, String password) {
    	super.reTry( () -> {
            clt_deleteFile( filename, userId, password );
            return null;
        });
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
    	super.reTry( () -> {
            clt_shareFile( filename, userId, userIdShare, password );
            return null;
        });
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
    	super.reTry( () -> {
            clt_unshareFile( filename, userId, userIdShare, password );
            return null;
        });
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
    	return super.reTry( () -> {
            return clt_getFile( filename, userId, accUserId, password);
        });
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
    	return super.reTry( () -> {
            return clt_listFiles( userId, password);
        });
    }
    
    private FileInfo clt_writeFile( String filename, byte[] data, String userId, String password) {
    	
        Response r = target.path(String.format("%s/%s", userId, filename))
        		.queryParam(RestUsers.PASSWORD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(FileInfo.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }
    
    private void clt_deleteFile(String filename, String userId, String password) {

        Response r = target.path(String.format("%s/%s", userId, filename))
        		 .queryParam(RestUsers.PASSWORD, password)
                 .request()
                 .delete();

        if( r.getStatus() != Response.Status.OK.getStatusCode() || !r.hasEntity() )
            System.out.println("Error, HTTP error status: " + r.getStatus() );

    }
    
    private void clt_shareFile(String filename, String userId, String userIdShare, String password) {
    	Response r = target.path(String.format("%s/%s/share/%s", userId, filename, userIdShare))
       		 	.queryParam(RestUsers.PASSWORD, password)
                .request()
                .post(Entity.entity(String.class,  MediaType.APPLICATION_JSON)); 
    		//TODO o que meter no post? no restDirectory este metodo nao consome nada...

       if( r.getStatus() != Response.Status.OK.getStatusCode() || !r.hasEntity() )
           System.out.println("Error, HTTP error status: " + r.getStatus() );
    }
    
    private void clt_unshareFile(String filename, String userId, String userIdShare, String password) {
    	Response r = target.path(String.format("%s/%s/share/%s", userId, filename, userIdShare))
       		 	.queryParam(RestUsers.PASSWORD, password)
                .request()
                .delete();
                

       if( r.getStatus() != Response.Status.OK.getStatusCode() || !r.hasEntity() )
           System.out.println("Error, HTTP error status: " + r.getStatus() );
    }
    
    private byte[] clt_getFile(String filename, String userId, String accUserId, String password) {

        Response r = target.path(String.format("%s/%s", userId, filename))
        		 .queryParam(RestUsers.USER_ID, accUserId)
        		 .queryParam(RestUsers.PASSWORD, password)
                 .request()
                 .accept(MediaType.APPLICATION_OCTET_STREAM)
                 .get();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return r.readEntity(new GenericType<byte[]>() {});
        else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return null;
    }
    
    private List<FileInfo> clt_listFiles(String userId, String password) {
    	Response r = target.path(String.format("%s", userId))
       		 .queryParam(RestUsers.PASSWORD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

       if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
           return r.readEntity(new GenericType<List<FileInfo>>() {});
       else
           System.out.println("Error, HTTP error status: " + r.getStatus() );

       return null;
    }
}
