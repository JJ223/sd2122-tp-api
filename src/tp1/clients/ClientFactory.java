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

import java.net.MalformedURLException;
import java.net.URI;

public class ClientFactory {

    public static Users getUsersClient( URI serverURI ) {
        if( serverURI.toString().endsWith("rest"))
            return new RestUsersClient( serverURI );
        else {
            try {
                return new SoapUsersClient( serverURI );
            } catch (MalformedURLException e) {
                System.out.println("URI ERROR");
                return null;
            }
        }
    }

    public static Files getFilesClient(URI serverURI ) {
        if( serverURI.toString().endsWith("rest"))
            return new RestFileClient( serverURI );
        else {
            try {
                return new SoapFileClient( serverURI );
            } catch (MalformedURLException e) {
                return null;
            }
        }
    }

    public static Directory getDirectoryClient(URI serverURI ) {
        if( serverURI.toString().endsWith("rest"))
            return new RestDirectoryClient( serverURI );
        else {
            try {
                return new SoapDirectoryClient( serverURI );
            } catch (MalformedURLException e) {
                return null;
            }
        }
    }
}
