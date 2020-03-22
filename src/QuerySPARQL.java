
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
        Repository repo = new HTTPRepository("http://192.168.1.102:7200/","agro");
        repo.init();
        RepositoryConnection con = repo.getConnection();

        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, "#Obtém as classes da DBpedia Ontology utilizadas por objetos.\n" +
                "PREFIX lodbr: <http://lodbr.ufrj.br/agrotoxicos/propriedade/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "select distinct ?Classe where { \n" +
                "    service <http://192.168.9.1:7200/repositories/Teste1> {\n" +
                "        ?s ?p ?o .\n" +
                "        bind(iri(replace(str(?o),\"page\",\"resource\"))as ?vObj)         \n" +
                "    }\n" +
                "    service <http://dbpedia.org/sparql> {\n" +
                "       \t?vObj rdf:type ?Classe .\n" +
                "        ?Classe rdf:type owl:Class .\n" +
                "    }\n" +
                "} ");
        TupleQueryResult result = null;

        result = query.evaluate();
        while (result.hasNext()){
            BindingSet binding = result.next();

            Value name = binding.getValue("name");
            logger.trace("name  = " + name.stringValue());
            con.close();
        }
    }

}
