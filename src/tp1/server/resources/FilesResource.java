package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.rest.RestFiles;
import tp1.server.Discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class FilesResource implements RestFiles {

    private Map<String, byte[]> files = new HashMap<>();

    private static Logger Log = Logger.getLogger(FilesResource.class.getName());

    public FilesResource( Discovery d) {
        d.listener();
    }

    @Override
    public void writeFile(String fileId, byte[] data, String token) {
        Log.info("writeFile : " + fileId);

        // Check if file data is valid
        if(fileId == null || data == null) {
            Log.info("File invalid.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        files.put(fileId, data);
        printFiles();
    }

    @Override
    public void deleteFile(String fileId, String token) {
        Log.info("deleteFile : " + fileId);

        // Check if file data is valid
        if(fileId == null ){
            Log.info("FileId invalid.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        byte[] file = files.get(fileId);
        if(file == null){
            Log.info("File does not exist.");
            throw new WebApplicationException( Response.Status.NOT_FOUND );
        }

        files.remove(fileId);

    }

    @Override
    public byte[] getFile(String fileId, String token) {
        Log.info("getFile : " + fileId);

        // Check if file data is valid
        if(fileId == null ){
            Log.info("FileId invalid.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        byte[] file = files.get(fileId);
        if(file == null){
            Log.info("File does not exist.");
            throw new WebApplicationException( Response.Status.NOT_FOUND );
        }

        return file;
    }

    private void printFiles(){
        System.out.println("PRINTING FILESSS");
        Set<String> fileIds = files.keySet();
        for(String s: fileIds){
            System.out.println(s);
        }
    }
}
