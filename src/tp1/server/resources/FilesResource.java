package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.rest.RestFiles;
import tp1.server.Discovery;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Logger;

public class FilesResource implements RestFiles {

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
        
		try {
            File outputFile = new File(fileId);
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(data);
            }

		} catch (IOException e) {
			e.printStackTrace();
		}
        

    }

    @Override
    public void deleteFile(String fileId, String token) {
        Log.info("deleteFile : " + fileId);

        // Check if file data is valid
        if(fileId == null ){
            Log.info("FileId invalid.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        File file = new File(fileId);
        
        if(file.exists()){
            Log.info("File does not exist.");
            throw new WebApplicationException( Response.Status.NOT_FOUND );
        }
        
        file.delete();

    }

    @Override
    public byte[] getFile(String fileId, String token) {
        Log.info("getFile : " + fileId);

        // Check if file data is valid
        if(fileId == null ){
            Log.info("FileId invalid.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        byte[] file;
		try {
			file = Files.readAllBytes(Paths.get(fileId));
			
			if(file == null){
	            Log.info("File does not exist.");
	            throw new WebApplicationException( Response.Status.NOT_FOUND );
	        }

	        return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return null;
    }
}
