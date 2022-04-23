package tp1.clients.soap;

import com.sun.xml.ws.client.BindingProviderProperties;
import jakarta.ws.rs.core.Response;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import tp1.api.service.soap.FilesException;
import tp1.api.service.soap.SoapFiles;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.clients.rest.Client;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class SoapFileClient extends SoapClient implements Files {

    private SoapFiles files;

    public SoapFileClient(URI serverURI){
        super( serverURI );
        QName qname = new QName(SoapFiles.NAMESPACE, SoapFiles.NAME);
        Service service = Service.create( makeURI(serverURI), qname);
        this.files = service.getPort(SoapFiles.class);

        ((BindingProvider) files).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        ((BindingProvider) files).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, READ_TIMEOUT);
    }

    private URL makeURI(URI serverURI){
        try {
            return URI.create(serverURI + WSDL).toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        return super.reTry( () -> {
            try {
                files.writeFile(fileId, data, token);
                return Result.ok();
            } catch (FilesException e) {
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
        return super.reTry( () -> {
            try {
                files.deleteFile(fileId, token);
                return Result.ok();
            } catch (FilesException e) {
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        return super.reTry( () -> {
            try {
                return Result.ok(files.getFile(fileId, token));
            } catch (FilesException e) {
                return getResultError(Response.Status.valueOf(e.getMessage()));
            }
        });
    }
}
