import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
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
    private TupleQuery query;
    private TupleQueryResult queryResult;
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
    public RepositoryConnection getConnection(){
        return repCon;
    }
    public TupleQueryResult runQuery(String queryCode){
        query = repCon.prepareTupleQuery(QueryLanguage.SPARQL,queryCode);
        queryResult=query.evaluate();
        return queryResult;
    }
    public void printQueryResult(){
        while(queryResult.hasNext()){
            System.out.println(queryResult.next().getValue(String.valueOf(queryResult.next().getBindingNames().toArray()[0])));
            System.out.println(queryResult.next().getValue(String.valueOf(queryResult.next().getBindingNames().toArray()[1])));
            System.out.println(queryResult.next().getValue(String.valueOf(queryResult.next().getBindingNames().toArray()[2])));
        }
    }
    public void finishConnection(){
        repCon.close();
    }
    public static void main(String args[]){
        



    }
}
