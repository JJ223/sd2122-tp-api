package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Result;
import tp1.clients.RestFileClient;
import tp1.clients.RestUsersClient;
import tp1.server.Discovery;
import tp1.server.UsersServer;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class DirectoryResource extends ServerResource implements RestDirectory {

    private static Logger Log = Logger.getLogger(DirectoryResource.class.getName());
    private Discovery d;
    private Map<String, List<FileInfo>> directory;

    public DirectoryResource(Discovery d) {
        this.d = d;
        directory = new HashMap<>();
        d.listener();
    }

    @Override
    public FileInfo writeFile(String filename, byte[] data, String userId, String password) {

        //user server
        URI[] userURI = d.knownUrisOf("users");
        RestUsersClient users = new RestUsersClient(userURI[0]);

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
        	getErrorException(res.error());

        String filedId = String.format("%s.%s", userId, filename);

        URI fileServerURI = null;
        FileInfo f;

        if(!directory.containsKey(userId)){
            directory.put(userId, new LinkedList<FileInfo>());
        }else{
            f = getFileInfoUser(userId, filename);
            if(f != null) {
                fileServerURI = URI.create(f.getFileURL().replace("/files/" + filedId, ""));
                RestFileClient files = new RestFileClient(fileServerURI);
                files.writeFile(filedId, data, "");

                return f;
            }
        }

        //choose file server
        URI[] fileURI = d.knownUrisOf("files");
        Random r = new Random();
        int result = r.nextInt(fileURI.length);
        fileServerURI = fileURI[result];

        String fileUrl = String.format("%s%s/%s.%s", fileURI[result], RestFiles.PATH, userId, filename);

        //Create newFileInfo
        f = new FileInfo(userId, filename, fileUrl, new HashSet<String>());
        directory.get(userId).add(f);

        //add file to file server        
        RestFileClient files = new RestFileClient(fileServerURI);
        files.writeFile(filedId, data, "");

        return f;


    }

    @Override
    public void deleteFile(String filename, String userId, String password) {
    	URI[] userURI = d.knownUrisOf("users");
        RestUsersClient users = new RestUsersClient(userURI[0]);

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
        	getErrorException(res.error());
        
        String filedId = String.format("%s.%s", userId, filename);
                
        if(!directory.containsKey(userId)) {
            Log.info("User with userid does not have files.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
        	FileInfo fileInfo = getFileInfoUser( userId, filename);
        	if(fileInfo!=null) {
        		URI fileServerURI = URI.create(fileInfo.getFileURL().replace("/files/" + filedId, ""));
        		RestFileClient files = new RestFileClient(fileServerURI);
        		files.deleteFile(filedId, "");
                List<FileInfo> l = directory.get(userId);
                l.remove(fileInfo);
        	} else {
        		Log.info("File does not exist.");
        		throw new WebApplicationException(Response.Status.NOT_FOUND);
        	}		
        }
    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {
        URI[] userURI = d.knownUrisOf(UsersServer.SERVICE);
        RestUsersClient users = new RestUsersClient(userURI[0]);

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            getErrorException(res.error());

        Result<Boolean> res2 = users.userExists(userIdShare);
        if(!res2.isOK())
            getErrorException(res2.error());

        FileInfo fI = getFileInfoUser(userId, filename);
        if(fI == null) {
            Log.info("File does not exist.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if(!sharedFile(fI,userIdShare)) {
            Set<String> shared = fI.getSharedWith();
            shared.add(userIdShare);
            fI.setSharedWith(shared);
        }

    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {

        URI[] userURI = d.knownUrisOf(UsersServer.SERVICE);
        RestUsersClient users = new RestUsersClient(userURI[0]);

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            getErrorException(res.error());

        Result<Boolean> res2 = users.userExists(userIdShare);
        if(!res2.isOK())
            getErrorException(res2.error());

        FileInfo fI = getFileInfoUser(userId, filename);
        if(fI == null) {
            Log.info("File does not exist.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if(sharedFile(fI,userIdShare)) {
            //TODO fazer um metodo para isto
            Set<String> shared = fI.getSharedWith();
            shared.remove(userIdShare);
        }
    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {

        //user server
        URI[] userURI = d.knownUrisOf(UsersServer.SERVICE);
        RestUsersClient users = new RestUsersClient(userURI[0]);
        
        Result<User> res = users.getUser(accUserId, password);
        if(!res.isOK())
        	getErrorException(res.error());

        Result<Boolean> res2 = users.userExists(userId);
        if(!res2.isOK())
            getErrorException(res2.error());
        
        if(!directory.containsKey(userId)) {
            Log.info("User with userid does not have files.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        FileInfo fI = getFileInfoUser(userId, filename);
        if(fI == null){
            Log.info("File does not exist.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if(!accUserId.equals(fI.getOwner()) && !sharedFile(fI,accUserId)){
            Log.info("User does not have access to file.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        throw new WebApplicationException(Response.temporaryRedirect(URI.create(fI.getFileURL())).build());


    }

    @Override
    public List<FileInfo> lsFile(String userId, String password) {
        return null;
    }

    private FileInfo getFileInfoUser(String userId, String filename){
        List<FileInfo> files = directory.get(userId);

        for ( FileInfo file : files) {
            if( file.getFilename().equals(filename))
                return file;
        }

        return null;
    }

    private boolean sharedFile(FileInfo fI, String accUserId){
        Set<String> sharedWith = fI.getSharedWith();
        return sharedWith.contains(accUserId);
    }

}
