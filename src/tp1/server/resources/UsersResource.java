package tp1.server.resources;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.RestDirectoryClient;
import tp1.clients.RestUsersClient;
import tp1.server.Discovery;

@Singleton
public class UsersResource extends ServerResource implements RestUsers {

	final Users impl = new JavaUsers();

	private Discovery d;

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());

	public UsersResource(Discovery d) {
		this.d = d;
		d.listener();
	}

	@Override
	public String createUser(User user) {
		var result = impl.createUser( user );
		if( result.isOK() )
			return result.value();
        else
			getErrorException(result.error()) ;
		return null;
	}

	@Override
	public User getUser(String userId, String password) {
		var result = impl.getUser( userId, password );
		if( result.isOK() )
			return result.value();
		else
			getErrorException(result.error()) ;
		return null;
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		var result = impl.updateUser( userId, password, user );
		if( result.isOK() )
			return result.value();
		else
			getErrorException(result.error()) ;
		return null;
	}

	@Override
	public User deleteUser(String userId, String password) {
		var result = impl.deleteUser( userId, password);
		if( result.isOK() ) {
			URI[] directoryURI = d.knownUrisOf("directory");
			RestDirectoryClient directory = new RestDirectoryClient(directoryURI[0]);
			directory.deleteUser(userId,password);
			return result.value();
		}else
			getErrorException(result.error()) ;
		return null;
	}

	@Override
	public List<User> searchUsers(String pattern) {
		var result = impl.searchUsers( pattern );
		if( result.isOK() )
			return result.value();
		else
			getErrorException(result.error()) ;
		return null;
	}

	@Override
	public boolean userExists(String userId) {
		var result = impl.userExists( userId );
		if( result.isOK() )
			return result.value();
		else
			getErrorException(result.error()) ;
		return false;
	}
}