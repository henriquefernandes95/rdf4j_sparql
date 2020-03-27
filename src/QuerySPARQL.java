
import jdk.internal.util.xml.impl.Input;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ontotext.graphdb.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


import java.io.*;
import java.util.Iterator;


public class QuerySPARQL<iterator> {
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







        //cria repositório

        InputStream config = null;
        RDFParser rdfParser=null;
        TreeModel graph = new TreeModel();
        RepositoryManager manager = RepositoryProvider.getRepositoryManager("http://192.168.1.102:7200");
        Model model = graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);
        Iterator<Statement> iter = model.iterator();
        Statement statement = iter.next();
        Resource repositoryNode =  statement.getSubject();
        manager.init();
        try {

            config = new FileInputStream("repo-default.ttl");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(graph));
        try {
            rdfParser.parse(config, String.valueOf(RepositoryConfig.create(model,repositoryNode)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            config.close();
        } catch (IOException e) {
            e.printStackTrace();
        }




        RepositoryConfig repositoryConfig = RepositoryConfig.create(graph,repositoryNode);
        manager.addRepositoryConfig(repositoryConfig);










    }










    // Instantiate a repository graph model


//    // Read repository configuration file
//    InputStream config;
//
//    {
//        try {
//            config = new FileInputStream(new File("repo-defaults.ttl"));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//




}
