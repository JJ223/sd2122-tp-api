package tp1.server.resources.rest;

import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.server.resources.JavaFiles;
import tp1.server.rest.Discovery;

public class RestFilesResource extends RestServerResource implements RestFiles {

    final Files impl = new JavaFiles();

    public RestFilesResource(Discovery d) {
        d.listener();
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) {
        var result = impl.writeFile( fileId, data, token );
        if( !result.isOK() )
            getErrorException(result.error()) ;
    }

    @Override
    public void deleteFile(String fileId, String token) {
        var result = impl.deleteFile( fileId, token );
        if( !result.isOK() )
            getErrorException(result.error()) ;

    }

    @Override
    public byte[] getFile(String fileId, String token) {
        var result = impl.getFile( fileId, token );
        if( result.isOK() )
            return result.value();
        else
            getErrorException(result.error()) ;
        return null;
    }
}
