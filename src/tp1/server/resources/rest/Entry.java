package tp1.server.resources.rest;

public class Entry<URI, Integer> implements Comparable<Entry<URI, Integer>>{

    private URI uri;
    private int numFiles;

    public Entry( URI uri, int numFiles){
        this.uri = uri;
        this.numFiles = numFiles;
    }

    public int getNumFiles(){ return numFiles;}

    public URI getURI(){ return uri;}

    public void updateNumFiles(int i){
        numFiles = numFiles + i;
    }

    @Override
    public int compareTo(Entry<URI, Integer> o) {
        if(o == null)
            return -1;

        if(uri.equals(o.getURI()))
            return 0;

        return numFiles-o.getNumFiles();
    }
}
