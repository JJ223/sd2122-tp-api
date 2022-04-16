package tp1.server;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>A class to perform service discovery, based on periodic service contact endpoint
 * announcements over multicast communication.</p>
 *
 * <p>Servers announce their *name* and contact *uri* at regular intervals. The server actively
 * collects received announcements.</p>
 *
 * <p>Service announcements have the following format:</p>
 *
 * <p>&lt;service-name-string&gt;&lt;delimiter-char&gt;&lt;service-uri-string&gt;</p>
 */
public class Discovery {
    private static Logger Log = Logger.getLogger(Discovery.class.getName());

    static {
        // addresses some multicast issues on some TCP/IP stacks
        System.setProperty("java.net.preferIPv4Stack", "true");
        // summarizes the logging format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
    }


    // The pre-aggreed multicast endpoint assigned to perform discovery.
    static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
    static final int DISCOVERY_PERIOD = 1000;
    static final int DISCOVERY_TIMEOUT = 5000;

    // Used separate the two fields that make up a service announcement.
    private static final String DELIMITER = "\t";

    private Map<String, ArrayList<String>> services;

    private InetSocketAddress addr;
    private String serviceName;
    private String serviceURI;

    /**
     * @param  serviceName the name of the service to announce
     * @param  serviceURI an uri string - representing the contact endpoint of the service being announced
     */
    Discovery( InetSocketAddress addr, String serviceName, String serviceURI) {
        this.addr = addr;
        this.serviceName = serviceName;
        this.serviceURI  = serviceURI;
        services = new HashMap<String, ArrayList<String>>();
    }

    /**
     * Continuously announces a service given its name and uri
     *
     * @param serviceName the composite service name: <domain:service>
     * @param serviceURI - the uri of the service
     */
    public void announce(String serviceName, String serviceURI) {
        Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s\n", DISCOVERY_ADDR, serviceName, serviceURI));

        var pktBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();

        DatagramPacket pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);
        // start thread to send periodic announcements
        new Thread(() -> {
            try (var ds = new DatagramSocket()) {
                for (;;) {
                    try {
                        Thread.sleep(DISCOVERY_PERIOD);
                        ds.send(pkt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Listens for the given composite service name, blocks until a minimum number of replies is collected.
     * @return the discovery results as an array
     */

    public void listener() {
        Log.info(String.format("Starting discovery on multicast group: %s, port: %d\n", DISCOVERY_ADDR.getAddress(), DISCOVERY_ADDR.getPort()));

        final int MAX_DATAGRAM_SIZE = 65536;
        var pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);

        new Thread(() -> {
            try (var ms = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
                joinGroupInAllInterfaces(ms);
                for(;;) {
                    try {
                        pkt.setLength(MAX_DATAGRAM_SIZE);
                        ms.receive(pkt);

                        var msg = new String(pkt.getData(), 0, pkt.getLength());
                        //System.out.printf( "FROM %s (%s) : %s\n", pkt.getAddress().getCanonicalHostName(), pkt.getAddress().getHostAddress(), msg);
                        var tokens = msg.split(DELIMITER);

                        if (tokens.length == 2) {
                            ArrayList<String> s = services.get(tokens[0]);
                            if(s == null){
                                s = new ArrayList<String>();
                                services.put(tokens[0], s);
                            }
                            s.add(tokens[1]);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(DISCOVERY_PERIOD);
                        } catch (InterruptedException e1) {
                            // do nothing
                        }
                        Log.finest("Still listening...");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Returns the known servers for a service.
     *
     * @param  serviceName the name of the service being discovered
     * @return an array of URI with the service instances discovered.
     *
     */
    public URI[] knownUrisOf(String serviceName) {
        ArrayList<String> a = services.get(serviceName);
        if(a == null) return null;

        URI[] u = new URI[a.size()];
        int i = 0;
        for (String s : a) {
            try {
                u[i++] = new URI(s);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return u;
    }

    private void joinGroupInAllInterfaces(MulticastSocket ms) throws SocketException {
        Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
        while (ifs.hasMoreElements()) {
            NetworkInterface xface = ifs.nextElement();
            try {
                ms.joinGroup(DISCOVERY_ADDR, xface);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    /**
     * Starts sending service announcements at regular intervals...
     */
    public void start() {
        announce(serviceName, serviceURI);
        listener();
    }

    // Main just for testing purposes
    public static void main( String[] args) throws Exception {
        Discovery discovery = new Discovery( DISCOVERY_ADDR, "test", "http://" + InetAddress.getLocalHost().getHostAddress());
        discovery.start();
    }
}
