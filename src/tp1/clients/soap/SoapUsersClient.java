package tp1.clients.soap;

import com.sun.xml.ws.client.BindingProviderProperties;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import tp1.api.User;
import tp1.api.service.soap.SoapFiles;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.rest.Client;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class SoapUsersClient extends SoapClient implements Users {

    private SoapUsers users;

    public SoapUsersClient(URI serverURI) {
        super( serverURI );
        QName qname = new QName(SoapUsers.NAMESPACE, SoapUsers.NAME);
        Service service = Service.create( makeURI(serverURI), qname);
        this.users = service.getPort(SoapUsers.class);

        ((BindingProvider) users).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        ((BindingProvider) users).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, READ_TIMEOUT);
    }

    private URL makeURI(URI serverURI){
        try {
            return URI.create(serverURI + "?wsdl").toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public Result<String> createUser(User user) {
        return super.reTry( () -> {
            try {
                return Result.ok(users.createUser(user));
            } catch (UsersException e) {
                //System.out.println(Response.Status.valueOf(e.getMessage()).name());
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        return super.reTry( () -> {
            try {
                return Result.ok(users.getUser(userId, password));
            } catch (UsersException e) {
                //System.out.println(Response.Status.valueOf(e.getMessage()).name());
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        return super.reTry( () -> {
            try {
                return Result.ok(users.updateUser(userId, password, user));
            } catch (UsersException e) {
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        return super.reTry( () -> {
            try {
                return Result.ok(users.deleteUser(userId, password));
            } catch (UsersException e) {
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return super.reTry( () -> {
            try {
                return Result.ok(users.searchUsers(pattern));
            } catch (UsersException e) {
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }

    @Override
    public Result<Boolean> userExists(String userId) {
        return super.reTry( () -> {
            try {
                return Result.ok(users.userExists(userId));
            } catch (UsersException e) {
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }

}
