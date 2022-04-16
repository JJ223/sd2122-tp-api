package tp1.clients;

import jakarta.ws.rs.client.WebTarget;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.rest.RestUsers;

import java.net.URI;

public class RestFileClient extends RestClient implements RestFiles {

    final WebTarget target;

    RestFileClient( URI serverURI ) {
        super( serverURI );
        target = client.target( serverURI ).path( RestFiles.PATH );
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) {

    }

    @Override
    public void deleteFile(String fileId, String token) {

    }

    @Override
    public byte[] getFile(String fileId, String token) {
        return new byte[0];
    }
}
