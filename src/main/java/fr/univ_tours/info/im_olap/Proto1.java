package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.*;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.model.*;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.eigen.Eigen;
import org.nd4j.linalg.factory.Nd4j;


import java.io.*;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static fr.univ_tours.info.im_olap.Proto3CubeLoad.loadCubeloadXML;

public class Proto1 {
    // I know you don't like static variables but it's easier for now since type are not set yet
    // just use function arguments

    private static String cubeSchema;
    private static Connection olap;

    static String session_eval_folder = "data/interp_deBie/";

    public static void gainsToCSVFile(ArrayList<Pair<Query,Double>> results, Session session) throws IOException {

        String sessionName = session.getFilename().replace(".log.json", "");
        String fileName;

        int slashIndex = sessionName.lastIndexOf("/");
        if (slashIndex > -1) {
            fileName = sessionName.substring(slashIndex+1);
        }
        else {
            fileName = sessionName;
        }

        File file = Paths.get(session_eval_folder + "result_" + fileName + ".csv").toFile();
        FileWriter fileWriter = new FileWriter(file);
        CSVFormat format = CSVFormat.DEFAULT.withHeader("user", "session", "query", "query_index", "gain");
        try (CSVPrinter csvPrinter = new CSVPrinter(fileWriter, format)) {
            for (int i = 0; i < results.size(); i++) {
                Pair<Query, Double> pair = results.get(i);
                csvPrinter.printRecord(session.getUserName().orElse("UNKNOWN"), sessionName, pair.left, i, pair.right);
            }
        }
        System.out.println("Wrote: " + session_eval_folder + "result_" + sessionName + ".csv");
    }


    public static void main(String[] args) throws FileNotFoundException{

        System.out.println("Loading sessions...");
        //List<Session> sessions = loadCubeloadSessions();
        List<Session> sessions = loadDopanSessions();

        System.out.println("Creating SessionEvaluator evaluator...");

        SessionEvaluator<QueryPart, Double, Pair<INDArray, HashMap<QueryPart, Integer>>> sessionEvaluator =
                new SessionEvaluator<>(SessionEvaluator::simpleInterconnections,
                        SessionEvaluator.linearInterpolation(0.9, true),
                        SessionEvaluator::pageRank);


        // helps to infer evaluator
        SessionEvaluator.GainEvaluator<Pair<INDArray, HashMap<QueryPart, Integer>>> gainEvaluator =
                SessionEvaluator.QPInterestingness(SessionEvaluator::descriptionLength);

        System.out.println("Started processing sessions...");

        // Log all MDX and SQL
        // -Dlog4j.debug
        // -Dlog4j.configuration=file:///home/alex/IdeaProjects/IM-OLAP-Sessions/data/log4j.properties
        dumpLog(sessions);

        for (int session_index = 0; session_index < sessions.size(); session_index++) {
            Session session = sessions.get(session_index);

            System.out.println();
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("                     SESSION "+session_index);
            System.out.println("-------------------------------------------------------------------------");
            System.out.println();

            System.out.println("Creating cube utils...");
            CubeUtils mdUtils = new CubeUtils(olap, session.getCubeName());


            System.out.println("Collecting user session...");
            List<Session> thisUser = sessions
                    .stream()
                    .filter(s -> s.getUserName().equals(session.getUserName()))
                    .collect(Collectors.toList());

            thisUser.remove(session);
            List<Session> sessionsModif = new ArrayList<>(sessions);
            sessionsModif.removeAll(thisUser);


            System.out.println("Building topology graph...");
            MutableValueGraph<QueryPart, Double> topoGraph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
            DimensionsGraph.injectSchema(topoGraph, cubeSchema);
            FiltersGraph.injectCompressedFilters(topoGraph, mdUtils);
            System.out.println("Building Logs graph...");
            MutableValueGraph<QueryPart, Double> logGraph = SessionGraph.buildFromLog(sessionsModif);
            System.out.println("Building user Graph...");
            MutableValueGraph<QueryPart, Double> userGraph = SessionGraph.buildFromLog(thisUser);

            HashSet<QueryPart> allNodes = new HashSet<>(topoGraph.nodes());
            allNodes.addAll(logGraph.nodes());
            allNodes.forEach(topoGraph::addNode);
            allNodes.forEach(logGraph::addNode);

            MutableValueGraph<QueryPart, Double> base = SessionEvaluator
                    .<QueryPart>linearInterpolation(0.5, true)
                    .interpolate(topoGraph, ValueGraphBuilder.directed().allowsSelfLoops(true).build(), logGraph);

            base = SessionEvaluator
                    .<QueryPart>linearInterpolation(0.8, true)
                    .interpolate(base, ValueGraphBuilder.directed().allowsSelfLoops(true).build(), userGraph);


            // Remove nodes not in the log
            System.out.println("Pruning Graph...");
            Set<QueryPart> baseNodes = new HashSet<>();
            baseNodes.addAll(logGraph.nodes());
            baseNodes.addAll(userGraph.nodes());
            Set<QueryPart> extension = new HashSet<>();
            for (QueryPart node : baseNodes){
                extension.addAll(successorsIfPresent(userGraph.asGraph(), node));
                extension.addAll(successorsIfPresent(logGraph.asGraph(), node));
            }
            baseNodes.addAll(extension);
            System.out.println(baseNodes.size());
            (new HashSet<>(base.nodes())).stream().filter(n -> !baseNodes.contains(n)).forEach(base::removeNode);

            System.out.printf("Schema graph size is %s nodes and %s edges.%n", base.nodes().size(), base.edges().size());

            for (QueryPart queryPart : base.nodes()) {
                base.putEdgeValue(queryPart, queryPart, 1.0);
            }

            //runAdajacencyTest(base);

            /**
             * Ben's stuff
             */

            System.out.println("Adding session query parts...");

            for (Query query : session.queries) {
                for (QueryPart qp : query.getAllParts()) {
                    base.addNode(qp);
                }
            }

            System.out.println("Ensuring all nodes have self edges...");

            for (QueryPart queryPart : base.nodes()) {
                base.putEdgeValue(queryPart, queryPart, 1.0);
            }

            System.out.printf("Graph size is %s nodes and %s edges.%n", base.nodes().size(), base.edges().size());

            System.out.println("Normalizing edges weights...");

            Graphs.normalizeWeightsi(base);

            System.out.println("Converting graph to matrix...");

            Pair<INDArray, HashMap<QueryPart, Integer>> pair = Graphs.toINDMatrix(base);

            System.out.println("Matrix shape:");
            System.out.println(Arrays.toString(pair.left.shape()));

            System.out.println();





            System.out.println("Evaluating session...");
            ArrayList<Pair<Query, Pair<INDArray, HashMap<QueryPart, Integer>>>> liste = sessionEvaluator.evaluateSession(base, session);

            System.out.print("Handling query ");

            int i = 0;
            for (Pair<Query, Pair<INDArray, HashMap<QueryPart, Integer>>> p : liste ) {
                System.out.print(i + ", ");

            /*
            ArrayList<Pair<QueryPart, Double>> displayList = new ArrayList<>();

            for (Map.Entry<QueryPart, Integer> entry : p.right.right.entrySet()) {
                displayList.add(new Pair<>(entry.getKey(), p.right.left.getDouble(entry.getValue())));
            }

            displayList.sort(Comparator.comparing(x -> x.left.toString()));

            for (Pair<QueryPart, Double> pair1 : displayList) {
                System.out.print(pair1.right.toString() + ",");
            }
            System.out.println();

            */
                i++;
            }


            System.out.println();
            System.out.println("-------------------------------------------------------------------------------");
            System.out.println();

            System.out.println("Computing gains...");
            System.out.println();

            ArrayList<Pair<Query, Double>> gains = SessionEvaluator.computeGains(liste, gainEvaluator);

            System.out.println("End of evaluation.");

            System.out.println("Writing results to CSV...");
            try {
                gainsToCSVFile(gains, session);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }


        }


    }

    private static void dumpLog(List<Session> sessions) {
        Map<Integer, String> mdxmap = DopanLoader.loadMDX("data/logs/dopan_converted");
        //LogManager.getRootLogger().setLevel(Level.DEBUG);
        for (Session session : sessions) {
            for (Query q : session.queries) {
                LogManager.getLogger(mondrian.rolap.RolapResultShepherd.class).debug("[MARKER][" + q.getProperties().get("id") + "]");
                try {
                    mondrian.olap.Query olapQuery = olap.parseQuery(mdxmap.get(q.getProperties().get("id")));
                    olap.execute(olapQuery);
                } catch (Exception e){
                    LogManager.getLogger(mondrian.rolap.RolapResultShepherd.class).debug("[MARKER][ERROR]");
                    System.err.println("Error for : " + q.getProperties().get("id"));
                }
                LogManager.getLogger(mondrian.rolap.RolapResultShepherd.class).debug("[MARKER][END]");
            }
        }
        //LogManager.getRootLogger().setLevel(Level.OFF);
        System.exit(0);
    }

    private static <V> void runAdajacencyTest(ValueGraph<V, Double> graph) {
        Nd4j.setDefaultDataTypes(DataType.DOUBLE, DataType.DOUBLE);
        List<V> nodes = new ArrayList<>(graph.nodes());

        HashMap<V, Integer> indexes = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            indexes.put(nodes.get(i), i);
        }

        int size = nodes.size();

        INDArray matrix = Nd4j.zeros(size,size);

        for (int i = 0; i < size; i++) {
            V from = nodes.get(i);
            for (V to : graph.successors(from)){
                int toIndex = indexes.get(to);
                if (i == toIndex)
                    matrix.put(i, toIndex, 1);
                else
                    //matrix.put(i, toIndex, 1);
                    matrix.put(i, toIndex, -1.0/Math.sqrt(graph.degree(to)*graph.degree(from)));
            }
        }

        INDArray eigenvalues = Eigen.symmetricGeneralizedEigenvalues(matrix, false);
        Nd4j.writeTxt(eigenvalues, "data/eigentest.txt");
        List<Double> vals = Arrays.stream(eigenvalues.toDoubleVector()).boxed().collect(Collectors.toList());
        Collections.sort(vals);
        for (Double val : vals)
            System.out.println(val);
        System.exit(0);
    }

    private static <T> Set<T> successorsIfPresent(Graph<T> graph, T node) {
        if (!graph.nodes().contains(node)) return new HashSet<>(0);
        HashSet<T> out = new HashSet<>();
        for (EndpointPair<T> pair: graph.incidentEdges(node)){
            if (pair.nodeV().equals(node))
                out.add(pair.nodeU());
        }
        return out;
    }

    public static List<Session> loadDopanSessions(){
        System.out.println("Connecting to Mondrian...");
        olap = MondrianConfig.getMondrianConnection();
        cubeSchema = "data/cubeSchemas/DOPAN_DW3.xml";
        return SessionGraph.fixSessions(DopanLoader.loadDir("data/logs/dopan_converted"), cubeSchema);
    }

    public static List<Session> loadCubeloadSessions(){
        cubeSchema = "data/cubeSchemas/ssb.xml";
        String paths[] = new String[]{"data/logs/ssb_converted/explorative.xml", "data/logs/ssb_converted/goal_oriented.xml", "data/logs/ssb_converted/slice_all.xml", "data/logs/ssb_converted/slice_and_drill.xml"};
        System.out.println("--- Establishing Mondrian Connection ---");
        try {
            olap = MondrianConfig.getSeparateConnection("data/ssb.properties");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("--- Connected ---");
        List<Session> testStuff = new ArrayList<>();
        for (String path : paths)
            testStuff.addAll(loadCubeloadXML(path, olap, "SSB"));

        System.out.println("--- Loaded Cubeload Sessions ---");

        return testStuff;
    }


}

