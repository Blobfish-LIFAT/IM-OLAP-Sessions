package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.model.*;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.nd4j.linalg.api.ndarray.INDArray;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public static void gainsToCSVFile(ArrayList<Pair<Query,Double>> results, String sessionName) throws IOException {

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
        CSVFormat format = CSVFormat.DEFAULT.withHeader("session", "query", "query_index", "gain");
        try (CSVPrinter csvPrinter = new CSVPrinter(fileWriter, format)) {
            for (int i = 0; i < results.size(); i++) {
                Pair<Query, Double> pair = results.get(i);
                csvPrinter.printRecord(sessionName, pair.left, i, pair.right);
            }
        }
        System.out.println("Wrote: " + session_eval_folder + "result_" + sessionName + ".csv");
    }


    public static void main(String[] args) {
        System.out.println("Loading sessions...");
        List<Session> sessions = loadCubeloadSessions();
        //List<Session> sessions = loadDopanSessions();

        System.out.println("Creating SessionEvaluator evaluator...");

        SessionEvaluator<QueryPart, Double, Pair<INDArray, HashMap<QueryPart, Integer>>> sessionEvaluator =
                new SessionEvaluator<>(SessionEvaluator::simpleInterconnections,
                        SessionEvaluator.linearInterpolation(0.9, true),
                        SessionEvaluator::pageRank);


        // helps to infer evaluator
        SessionEvaluator.GainEvaluator<Pair<INDArray, HashMap<QueryPart, Integer>>> gainEvaluator =
                SessionEvaluator.QPInterestingness(SessionEvaluator::descriptionLength);

        System.out.println("Started processing sessions...");
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
            //(new HashSet<>(base.nodes())).stream().filter(n -> !baseNodes.contains(n)).forEach(base::removeNode);

            System.out.printf("Schema graph size is %s nodes and %s edges.%n", base.nodes().size(), base.edges().size());

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
                gainsToCSVFile(gains, session.getFilename().replace(".log.json", ""));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }


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

