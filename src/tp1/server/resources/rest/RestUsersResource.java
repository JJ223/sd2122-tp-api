package tp1.server.resources.rest;

import java.util.*;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Users;
import tp1.server.resources.JavaUsers;
import tp1.server.rest.Discovery;

@Singleton
public class RestUsersResource extends RestServerResource implements RestUsers {

	final Users impl;
	
	public RestUsersResource(Discovery d) {
		d.listener();
		impl = new JavaUsers(d);
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