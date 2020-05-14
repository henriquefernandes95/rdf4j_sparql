import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigSchema;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static org.eclipse.rdf4j.rio.RDFFormat.RDFXML;


public class QuerySPARQL<iterator> {
    private static Logger logger = LoggerFactory.getLogger(QuerySPARQL.class);
    static TupleQuery query;
    static File saida = new File("saida.txt");
    static String httpRepo = new String("http://192.168.1.102:7200/");
    static String repoID = new String("P1");
    static RepositoryConnection con;
    static BindingSet binding;
    static LocalBase locB;//Base local na memória
    static ArrayList<String> graphAdds = new ArrayList<String>();
    static ArrayList<String> extraQueries = new ArrayList<String>();
    static int numGraphs;
    static int currGraph;
    static int numExtQueires=0;

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
        int startQueryNumber;
        Iterator inter;



        //Carrega as consultas do arquivo

        String fileAsString = dadosQuery();
        queries=fileAsString.split("#QUERY");//separador das consultas
        numQueries=queries.length;
        startQueryNumber=numQueries;
        locB = new LocalBase();
        currGraph=0;
        numGraphs=0;
        while(current < numQueries) {
            count=0;
            primeira=true;
            System.out.println("\n\n********\n\nComeçou\n\n********\n\n");
            iniciaRepo();
            //Realiza a consulta

            while(current>1&&currGraph<numGraphs){

                if(!(current >1)) {
                    queries[current] = queries[current].replaceAll("#graphToQuery", graphAdds.get(currGraph) + "/");
                }
                else{
                    extraQueries.add(queries[current].replaceAll("#graphToQuery", graphAdds.get(currGraph) + "/"));
                    //numQueries++;

                }
                System.out.println(queries[current]);
                System.out.println("\n ***"+current+"***** \n");
                currGraph++;

            }
            if(current==2){
                numQueries=(startQueryNumber-2)*(numGraphs)+1;
            }


            if(current <=1){
                query = con.prepareTupleQuery(QueryLanguage.SPARQL, queries[current]);
            }
            if(current>1&&current<6){
                query = con.prepareTupleQuery(QueryLanguage.SPARQL, extraQueries.get(current-2).toString());
                System.out.println("\n\n EXECUÇÂO \n\n");
            }
            //currGraph=0;
            TupleQueryResult result = null;

            result = query.evaluate();
            values = new String[result.getBindingNames().size()];
            classes = new String[result.getBindingNames().size()];

            ModelBuilder modBuilder = new ModelBuilder();
            ValueFactory factory = null;
            ValueFactory factoryLoc = null;

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
            factory= repoCon.getValueFactory();//inicializa o value factory correspondente ao respositório criado
            factoryLoc=locB.getConnection().getValueFactory();


            //Fim dos processos de criação do repositório

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
            while (result.hasNext()) {//Avança por cada linha dos resultados
                binding = result.next();
                System.out.println(binding.size());

                count = 0;
                while (count < binding.size()) {
                    System.out.println(binding.getValue(values[count]).toString());
                    classes[count] = binding.getValue(values[count]).toString();
                    count++;
                }

                //modBuilder.subject(binding.getValue("DS").stringValue())
                //Criando as triplas. Teste. Restringindo a cada caso
                if(binding.hasBinding("DS")&&binding.hasBinding("nomeOrg")) {
                    repoCon.add(factory.createIRI(binding.getValue("nomeOrg").stringValue()), RDF.TYPE, factory.createIRI(binding.getValue("publicador").stringValue()));
                    repoCon.add(factory.createIRI(binding.getValue("DS").stringValue()), RDF.TYPE, DCAT.DATASET);
                    repoCon.add(factory.createIRI(binding.getValue("DS").stringValue()), DC.PUBLISHER, factory.createIRI(binding.getValue("nomeOrg").stringValue()));
                    repoCon.add(factory.createIRI(binding.getValue("DS").stringValue()), factory.createIRI("http://purl.org/dc/terms/references"), factory.createIRI(binding.getValue("nomeOrg").stringValue()));
                    graphAdds.add(String.valueOf(binding.getValue("DS")));
                    numGraphs++;
                    System.out.println("\nNUM GRAPHS"+numGraphs);
                }
                if(binding.hasBinding("DS")&&binding.hasBinding("nomeOrg")) {
                    locB.getConnection().add(factoryLoc.createIRI(binding.getValue("nomeOrg").stringValue()), RDF.TYPE, factory.createIRI(binding.getValue("publicador").stringValue()));
                    locB.getConnection().add(factoryLoc.createIRI(binding.getValue("DS").stringValue()), RDF.TYPE, DCAT.DATASET);
                    locB.getConnection().add(factoryLoc.createIRI(binding.getValue("DS").stringValue()), DC.PUBLISHER, factory.createIRI(binding.getValue("nomeOrg").stringValue()));
                    locB.getConnection().add(factoryLoc.createIRI(binding.getValue("DS").stringValue()), factory.createIRI("http://purl.org/dc/terms/references"), factory.createIRI(binding.getValue("nomeOrg").stringValue()));
                }

                if(binding.hasBinding("DS")&&binding.hasBinding("nomeOrg")) {
                    locB.getConnection().add(factoryLoc.createIRI(binding.getValue("nomeOrg").stringValue()), RDF.TYPE, factory.createIRI(binding.getValue("publicador").stringValue()));
                    locB.getConnection().add(factoryLoc.createIRI(binding.getValue("DS").stringValue()), RDF.TYPE, DCAT.DATASET);
                    locB.getConnection().add(factoryLoc.createIRI(binding.getValue("DS").stringValue()), DC.PUBLISHER, factory.createIRI(binding.getValue("nomeOrg").stringValue()));
                    locB.getConnection().add(factoryLoc.createIRI(binding.getValue("DS").stringValue()), factory.createIRI("http://purl.org/dc/terms/references"), factory.createIRI(binding.getValue("nomeOrg").stringValue()));
                }



                //if(binding.hasBinding(""))



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




            //        //Carregar dados
            //        repoCon.begin();
            //        Update updateOp = repoCon.prepareUpdate(QueryLanguage.SPARQL, classeConceito.stringValue());
            //        updateOp.execute();


            //carregaDados(repoCon);




            //Encerrar a conexão
            repoCon.close();
            repository.shutDown();
            manager.shutDown();


            //        RepositoryConfig repositoryConfig = RepositoryConfig.create(graph,repositoryNode);
            //        manager.addRepositoryConfig(repositoryConfig);
            //

            current++;
        }
        locB.runQuery("select * where { ?s ?p ?o. }");
        locB.printQueryResult();

        locB.finishConnection();


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



        try {
            repoCon.add(new File("metadata_from_portal.rdf"),"", RDFXML);
        } catch (IOException e) {
            e.printStackTrace();
        }


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
