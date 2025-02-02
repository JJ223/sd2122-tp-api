package tp1.server.resources;

import tp1.api.service.util.Files;
import tp1.api.service.util.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class JavaFiles implements Files {

    private static Logger Log = Logger.getLogger(JavaFiles.class.getName());

    public JavaFiles() {
    }

    @Override
    public synchronized Result<Void> writeFile(String fileId, byte[] data, String token) {
        Log.info("writeFile : " + fileId);

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
    public synchronized Result<Void> deleteFile(String fileId, String token) {
        Log.info("deleteFile : " + fileId);

        // Check if file data is valid
        if(fileId == null ){
            Log.info("FileId invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        try {
            java.nio.file.Files.delete(Paths.get(fileId));
        }catch (IOException e) {
            Log.info("File does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND );
        }

        return Result.ok();
    }

    @Override
    public synchronized Result<byte[]> getFile(String fileId, String token) {
        Log.info("getFile : " + fileId);

        // Check if file data is valid
        if(fileId == null ){
            Log.info("FileId invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        byte[] file;
        try {
            file = java.nio.file.Files.readAllBytes(Paths.get(fileId));
            return Result.ok(file);

        } catch (IOException e) {
            Log.info("File does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

    }
}
