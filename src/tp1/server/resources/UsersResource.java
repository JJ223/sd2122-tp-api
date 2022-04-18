package tp1.server.resources;

import java.util.*;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.server.Discovery;

@Singleton
public class UsersResource implements RestUsers {

	private final Map<String,User> users = new HashMap<>();

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	public UsersResource(Discovery d) {
		d.listener();
	}
		
	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || 
				user.getEmail() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
		
		// Check if userId already exists
		if( users.containsKey(user.getUserId())) {
			Log.info("User already exists.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		//Add the user to the map of users
		users.put(user.getUserId(), user);
		return user.getUserId();
	}


	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);
		
		User user = users.get(userId);
		System.out.println("Password GIven: "+password+"Real Password: "+user.getPassword());
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		// Check if user is valid
		if(userId == null || password == null) {
			Log.info("UserId or passwrod null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
		
		return user;
	}


	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

		if(user == null){
			Log.info("User is null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		User user2 = getUser(userId, password);

		String email = user.getEmail();
		if(email != null)
			user2.setEmail(email);

		String pass = user.getPassword();
		if(pass != null)
			user2.setPassword(pass);

		String fullName = user.getFullName();
		if(fullName != null)
			user2.setFullName(fullName);

		return user2;

	}


	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);
		User user = getUser(userId, password);
		users.remove(userId);
		return user;
	}


	@Override
	public List<User> searchUsers(String pattern) {

		Log.info("searchUsers : pattern = " + pattern);

		if(pattern == null){
			Log.info("Pattern is null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		List<User> result = new ArrayList<User>();
		users.values().stream().forEach( u -> { 
			if( u.getFullName().toUpperCase().indexOf(pattern.toUpperCase()) != -1) result.add( new User(u.getUserId(),u.getFullName(),u.getEmail(),"")); 
			});
		return result;
	}

}
