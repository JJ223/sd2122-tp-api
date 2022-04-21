package tp1.server.resources.soap;

import tp1.api.FileInfo;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.FilesException;
import tp1.api.service.soap.SoapDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.server.resources.JavaDirectory;
import tp1.server.resources.JavaFiles;
import tp1.server.rest.Discovery;

import java.util.List;

public class SoapDirectoryWebService implements SoapDirectory {

    final Directory impl;

    public SoapDirectoryWebService( Discovery d){
        d.listener();
        impl = new JavaDirectory(d);
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) throws DirectoryException {
        var result = impl.writeFile( filename, data, userId, password );
        if( result.isOK() )
            return result.value();
        else
            throw new DirectoryException(result.error().toString());
    }

    @Override
    public void deleteFile(String filename, String userId, String password) throws DirectoryException {
        var result = impl.deleteFile( filename, userId, password );
        if( !result.isOK() )
            throw new DirectoryException(result.error().toString());
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
        var result = impl.shareFile( filename, userId, userIdShare, password );
        if( !result.isOK() )
            throw new DirectoryException(result.error().toString());
    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException {
        var result = impl.shareFile( filename, userId, userIdShare, password );
        if( !result.isOK() )
            throw new DirectoryException(result.error().toString());
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) throws DirectoryException {
        var result = impl.getFile( filename, userId, accUserId, password );
        if( result.isOK() )
            return result.value();
        else
            throw new DirectoryException(result.error().toString());
    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) throws DirectoryException {
        var result = impl.lsFile( userId, password );
        if( result.isOK() )
            return result.value();
        else
            throw new DirectoryException(result.error().toString());
    }

    @Override
    public void deleteUser(String userId, String password) throws DirectoryException {
        var result = impl.lsFile( userId, password );
        if( !result.isOK() )
            throw new DirectoryException(result.error().toString());
    }
}
