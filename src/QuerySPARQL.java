
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


public class QuerySPARQL {
    private static Logger logger =
            LoggerFactory.getLogger(QuerySPARQL.class);

    public static void main(String args[]){
        Repository repo = new HTTPRepository("http://192.168.1.102:7200/","18");
        repo.init();
        RepositoryConnection con = repo.getConnection();

        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, "select * where{?name ?b ?c.}limit 10");
        TupleQueryResult result = null;

        result = query.evaluate();
        while (result.hasNext()){
            BindingSet binding = result.next();

            Value name = binding.getValue("name");
            logger.trace("name  = " + name.stringValue());
            System.out.print(name.stringValue());
            con.close();
        }
    }

}
