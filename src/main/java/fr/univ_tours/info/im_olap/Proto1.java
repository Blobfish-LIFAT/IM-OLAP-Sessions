package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import fr.univ_tours.info.im_olap.compute.PageRank;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.graph.Graphs;
import fr.univ_tours.info.im_olap.model.*;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.eigen.Eigen;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Proto1 {
    // I know you don't like static variables but it's easier for now since type are not set yet
    // just use function arguments
    static String dataDir = "data/logs/dopan_converted";
    private static String cubeSchema = "data/cubeSchemas/DOPAN_DW3.xml";

    static String session_eval_folder = "data/eval_results/";

    public static void gainsToCSVFile(ArrayList<Pair<Query,Double>> results, String sessionName) throws IOException {

        File file = Paths.get(session_eval_folder + "result_" + sessionName + ".csv").toFile();
        FileWriter fileWriter = new FileWriter(file);
        CSVFormat format = CSVFormat.DEFAULT.withHeader("session", "query", "query_index", "gain");
        try (CSVPrinter csvPrinter = new CSVPrinter(fileWriter, format)) {
            for (int i = 0; i < results.size(); i++) {
                Pair<Query, Double> pair = results.get(i);
                csvPrinter.printRecord(sessionName, pair.left, i, pair.right);
            }
        }
    }


    public static void main(String[] args) {
        //Nd4j.setDefaultDataTypes(DataType.DOUBLE, DataType.DOUBLE);

        System.out.println("Connecting to Mondrian...");
        Connection olap = MondrianConfig.getMondrianConnection();

        System.out.println("Loading sessions...");
        List<Session> sessions = SessionGraph.fixSessions(DopanLoader.loadDir(dataDir), cubeSchema);

        System.out.println("Creating SessionEvaluator evaluator...");

        SessionEvaluator<QueryPart, Double, Pair<INDArray, HashMap<QueryPart, Integer>>> sessionEvaluator =
                new SessionEvaluator<>(SessionEvaluator::simpleInterconnections,
                        SessionEvaluator::replaceEdges,
                        SessionEvaluator::pageRank);


        // helps to infer evaluator
        SessionEvaluator.GainEvaluator<Pair<INDArray, HashMap<QueryPart, Integer>>> kullbackLeibler = SessionEvaluator::KullbackLeibler;

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
            sessions.removeAll(thisUser);


            System.out.println("Building topology graph...");
            MutableValueGraph<QueryPart, Double> base = SessionGraph.buildFromLog(sessions);
            Set<QueryPart> baseNodes = new HashSet<>(base.nodes());
            baseNodes.addAll(session.allParts());
            baseNodes.addAll(thisUser.stream().flatMap(s -> s.allParts().stream()).collect(Collectors.toList()));
            DimensionsGraph.injectSchema(base, "data/cubeSchemas/DOPAN_DW3.xml");

            System.out.println("Injecting filters...");
            FiltersGraph.injectCompressedFilters(base, mdUtils);

            (new HashSet<>(base.nodes())).stream().filter(n -> !baseNodes.contains(n)).forEach(base::removeNode);

            System.out.printf("Schema graph size is %s nodes and %s edges.%n", base.nodes().size(), base.edges().size());
/*
        SparseStore demo = toMatrixNorm(base);
        System.out.println("Matrix filled");

        for (int i = 0; i < 42; i++) {
            System.out.printf("Matrix multiplication iteration n°%s%n", i);
            demo.multiply(demo, demo);
        }
*/
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

            ArrayList<Pair<Query, Double>> gains = SessionEvaluator.computeGains(liste, kullbackLeibler);

            System.out.println("End of evaluation.");

            System.out.println("Writing results to CSV...");
            try {
                gainsToCSVFile(gains, session.getFilename().replace(".log.json", ""));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }




    }

/*
    public static <V> SparseStore<Double> toMatrixNorm(ValueGraph<V, ? extends Number> in) {
        List<V> nodes = new ArrayList<>(in.nodes());
        HashMap<V, Integer> indexes = new HashMap<>();

        for (int i = 0; i < nodes.size(); i++) {
            indexes.put(nodes.get(i), i);
        }

        int size = nodes.size();
        final SparseStore<Double> out = SparseStore.PRIMITIVE.make(size, size);

        for (int i = 0; i < size; i++) {
            V from = nodes.get(i);
            double sum = 0;
            for (V to : in.successors(from))
                sum += in.edgeValue(from, to).get().doubleValue();
            for (V to : in.successors(from)){
                int toIndex = indexes.get(to);
                out.set(i, toIndex, in.edgeValue(from, to).get().doubleValue()/sum);
            }
        }

        return out;
    }
*/

}

