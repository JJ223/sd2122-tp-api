package tp1.server.soap;

import jakarta.xml.ws.Endpoint;
import tp1.server.resources.soap.SoapDirectoryWebService;
import tp1.server.resources.soap.SoapUsersWebService;
import tp1.server.rest.Discovery;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapDirectoryServer {

    public static final int PORT = 8080;
    public static final String SERVICE_NAME = "directory";
    public static String SERVER_BASE_URI = "http://%s:%s/soap";
    private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("227.227.227.227", 2277);

    private static Logger Log = Logger.getLogger(SoapDirectoryServer.class.getName());

    public static void main(String[] args) throws Exception {

        /*
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        */
        Log.setLevel(Level.INFO);

        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

        Discovery d = new Discovery(DISCOVERY_ADDR, SERVICE_NAME, serverURI);
        d.start();
        Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), new SoapDirectoryWebService(d));

        Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));
    }

}
