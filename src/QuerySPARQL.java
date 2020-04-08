
import com.ontotext.trree.util.convert.storage.PrettyPrinter;
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
import org.eclipse.rdf4j.repository.util.RDFInserter;
import org.eclipse.rdf4j.repository.util.RDFLoader;
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
import sun.nio.cs.UTF_32;


import java.io.*;
import java.util.Iterator;


public class QuerySPARQL<iterator> {
    private static Logger logger = LoggerFactory.getLogger(QuerySPARQL.class);
    static TupleQuery query;
    static File saida = new File("saida.txt");
    static String httpRepo = new String("http://192.168.1.102:7200/");
    static String repoID = new String("D3");
    static RepositoryConnection con;
    static BindingSet binding;
    private QuerySPARQL(){

    }


    public static void main(String args[]) {
        //inicializa o repositório




        String results = new String();
        String[] queries, values, classes;
        Boolean primeira=true;
        int count=0;
        int current = 1;
        int numQueries = 0;
        Iterator inter;



        //Carrega as consultas do arquivo

        String fileAsString = dadosQuery();
        queries=fileAsString.split("#QUERY");//separador das consultas
        numQueries=queries.length;


        while(current < numQueries) {
            count=0;
            primeira=true;
            System.out.println("\n\n********\n\nComeçou\n\n********\n\n");
            iniciaRepo();
            //Realiza a consulta
            query = con.prepareTupleQuery(QueryLanguage.SPARQL, queries[current]);
            TupleQueryResult result = null;

            result = query.evaluate();
            values = new String[result.getBindingNames().size()];
            classes = new String[result.getBindingNames().size()];



            if (primeira) {

                while (count < result.getBindingNames().size()) {
                    System.out.println(result.getBindingNames().get(0));
                    values[count] = String.valueOf(result.getBindingNames().get(count));
                    results += values[count] + "\t";
                    count++;
                }
                results += "\n";
                primeira = false;

            }

            System.out.println(result.getBindingNames());
            while (result.hasNext()) {
                binding = result.next();
                System.out.println(binding.size());

                count = 0;
                while (count < binding.size()) {
                    System.out.println(binding.getValue(values[count]).toString());
                    classes[count] = binding.getValue(values[count]).toString();
                    count++;
                }


                for (String clas : classes) {
                    results += clas + "\t";
                }
                results += "\n";
                /*
                try {
                    FileUtils.writeStringToFile(saida,results);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/


            }
            con.close();
            escreveArquivo(results);


            //cria repositório

            InputStream config = null;
            RDFParser rdfParser = null;
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


            carregaDados(repoCon);




            //Encerrar a conexão
            repoCon.close();
            repository.shutDown();
            manager.shutDown();


            //        RepositoryConfig repositoryConfig = RepositoryConfig.create(graph,repositoryNode);
            //        manager.addRepositoryConfig(repositoryConfig);
            //

            current++;
        }



    }

    private static int iniciaRepo(){
        Repository repo = new HTTPRepository(httpRepo, repoID);
        repo.init();
        con = repo.getConnection();
        return 0;
    }

    private static String dadosQuery(){//Obtém os dados de quey do arquivo
        InputStream stream = null;
        String line=null;
        BufferedReader buf = null;
        //Carrega as consultas do arquivo
        try {
            stream = new FileInputStream(new File("queries.txt"));
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
        return sb.toString();
    }

    private static int escreveArquivo(String cont){
        try{
            FileUtils.writeStringToFile(saida,cont);
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    private static int carregaDados(RepositoryConnection repoCon){
        RDFInserter inserter = new RDFInserter(repoCon);
        RDFLoader loader = new RDFLoader(repoCon.getParserConfig(),repoCon.getValueFactory())


        repoCon.begin();
        loader.load(new File("metadata_from_portal.rdf"),RDFFormat.NTRIPLES,inserter);
        return 0;
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
