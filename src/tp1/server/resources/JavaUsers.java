package tp1.server.resources;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import tp1.api.User;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.ClientFactory;
import tp1.server.rest.Discovery;
import tp1.server.soap.SoapDirectoryServer;

public class JavaUsers implements Users{

    private final Map<String,User> users = new ConcurrentHashMap<>();

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private Discovery d;

    public JavaUsers( Discovery d) {
        this.d = d;
    }

    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null ||
                user.getEmail() == null) {
            Log.info("User object invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST );
        }

        // Check if userId already exists
        if( users.containsKey(user.getUserId())) {
            Log.info("User already exists.");
            return Result.error(Result.ErrorCode.CONFLICT );
        }

        //Add the user to the map of users
        users.put(user.getUserId(), user);
        return Result.ok(user.getUserId());
    }


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
            return Result.error(Result.ErrorCode.FORBIDDEN );
        }

        // Check if user is valid
        if(userId == null || password == null) {
            Log.info("UserId or password null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST );
        }

        return Result.ok(user);
    }


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


    public Result<User> deleteUser(String userId, String password) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + password);

        Result<User> result = getUser(userId, password);

        if(!result.isOK())
            return result;

        users.remove(userId);

        URI[] directoryURI = d.knownUrisOf(SoapDirectoryServer.SERVICE_NAME);
        Directory directory = ClientFactory.getDirectoryClient(directoryURI[0]);
        Result<Void> r = directory.deleteUserFiles(userId, password);

        if (!r.isOK())
            return Result.error(r.error());

        return result;
    }


    public Result<List<User>> searchUsers(String pattern) {

        Log.info("searchUsers : pattern = " + pattern);

        if(pattern == null){
            Log.info("Pattern is null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST );
        }

        List<User> list = new ArrayList<User>();
        users.values().stream().forEach( u -> {
            if( u.getFullName().toUpperCase().indexOf(pattern.toUpperCase()) != -1) list.add( new User(u.getUserId(),u.getFullName(),u.getEmail(),""));
        });

        return Result.ok(list);
    }

    public Result<Boolean> userExists(String userId) {
        if(users.containsKey(userId))
            return Result.ok(true);
        return Result.error(Result.ErrorCode.NOT_FOUND);
    }

}
