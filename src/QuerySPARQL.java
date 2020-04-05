
import jdk.internal.util.xml.impl.Input;
import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.*;
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
        Repository repo = new HTTPRepository("http://192.168.1.102:7200/", "D3");
        repo.init();
        RepositoryConnection con = repo.getConnection();
        InputStream stream = null;
        String line=null;
        BufferedReader buf = null;
        PrintWriter file = null;
        String results = new String();
        String[] queries, values, classes;
        Boolean primeira=true;
        int count=0;
        int size = 0;
        Iterator inter;

        Value classeConceito1 = null;//variavel sera utilizada em diferentes escopos do codigo
        Value classeConceito2 = null;

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

        queries=fileAsString.split("#QUERY");//separador das consultas

        System.out.println(queries.length);
        //Realiza a consulta
        TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, queries[2]);
        TupleQueryResult result = null;

        result = query.evaluate();
        values = new String[result.getBindingNames().size()];
        classes = new String[result.getBindingNames().size()];
        while (result.hasNext()){
            BindingSet binding = result.next();

            if(primeira){
                inter=binding.iterator();



                while(count < result.getBindingNames().size()){
                    System.out.println(result.getBindingNames().get(0));
                    values[count]= String.valueOf(result.getBindingNames().get(count));
                    //System.out.println(values[count]);
                    results+= values[count]+"\t";
                    count++;
                }
                results+="\n";
                //System.out.println(results);
                primeira=false;

            }
            count=0;
            while(count < binding.size()){
                System.out.println(binding.getValue(values[count]).toString());
                classes[count]=binding.getValue(values[count]).toString();
                count++;
            }



            for (String clas : classes){
                results += clas+"\t";
            }
            results+= "\n";

            try {
                FileUtils.writeStringToFile(new File("saida.txt"),results);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //System.out.println(predicative.stringValue());
            //System.out.println(object.stringValue());


        }
        con.close();





        //cria repositório

        InputStream config = null;
        RDFParser rdfParser=null;
        TreeModel graph = new TreeModel();
        Model model = graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY);



        RepositoryManager manager = RepositoryProvider.getRepositoryManager("http://192.168.1.102:7200");
        manager.init();
        manager.getAllRepositories();

        try {

            config = new FileInputStream(new File("repo-defaults.ttl"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        rdfParser = Rio.createParser(RDFFormat.TURTLE);
        rdfParser.setRDFHandler(new StatementCollector(graph));

        try {
            rdfParser.parse(config, RepositoryConfigSchema.NAMESPACE);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            config.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//



        //Obtendo o repositório como recurso
        Resource repoNode = Models.subject(graph.filter(null, RDF.TYPE, RepositoryConfigSchema.REPOSITORY)).orElseThrow(() -> new RuntimeException("Oops, no <http://www.openrdf.org/config/repository#> subject found!"));


        //Adicionando as configurações
        RepositoryConfig configObj = RepositoryConfig.create(graph, repoNode);
        manager.addRepositoryConfig(configObj);


        //Obter o repositorio criado
        Repository repository = manager.getRepository("graphdb-repo");

        //Conectar ao repositorio
        RepositoryConnection repoCon = repository.getConnection();


//        //Carregar dados
//        repoCon.begin();
//        Update updateOp = repoCon.prepareUpdate(QueryLanguage.SPARQL, classeConceito.stringValue());
//        updateOp.execute();

        //Encerrar a conexão
        repoCon.close();
        repository.shutDown();
        manager.shutDown();



//        RepositoryConfig repositoryConfig = RepositoryConfig.create(graph,repositoryNode);
//        manager.addRepositoryConfig(repositoryConfig);
//









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
