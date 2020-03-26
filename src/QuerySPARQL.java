
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class QuerySPARQL {
    private static Logger logger =
            LoggerFactory.getLogger(QuerySPARQL.class);

    public static void main(String args[]) {
        //inicializa o repositório
        Repository repo = new HTTPRepository("http://192.168.1.102:7200/", "22");
        repo.init();
        RepositoryConnection con = repo.getConnection();
        InputStream stream = null;
        String line=null;
        BufferedReader buf = null;

        //Carrega as consultas do arquivo
        try {
            stream = new FileInputStream(new File("dbpedia.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        buf = new BufferedReader(new InputStreamReader(stream));
        try {
            line = buf.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Constroi a string com o conteúdo do arquivo
        StringBuilder sb = new StringBuilder();


        while(line != null){
            sb.append(line).append("\n");
            try {
                line = buf.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String fileAsString = sb.toString();
        //System.out.println("Contents : " + fileAsString);


        //Realiza a consulta
        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, fileAsString);
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
