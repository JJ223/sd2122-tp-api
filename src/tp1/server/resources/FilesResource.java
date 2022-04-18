package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.server.Discovery;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Logger;

public class FilesResource extends ServerResource implements RestFiles {

    private static Logger Log = Logger.getLogger(FilesResource.class.getName());

    final Files impl = new JavaFiles();

    public FilesResource( Discovery d) {
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
