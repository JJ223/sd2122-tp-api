package tp1.server.resources.rest;

import tp1.server.resources.rest.Entry;
import tp1.server.rest.Discovery;

import java.net.URI;
import java.util.*;

public class ServerCapacityManager {

    private String serviceName;
    private Discovery d;
    private List<Entry<URI,Integer>> servers;

    public ServerCapacityManager(String serviceName, Discovery d){
        this.serviceName = serviceName;
        this.d = d;
        servers = Collections.synchronizedList(new ArrayList<Entry<URI,Integer>>());
        d.listener();
        updateUris();
    }

    public Iterator<Entry<URI,Integer>> getServers(){
        updateUris();
        Collections.sort(servers);
        return servers.iterator();
    }

    public void updateCapacity(URI uri, int num){
        for( Entry e : servers){
            if(e.getURI().equals(uri))
                e.updateNumFiles(num);
        }
    }

    private void updateUris(){
        URI[] uris = d.knownUrisOf(serviceName);
        if(uris != null) {
            for (int i = 0; i < uris.length; i++) {
                if (!contains(uris[i])) {
                    Entry<URI, Integer> entry = new Entry<URI, Integer>(uris[i], 0);
                    servers.add(entry);
                }
            }
        }
    }

    private boolean contains(URI uri){
        for( Entry<URI,Integer> e : servers){
            if(e.getURI().equals(uri))
                return true;
        }
        return false;
    }


}
