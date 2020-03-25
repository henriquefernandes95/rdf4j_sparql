
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.FileInputStream;
import java.io.InputStream;


public class QuerySPARQL {
    private static Logger logger =
            LoggerFactory.getLogger(QuerySPARQL.class);

    public static void main(String args[]){
        Repository repo = new HTTPRepository("http://192.168.1.102:7200/","22");
        repo.init();
        RepositoryConnection con = repo.getConnection();
        InputStream is = new FileInputStream("dbpedia.txt");

        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, "");
        TupleQueryResult result = null;

        result = query.evaluate();
        
        while (result.hasNext()){
            BindingSet binding = result.next();

            Value subject = binding.getValue("subject");
            Value predicative = binding.getValue("predicative");
            Value object = binding.getValue("object");
            Value classeConceito = binding.getValue("ClasseConceito");
            //logger.trace("name  = " + name.stringValue());
            System.out.println("********");
            System.out.println(classeConceito.stringValue());
            //System.out.println(predicative.stringValue());
            //System.out.println(object.stringValue());
            System.out.println("********");

        }
        con.close();
    }

}
