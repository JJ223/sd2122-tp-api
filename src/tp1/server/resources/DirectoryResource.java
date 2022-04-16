package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.rest.RestFiles;
import tp1.clients.RestFileClient;
import tp1.clients.RestUsersClient;
import tp1.server.Discovery;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

public class DirectoryResource implements RestDirectory {

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
        User user = users.getUser(userId, password);

        //choose file server
        URI[] fileURI = d.knownUrisOf("files");
        Random r = new Random();
        int result = r.nextInt(fileURI.length);

        String fileUrl = String.format("%s%s%s/%s/%s", fileURI[result], "/rest", RestFiles.PATH, userId, filename);

        //Create newFileInfo
        FileInfo newFile = new FileInfo(userId, filename, fileUrl, new HashSet<String>());
        if(!directory.containsKey(userId))
            directory.put(userId, new ArrayList<FileInfo>());

        directory.get(userId).add(newFile);

        //add file to file server
        RestFileClient files = new RestFileClient(fileURI[result]);

        String filedId = String.format("%s/%s", userId, filename);
        files.writeFile(filedId, data, "");

        return newFile;


    }

    @Override
    public void deleteFile(String filename, String userId, String password) {

    }

    @Override
    public void shareFile(String filename, String userId, String userIdShare, String password) {

    }

    @Override
    public void unshareFile(String filename, String userId, String userIdShare, String password) {

    }

    @Override
    public byte[] getFile(String filename, String userId, String accUserId, String password) {

        //user server
        URI[] userURI = d.knownUrisOf("users");
        RestUsersClient users = new RestUsersClient(userURI[0]);
        User user = users.getUser(accUserId, password);

        if(!directory.containsKey(userId)) {
            Log.info("User with userid does not have files.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        FileInfo fI = getFileInfoUser(userId, password);
        if(fI == null){
            Log.info("File does not exist.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        if( userId != accUserId && !sharedFile(fI,accUserId)){
            Log.info("User does not have access to file.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        //get File from file server
        try {
            RestFileClient files = new RestFileClient(new URI(fI.getFileURL()));
            String filedId = String.format("%s/%s", userId, filename);
            Log.info("URL: "+ fI.getFileURL()+" FILEID: "+filedId);
            return files.getFile(filedId, "");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
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
