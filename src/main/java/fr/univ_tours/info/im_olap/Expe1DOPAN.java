package fr.univ_tours.info.im_olap;

import com.alexsxode.utilities.Nd4jUtils;
import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import fr.univ_tours.info.im_olap.compute.PageRank;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.model.*;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class Expe1DOPAN {
    static String cubeSchema = "data/cubeSchemas/DOPAN_DW3.xml";
    private static Pair<INDArray, HashMap<QueryPart, Integer>> original_ref = null;

    public static void main(String[] args) throws Exception{

        System.out.println("--- Establishing Mondrian Connection ---");
        Connection connection = MondrianConfig.getMondrianConnection();
        System.out.println("--- Connected ---");

        Map<String, List<Session>> profiles = new HashMap<>();
        List<Session> allSessions = DopanLoader.loadDir("data/logs/dopan_converted");
        for (Session s : allSessions){
            String username = s.getUserName().get();
            profiles.computeIfAbsent(username, k -> new ArrayList<>());
            profiles.get(username).add(s);
        }


        System.out.println("--- Loaded DOPAN Sessions ---");

        PrintWriter out = new PrintWriter(new FileOutputStream("data/result_dopan.csv"));

        double[] alphas = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (double alpha : alphas) {
            for (String userProfile : profiles.keySet()) {
                HashSet<String> userCubes = new HashSet<>();
                for (Session test : profiles.get(userProfile)){
                    userCubes.add(test.getCubeName());
                }
                for (String cubeName : userCubes) {
                    for (int i = 0; i < 5; i++) {
                        List<Session> user = draw(profiles.get(userProfile), 2, cubeName);
                        Set<Session> all = profiles.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
                        all.removeAll(user);
                        List<Session> log = new ArrayList<>(all);
                        CubeUtils mdUtils = new CubeUtils(connection, cubeName);

                        //System.out.println("Building topology graph...");
                        MutableValueGraph<QueryPart, Double> topoGraph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
                        DimensionsGraph.injectSchema(topoGraph, cubeSchema);
                        FiltersGraph.injectCompressedFilters(topoGraph, mdUtils);
                        //System.out.println("Building Logs graph...");
                        MutableValueGraph<QueryPart, Double> logGraph = SessionGraph.buildFromLog(log);
                        //System.out.println("Building user Graph...");
                        MutableValueGraph<QueryPart, Double> userGraph = SessionGraph.buildFromLog(user);

                        MutableValueGraph<QueryPart, Double> base = SessionEvaluator
                                .<QueryPart>linearInterpolation(0.5, true)
                                .interpolate(topoGraph, ValueGraphBuilder.directed().allowsSelfLoops(true).build(), logGraph);

                        Pair<INDArray, HashMap<QueryPart, Integer>> ref = PageRank.pagerank(base, 50);

                        base = SessionEvaluator
                                .<QueryPart>linearInterpolation(alpha, true)
                                .interpolate(base, ValueGraphBuilder.directed().allowsSelfLoops(true).build(), userGraph);
                        //System.out.println("Graph size is " + base.nodes().size());

                        Pair<INDArray, HashMap<QueryPart, Integer>> withProfile = PageRank.pagerank(base, 50);

                        if (original_ref == null) {
                            original_ref = sort(ref);
                        }
/*
                    INDArray profileDist = aligned(original_ref, withProfile);
                    INDArray refDist = aligned(original_ref, ref);
*/
                        //double hellinger = Nd4jUtils.hellinger(refDist, profileDist);
                        //double jensen = Nd4jUtils.JensenShannon(refDist, profileDist);
                        //System.out.printf("%s;%s;%s;%s%n", userProfile, alpha, hellinger, jensen);

                        out.printf("%s;%s;%s;%s%n", cubeName, userProfile, alpha, printIND(withProfile.left));
                        out.printf("%s;%s;%s;%s%n", cubeName, "Page Rank", alpha, printIND(ref.left));

                        out.flush();
                    }
                }
            }
        }
        out.close();

    }

    private static Pair<INDArray, HashMap<QueryPart, Integer>> sort(Pair<INDArray, HashMap<QueryPart, Integer>> ref) {
        return ref;//TODO
    }

    public static List<Session> draw(List<Session> from, int number, String cubeName){
        from = from.stream().filter(s -> s.getCubeName().equals(cubeName)).collect(Collectors.toList());
        if (number >= from.size())
            return new ArrayList<>(from);

        HashSet<Session> flag = new HashSet<>();
        List<Session> out = new ArrayList<>(number);
        Random rd = new Random();

        int count = 0;
        while (count <= number){
            Session candidate = from.get(rd.nextInt(from.size()));
            if (!flag.contains(candidate)) {
                out.add(candidate);
                flag.add(candidate);
                count++;
            }
        }

        return out;
    }

    private static String printTypes(Pair<INDArray, HashMap<QueryPart, Integer>> ref) {
        int[] parts = new int[ref.right.size()];
        ref.right.forEach((k, v) -> parts[v] = k.getType().getValue());
        return Arrays.toString(parts).replaceAll("[\\[\\] ]", "");
    }

    private static INDArray aligned(Pair<INDArray, HashMap<QueryPart, Integer>> ref, Pair<INDArray, HashMap<QueryPart, Integer>> other) {
        INDArray out = Nd4j.create(ref.left.shape());
        INDArray in = other.left;
        Map<QueryPart, Integer> pos = ref.right;
        for (Map.Entry<QueryPart, Integer> e : other.right.entrySet())
            out.put(0, pos.get(e.getKey()), in.getDouble(0, e.getValue()));
        return out;
    }

    private static String printIND(INDArray in){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.columns(); i++) {
            sb.append(in.getDouble(0, i));
            if (i != in.columns() - 1)
                sb.append(",");
        }
        return sb.toString();
    }


}
