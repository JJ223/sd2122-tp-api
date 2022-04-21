package tp1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;

import java.net.URI;
import java.util.List;

public class RestDirectoryClient extends Client implements Directory {

    final WebTarget target;

    public RestDirectoryClient( URI serverURI ) {
        super( serverURI );
        target = client.target( serverURI ).path( RestDirectory.PATH );
    }

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
    	return super.reTry( () -> {
            return clt_writeFile(filename, data, userId, password);
        });
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        return super.reTry( () -> {
            return clt_deleteFile( filename, userId, password );
        });
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry( () -> {
            return clt_shareFile( filename, userId, userIdShare, password );
        });
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry( () -> {
            return clt_unshareFile( filename, userId, userIdShare, password );
        });
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
    	return super.reTry( () -> {
            return clt_getFile( filename, userId, accUserId, password);
        });
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
    	return super.reTry( () -> {
            return clt_listFiles( userId, password);
        });
    }

    @Override
    public Result<Void> deleteUser(String userId, String password) {
        return super.reTry( () -> {
             return clt_deleteUser( userId, password);
        });

    }

    private Result<Void> clt_deleteUser(String userId, String password){
        Response r = target.path(userId)
                .queryParam(RestUsers.PASSWORD, password)
                .request()
                .delete();

        if( r.getStatus() == Response.Status.NO_CONTENT.getStatusCode() )
            return Result.ok();
        return getResultError(Response.Status.fromStatusCode(r.getStatus()));

    }

    private Result<FileInfo> clt_writeFile( String filename, byte[] data, String userId, String password) {

        Response r = target.path(String.format("%s.%s", userId, filename))
        		.queryParam(RestUsers.PASSWORD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(FileInfo.class));
        else
            return getResultError(Response.Status.fromStatusCode(r.getStatus()));
    }
    
    private Result<Void> clt_deleteFile(String filename, String userId, String password) {

        Response r = target.path(String.format("%s.%s", userId, filename))
        		 .queryParam(RestUsers.PASSWORD, password)
                 .request()
                 .delete();


        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok();
        return getResultError(Response.Status.fromStatusCode(r.getStatus()));

    }
    
    private Result<Void> clt_shareFile(String filename, String userId, String userIdShare, String password) {
    	Response r = target.path(String.format("%s.%s/share/%s", userId, filename, userIdShare))
       		 	.queryParam(RestUsers.PASSWORD, password)
                .request()
                .post(Entity.entity(String.class,  MediaType.APPLICATION_JSON));

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok();
        return getResultError(Response.Status.fromStatusCode(r.getStatus()));
    }
    
    private Result<Void> clt_unshareFile(String filename, String userId, String userIdShare, String password) {
    	Response r = target.path(String.format("%s.%s/share/%s", userId, filename, userIdShare))
       		 	.queryParam(RestUsers.PASSWORD, password)
                .request()
                .delete();


        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok();
        return getResultError(Response.Status.fromStatusCode(r.getStatus()));
    }
    
    private Result<byte[]> clt_getFile(String filename, String userId, String accUserId, String password) {

        Response r = target.path(String.format("%s.%s", userId, filename))
        		 .queryParam(RestUsers.USER_ID, accUserId)
        		 .queryParam(RestUsers.PASSWORD, password)
                 .request()
                 .accept(MediaType.APPLICATION_OCTET_STREAM)
                 .get();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
            return Result.ok(r.readEntity(new GenericType<byte[]>() {}));
        else
            return getResultError(Response.Status.fromStatusCode(r.getStatus()));
    }
    
    private Result<List<FileInfo>> clt_listFiles(String userId, String password) {
    	Response r = target.path(String.format("%s", userId))
       		 .queryParam(RestUsers.PASSWORD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

       if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
           return Result.ok(r.readEntity(new GenericType<List<FileInfo>>() {}));
       else
           return getResultError(Response.Status.fromStatusCode(r.getStatus()));
    }
}
