package tp1.server.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class JavaFiles implements Files {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        Log.info("writeFile : " + fileId);
        System.out.println("JAVA FILES");

        // Check if file data is valid
        if(fileId == null || data == null) {
            Log.info("File invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST );
        }

        try {
            File outputFile = new File(fileId);
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                outputStream.write(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
        Log.info("deleteFile : " + fileId);

        // Check if file data is valid
        if(fileId == null ){
            Log.info("FileId invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        File file = new File(fileId);

        if(file.exists()){
            Log.info("File does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND );
        }

        file.delete();
        return Result.ok();
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        Log.info("getFile : " + fileId);

        // Check if file data is valid
        if(fileId == null ){
            Log.info("FileId invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        byte[] file;
        try {
            file = java.nio.file.Files.readAllBytes(Paths.get(fileId));

            if(file == null){
                Log.info("File does not exist.");
                return Result.error(Result.ErrorCode.NOT_FOUND );
            }

            return Result.ok(file);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
