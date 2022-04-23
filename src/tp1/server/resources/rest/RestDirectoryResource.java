package tp1.server.resources.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.server.resources.JavaDirectory;
import tp1.server.rest.Discovery;

import java.util.*;
import java.util.logging.Logger;

public class RestDirectoryResource implements RestDirectory {

    private Discovery d;

    private static Logger Log = Logger.getLogger(RestDirectoryResource.class.getName());

    final Directory impl;

    public RestDirectoryResource(Discovery d) {
        this.d = d;
        d.listener();
        impl = new JavaDirectory(d);
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {
        var result = impl.writeFile( filename, data, userId, password );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getErrorException(result.error()));

    }

    @Override
    public void deleteFile(String filename, String userId, String password) {
        var result = impl.deleteFile( filename, userId, password );
        if( !result.isOK() )
            throw new WebApplicationException(getErrorException(result.error()));
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        var result = impl.shareFile( filename, userId, userIdShare, password );
        if( !result.isOK() )
            throw new WebApplicationException(getErrorException(result.error()));
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {
        var result = impl.unshareFile( filename, userId, userIdShare, password );
        if( !result.isOK() )
            throw new WebApplicationException(getErrorException(result.error()));
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {
        var result = impl.getFile( filename, userId, accUserId, password );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getErrorException(result.error()));
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        var result = impl.lsFile( userId, password );
        if( result.isOK() )
            return result.value();
        else
            throw new WebApplicationException(getErrorException(result.error()));
    }

    @Override
    public void deleteUserFiles(String userId, String password) {
        var result = impl.deleteUserFiles( userId, password );
        if( !result.isOK() )
            throw new WebApplicationException(getErrorException(result.error()));
    }

    private Response.Status getErrorException(Result.ErrorCode err) throws WebApplicationException{
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
