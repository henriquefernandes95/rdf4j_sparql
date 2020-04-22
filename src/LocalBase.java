import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.io.IOException;

import static org.eclipse.rdf4j.rio.RDFFormat.RDFXML;

public class LocalBase {
    public void LocalBase(){

    }
    public static void main(String args[]){
        
        Repository localRepo = new SailRepository(new MemoryStore());
        RepositoryConnection repCon = localRepo.getConnection();
        try {
            repCon.add(new File("metadata_from_portal.rdf"),"", RDFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(repCon.size());
    }
}
