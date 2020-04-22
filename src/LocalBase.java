import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;

import static org.eclipse.rdf4j.rio.RDFFormat.RDFXML;

public class LocalBase {
    private static Repository localRepo;
    private static RepositoryConnection repCon;
    public LocalBase(){
        this.localRepo = new SailRepository(new MemoryStore());
        this.repCon = localRepo.getConnection();
        this.loadRDFData();
    }
    public int loadRDFData(){
        try {
            repCon.add(new File("metadata_from_portal.rdf"),"", RDFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static void main(String args[]){
        


        try {
            repCon.add(new File("metadata_from_portal.rdf"),"", RDFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(repCon.size());
    }
}
