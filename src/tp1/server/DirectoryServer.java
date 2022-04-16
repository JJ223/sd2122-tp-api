package tp1.server;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tp1.server.resources.DirectoryResource;
import tp1.server.resources.FilesResource;
import util.Debug;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DirectoryServer {

    private static Logger Log = Logger.getLogger(DirectoryServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "directory";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";
    private static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);

    public static void main(String[] args) {
        try {
            Debug.setLogLevel( Level.INFO, Debug.SD2122 );
            ResourceConfig config = new ResourceConfig();

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);

            Discovery d = new Discovery(DISCOVERY_ADDR, SERVICE, serverURI);
            d.start();

            config.register( new DirectoryResource(d) );
            //config.register(CustomLoggingFilter.class);
            //config.register(GenericExceptionMapper.class);

            JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));

            //More code can be executed here...
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }
}