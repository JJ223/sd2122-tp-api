package tp1.server.resources;

import java.util.*;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import tp1.api.User;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.server.Discovery;

@Singleton
public class UsersResource implements Users {

	private final Map<String,User> users = new HashMap<>();

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	public UsersResource(Discovery d) {
		d.listener();
	}
		
	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || 
				user.getEmail() == null) {
			Log.info("User object invalid.");
			Result.error(Result.ErrorCode.BAD_REQUEST );
		}
		
		// Check if userId already exists
		if( users.containsKey(user.getUserId())) {
			Log.info("User already exists.");
			Result.error(Result.ErrorCode.CONFLICT );
		}

		//Add the user to the map of users
		users.put(user.getUserId(), user);
		return Result.ok(user.getUserId());
	}


	@Override
	public Result<User> getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);
		
		User user = users.get(userId);
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			Result.error(Result.ErrorCode.FORBIDDEN );
		}

		// Check if user is valid
		if(userId == null || password == null) {
			Log.info("UserId or passwrod null.");
			Result.error(Result.ErrorCode.BAD_REQUEST );
		}
		
		return Result.ok(user);
	}


	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

		if(user == null){
			Log.info("User is null.");
			return Result.error(Result.ErrorCode.BAD_REQUEST);
		}

		Result<User> result = getUser(userId, password);

		if(!result.isOK())
			return result;
		
		User user2 = result.value();
		
		String email = user.getEmail();
		if(email != null)
			user2.setEmail(email);

		String pass = user.getPassword();
		if(pass != null)
			user2.setPassword(pass);

		String fullName = user.getFullName();
		if(fullName != null)
			user2.setFullName(fullName);

		return result;

	}


	@Override
	public Result<User> deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);
		
		Result<User> result = getUser(userId, password);
		
		if(!result.isOK())
			return result;
		
		users.remove(userId);
		return result;
	}


	@Override
	public Result<List<User>> searchUsers(String pattern) {

		Log.info("searchUsers : pattern = " + pattern);

		if(pattern == null){
			Log.info("Pattern is null.");
			Result.error(Result.ErrorCode.BAD_REQUEST );
		}

		List<User> list = new ArrayList<User>();
		users.values().stream().forEach( u -> { 
			if( u.getFullName().toUpperCase().indexOf(pattern.toUpperCase()) != -1) list.add( new User(u.getUserId(),u.getFullName(),u.getEmail(),"")); 
			});
		
		
		
		return Result.ok(list);
	}

}
