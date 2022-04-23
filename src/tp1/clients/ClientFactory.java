package tp1.clients;

import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Users;
import tp1.clients.rest.RestDirectoryClient;
import tp1.clients.rest.RestFileClient;
import tp1.clients.rest.RestUsersClient;
import tp1.clients.soap.SoapDirectoryClient;
import tp1.clients.soap.SoapFileClient;
import tp1.clients.soap.SoapUsersClient;
import java.net.URI;

public class ClientFactory {

    private static final String REST = "rest";

    public static Users getUsersClient( URI serverURI ) {
        if( serverURI.toString().endsWith(REST))
            return new RestUsersClient( serverURI );
        else {
            return new SoapUsersClient( serverURI );
        }
    }

    public static Files getFilesClient(URI serverURI ) {
        if( serverURI.toString().endsWith(REST))
            return new RestFileClient( serverURI );
        else {
            return new SoapFileClient( serverURI );
        }
    }

    public static Directory getDirectoryClient(URI serverURI ) {
        if( serverURI.toString().endsWith(REST))
            return new RestDirectoryClient( serverURI );
        else {
            return new SoapDirectoryClient( serverURI );
        }
    }
}
