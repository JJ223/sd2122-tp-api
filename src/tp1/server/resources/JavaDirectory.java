package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.ClientFactory;
import tp1.server.resources.rest.Entry;
import tp1.server.resources.rest.ServerCapacityManager;
import tp1.server.resources.rest.RestServerResource;
import tp1.server.rest.Discovery;
import tp1.server.soap.SoapDirectoryServer;
import tp1.server.soap.SoapFilesServer;
import tp1.server.soap.SoapUsersServer;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class JavaDirectory extends RestServerResource implements Directory {

    private static Logger Log = Logger.getLogger(JavaDirectory.class.getName());
    private Discovery d;
    private Map<String, List<FileInfo>> directory;
    private ServerCapacityManager sv;
    private Users users;
    
    private final String FILEID_FORMAT = "%s.%s";
    private final String FILE_URL_BUILD = RestFiles.PATH+"/%s";
    private final String FILE_URL = "%s%s/"+FILEID_FORMAT;
    private final String REST = "rest";

    public JavaDirectory(Discovery d) {
        this.d = d;
        directory = new ConcurrentHashMap<>();
        sv = new ServerCapacityManager(SoapFilesServer.SERVICE_NAME, d);
        users = null;
    }

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        //user server
        if(users == null) {
            URI[] userURI = d.knownUrisOf(SoapUsersServer.SERVICE_NAME);
            users = ClientFactory.getUsersClient(userURI[0]);
        }
        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            return Result.error(res.error());

        String fileId = String.format(FILEID_FORMAT, userId, filename);

        FileInfo f;

        if(!directory.containsKey(userId)){
            directory.put(userId, new LinkedList<FileInfo>());
        }else{
            f = getFileInfoUser(userId, filename);
            if(f != null) {
                URI fileServerURI = URI.create(f.getFileURL().replace(String.format(FILE_URL_BUILD, fileId), ""));
                Files files = ClientFactory.getFilesClient(fileServerURI);
                files.writeFile(fileId, data, "");
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
            fileUrl = String.format(FILE_URL, uri.toString(), RestFiles.PATH, userId, filename);

            try {
                Files files = ClientFactory.getFilesClient(uri);
                files.writeFile(fileId, data, "");
                sv.updateCapacity(uri, 1);
            } catch (Exception e) {
                if (fileURIs.hasNext()) {
                    uri = fileURIs.next().getURI();
                    Files files = ClientFactory.getFilesClient(uri);
                    files.writeFile(fileId, data, "");
                    sv.updateCapacity(uri, 1);
                }
            }
        }

        //Create newFileInfo
        Set<String> set = new HashSet<String>();
        f = new FileInfo(userId, filename, fileUrl, set);
        directory.get(userId).add(f);

        return Result.ok(f);
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        if(users == null) {
            URI[] userURI = d.knownUrisOf(SoapUsersServer.SERVICE_NAME);
            users = ClientFactory.getUsersClient(userURI[0]);
        }

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK()) {
            return Result.error(res.error());
        }

        String fileId = String.format(FILEID_FORMAT, userId, filename);

        if(!directory.containsKey(userId)) {
            Log.info("User with userid does not have files.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else {
            FileInfo fileInfo = getFileInfoUser( userId, filename);
            if(fileInfo!=null) {

                URI fileServerURI = URI.create(fileInfo.getFileURL().replace(String.format(FILE_URL_BUILD, fileId), ""));
                Files files = ClientFactory.getFilesClient(fileServerURI);
                files.deleteFile(fileId, "");
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
        if(users == null) {
            URI[] userURI = d.knownUrisOf(SoapUsersServer.SERVICE_NAME);
            users = ClientFactory.getUsersClient(userURI[0]);
        }

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            return Result.error(res.error());

        Result<Boolean> res2 = users.userExists(userIdShare);
        if(!res2.isOK())
            return Result.error(res2.error());

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

        if(users == null) {
            URI[] userURI = d.knownUrisOf(SoapUsersServer.SERVICE_NAME);
            users = ClientFactory.getUsersClient(userURI[0]);
        }

        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            return Result.error(res.error());

        Result<Boolean> res2 = users.userExists(userIdShare);
        if(!res2.isOK())
            return Result.error(res2.error());

        FileInfo fI = getFileInfoUser(userId, filename);
        if(fI == null) {
            Log.info("File does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        if(sharedFile(fI,userIdShare)) {
            Set<String> shared = fI.getSharedWith();
            shared.remove(userIdShare);
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {

        if(users == null) {
            URI[] userURI = d.knownUrisOf(SoapUsersServer.SERVICE_NAME);
            users = ClientFactory.getUsersClient(userURI[0]);
        }

        Result<User> res = users.getUser(accUserId, password);
        if(!res.isOK())
            return Result.error(res.error());

        Result<Boolean> res2 = users.userExists(userId);
        if(!res2.isOK())
            return Result.error(res2.error());

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

        URI[] dir = d.knownUrisOf(SoapDirectoryServer.SERVICE_NAME);
        if( dir[0].toString().contains(REST))
            throw new WebApplicationException(Response.temporaryRedirect(URI.create(fI.getFileURL())).build());
        else {
            String fileId = String.format(FILEID_FORMAT, userId, filename);
            String uri = fI.getFileURL().replace(String.format(FILE_URL_BUILD, fileId), "");
            Files files = ClientFactory.getFilesClient(URI.create(uri));
            return files.getFile(fileId, "");
        }


    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {

        if(users == null) {
            URI[] userURI = d.knownUrisOf(SoapUsersServer.SERVICE_NAME);
            users = ClientFactory.getUsersClient(userURI[0]);
        }


        Result<User> res = users.getUser(userId, password);
        if(!res.isOK())
            return Result.error(res.error());

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
    public Result<Void> deleteUserFiles(String userId, String password) {

        List<FileInfo> userFiles = directory.get(userId);
        if(userFiles != null){
            for(FileInfo f : userFiles){
            	String fileId = String.format(FILEID_FORMAT, f.getOwner(), f.getFilename());
                URI fileServerURI = URI.create(f.getFileURL().replace(String.format(FILE_URL_BUILD, fileId), ""));
                Files files = ClientFactory.getFilesClient(fileServerURI);
                files.deleteFile(fileId, "");

            }
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
