package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.clients.RestFileClient;
import tp1.clients.RestUsersClient;
import tp1.server.Discovery;
import tp1.server.UsersServer;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class JavaDirectory extends ServerResource implements Directory {

    private static Logger Log = Logger.getLogger(JavaDirectory.class.getName());
    private Discovery d;
    private Map<String, List<FileInfo>> directory;
    private ServerCapacityManager sv;

    public JavaDirectory(Discovery d) {

        this.d = d;
        directory = new ConcurrentHashMap<>();
        this.d.listener();
        sv = new ServerCapacityManager("files", d);
    }

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        //user server
        URI[] userURI = d.knownUrisOf("users");
        RestUsersClient users = new RestUsersClient(userURI[0]);

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            getErrorException(res.error());

        String filedId = String.format("%s.%s", userId, filename);

        FileInfo f;

        if(!directory.containsKey(userId)){
            directory.put(userId, new LinkedList<FileInfo>());
        }else{
            f = getFileInfoUser(userId, filename);
            if(f != null) {
                URI fileServerURI = URI.create(f.getFileURL().replace("/files/" + filedId, ""));
                RestFileClient files = new RestFileClient(fileServerURI);
                files.writeFile(filedId, data, "");
                synchronized (sv) {
                    sv.updateCapacity(fileServerURI, 1);
                }
                return Result.ok(f);
            }
        }

        String fileUrl;
        //choose file server
        synchronized (sv) {
        Iterator<Entry<URI, Integer>> fileURIs = sv.getServers();
        URI uri = fileURIs.next().getURI();
        fileUrl = String.format("%s%s/%s.%s", uri.toString(), RestFiles.PATH, userId, filename);


            //add file to file server
            try {
                RestFileClient files = new RestFileClient(uri);
                files.writeFile(filedId, data, "");
                sv.updateCapacity(uri, 1);
            } catch (Exception e) {
                if (fileURIs.hasNext()) {
                    uri = fileURIs.next().getURI();
                    RestFileClient files = new RestFileClient(uri);
                    files.writeFile(filedId, data, "");
                    sv.updateCapacity(uri, 1);
                }
            }
        }

        //Create newFileInfo
        f = new FileInfo(userId, filename, fileUrl, new HashSet<String>());
        directory.get(userId).add(f);

        return Result.ok(f);
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        URI[] userURI = d.knownUrisOf("users");
        RestUsersClient users = new RestUsersClient(userURI[0]);

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            getErrorException(res.error());

        String filedId = String.format("%s.%s", userId, filename);

        if(!directory.containsKey(userId)) {
            Log.info("User with userid does not have files.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else {
            FileInfo fileInfo = getFileInfoUser( userId, filename);
            if(fileInfo!=null) {

                URI fileServerURI = URI.create(fileInfo.getFileURL().replace("/files/" + filedId, ""));
                RestFileClient files = new RestFileClient(fileServerURI);
                files.deleteFile(filedId, "");
                List<FileInfo> l = directory.get(userId);
                l.remove(fileInfo);
                synchronized (sv) {
                    sv.updateCapacity(fileServerURI, -1);
                }

            } else {
                Log.info("File does not exist.");
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
        }
        return Result.ok();
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
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
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        if(!sharedFile(fI,userIdShare)) {
            Set<String> shared = fI.getSharedWith();
            shared.add(userIdShare);
            fI.setSharedWith(shared);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {

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
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        if(sharedFile(fI,userIdShare)) {
            //TODO fazer um metodo para isto
            Set<String> shared = fI.getSharedWith();
            shared.remove(userIdShare);
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {

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
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        FileInfo fI = getFileInfoUser(userId, filename);
        if(fI == null){
            Log.info("File does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        if(!accUserId.equals(fI.getOwner()) && !sharedFile(fI,accUserId)){
            Log.info("User does not have access to file.");
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }

        throw new WebApplicationException(Response.temporaryRedirect(URI.create(fI.getFileURL())).build());


    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {

        URI[] userURI = d.knownUrisOf(UsersServer.SERVICE);
        RestUsersClient users = new RestUsersClient(userURI[0]);


        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            getErrorException(res.error());

        List<FileInfo> sharedFiles = new LinkedList<FileInfo>();

        for(List<FileInfo> userFiles : directory.values()) {
            for(FileInfo fileInfo : userFiles) {
                if(sharedFile(fileInfo, userId))
                    sharedFiles.add(fileInfo);
            }
        }
        if(directory.containsKey(userId))
            sharedFiles.addAll(directory.get(userId));

        return Result.ok(sharedFiles);
    }

    @Override
    public Result<Void> deleteUser(String userId, String password) {
        //TODO encontrar melho solucao para lidar com os ficheiros partilhados

        List<FileInfo> userFiles = directory.get(userId);
        for(FileInfo f : userFiles){
            //TODO make this tring pretty
            URI fileServerURI = URI.create(f.getFileURL().replace("/files/" + f.getOwner()+ "."+f.getFilename(), ""));
            RestFileClient files = new RestFileClient(fileServerURI);
            files.deleteFile(f.getOwner()+ "."+f.getFilename(), "");

        }

        directory.remove(userId);

        for(List<FileInfo> files : directory.values()) {
            for(FileInfo fileInfo : files) {
                if(sharedFile(fileInfo, userId)){
                    Set<String> sharedWith = fileInfo.getSharedWith();
                    sharedWith.remove(userId);
                }
            }
        }
        return Result.ok();
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