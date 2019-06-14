package fr.univ_tours.info.im_olap;


import com.alexsxode.utilities.Nd4jUtils;
import com.alexsxode.utilities.collection.Pair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.univ_tours.info.im_olap.compute.PageRank;
import fr.univ_tours.info.im_olap.data.cubeloadBeans.*;


import fr.univ_tours.info.im_olap.model.*;
import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.olap.Dimension;
import mondrian.olap.SchemaReader;
import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Proto3CubeLoad {
    private static String dataDir = "data/logs/ssb_converted/";
    private static String[] cubeloadProfiles = new String[]{"explorative", "goal_oriented", "slice_all", "slice_and_drill"};
    private static Map<String ,String> pprint = new HashMap<>();
    static {
        pprint.put("explorative", "Explorative");
        pprint.put("goal_oriented", "Goal Oriented");
        pprint.put("slice_all", "Slice All");
        pprint.put("slice_and_drill", "Slice and Drill");
    }
    private static String cubeSchema = "data/cubeSchemas/ssb.xml";
    private static Pair<INDArray, HashMap<QueryPart, Integer>> original_ref = null;

    public static void main(String[] args) throws Exception{

        System.out.println("--- Establishing Mondrian Connection ---");
        Connection connection = MondrianConfig.getSeparateConnection("data/ssb.properties");
        System.out.println("--- Connected ---");

        Map<String, List<Session>> profiles = new HashMap<>();
        for (String cubeloadProfile : cubeloadProfiles)
            profiles.put(cubeloadProfile, loadCubeloadXML(dataDir + cubeloadProfile + ".xml", connection, "SSB"));

        System.out.println("--- Loaded Cubeload Sessions ---");

        PrintWriter out = new PrintWriter(new FileOutputStream("data/result_dolap.csv"));
        //out.printf("user_profile,eval_profile,alpha,hellinger%n");
        /*
        HashSet<QueryPart> logParts = new HashSet<>();
        for (String p : cubeloadProfiles){
            for (Session session : profiles.get(p)){
                for (Query q : session){
                    logParts.addAll(q.getAllParts());
                }
            }
        }
        List<String> hashes = new ArrayList<>(logParts.size());
        HashFunction hf = Hashing.md5();
        for (QueryPart qp : logParts){
            //hashes.add(qp.toString());
            hashes.add(hf.hashBytes(ArrayUtils.addAll(qp.getType().getBytes(), qp.getValue().getBytes())).toString());
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.write(Paths.get("data/cubeload_parts_hash.json"), gson.toJson(hashes).getBytes());
         */

        double[] alphas = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        for (double alpha : alphas) {
            //System.out.printf(" --- Running test for alpha=%s ---%n", alpha);
            for (String userProfile : cubeloadProfiles) {
                //for (String evalProfile : cubeloadProfiles) {
                String evalProfile = userProfile;
                    for (Session test : profiles.get(evalProfile)) {
                        List<Session> user = draw(profiles.get(userProfile), 7);
                        Set<Session> all = profiles.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
                        all.removeAll(user);
                        all.remove(test);
                        List<Session> log = new ArrayList<>(all);
                        CubeUtils mdUtils = new CubeUtils(connection, "SSB");

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
                            original_ref = ref;
                            out.printf("userProfile;alpha;");
                            out.printf("%s%n", printTypes(ref));
                        }

                        INDArray profileDist = aligned(original_ref, withProfile);
                        INDArray refDist = aligned(original_ref, ref);

                        double hellinger = Nd4jUtils.hellinger(refDist, profileDist);
                        System.out.printf("%s;%s;%s%n", pprint.get(userProfile), alpha, hellinger);

                        out.printf("%s;%s;%s%n", pprint.get(userProfile), alpha, printIND(profileDist));
                        out.printf("%s;%s;%s%n", "Page Rank", alpha, printIND(refDist));

                        out.flush();
                    }
                //}
            }
        }
        out.close();

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

    public static  <T> List<T> draw(List<T> from, int number){
        if (number >= from.size())
            return new ArrayList<>(from);

        HashSet<T> flag = new HashSet<>();
        List<T> out = new ArrayList<>(number);
        Random rd = new Random();

        int count = 0;
        while (count <= number){
            T candidate = from.get(rd.nextInt(from.size()));
            if (!flag.contains(candidate)) {
                out.add(candidate);
                flag.add(candidate);
                count++;
            }
        }

        return out;
    }

    public static List<Session> loadCubeloadXML(String filePath, Connection connection, String cubeName){
        ArrayList<Session> sessions = new ArrayList<>();
        int count = 0;
        try {
            JAXBContext context = JAXBContext.newInstance(Benchmark.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Benchmark benchmark = (Benchmark) unmarshaller.unmarshal(new File(filePath));

            CubeUtils cube = new CubeUtils(connection, cubeName);
            SchemaReader reader = cube.getCube().getSchemaReader(null).withLocus();

            for (fr.univ_tours.info.im_olap.data.cubeloadBeans.Session s : benchmark.getSession()){
                List<Query> queries = new ArrayList<>();
                for (fr.univ_tours.info.im_olap.data.cubeloadBeans.Query q : s.getQuery()){
                    List<QueryPart> parts = new ArrayList<>();
                    // Load Dimensions
                    for (Element dim : q.getGroupBy().getElement()){
                        String hName = ((Hierarchy) dim.getContent().stream().filter(e -> e instanceof Hierarchy).findFirst().get()).getValue();
                        String levelName = ((Level) dim.getContent().stream().filter(e -> e instanceof Level).findFirst().get()).getValue();
                        String tmp = "["+hName+"]";
                        Dimension mdDim = reader.getCubeDimensions(cube.getCube()).stream()
                                .filter(dimension -> Arrays.stream(dimension.getHierarchies()).anyMatch(h -> h.toString().equals(tmp)))
                                .findFirst().get();
                        parts.add(QueryPart.newDimension("[" + mdDim.getName() + "].[" + hName + "].[" + levelName + "]"));
                    }

                    //Load Filters
                    for (Object obj : q.getSelectionPredicates().getContent()){
                        if (obj instanceof String)
                            continue;
                        Element sel = (Element) obj;
                        String hName = ((Hierarchy) sel.getContent().stream().filter(e -> e instanceof Hierarchy).findFirst().get()).getValue();
                        String levelName = ((Level) sel.getContent().stream().filter(e -> e instanceof Level).findFirst().get()).getValue();
                        String predicate = ((Predicate) sel.getContent().stream().filter(e -> e instanceof Predicate).findFirst().get()).getValue();
                        String tmp = "["+hName+"]";
                        Dimension mdDim = reader.getCubeDimensions(cube.getCube()).stream()
                                .filter(dimension -> Arrays.stream(dimension.getHierarchies()).anyMatch(h -> h.toString().equals(tmp)))
                                .findFirst().get();
                        //parts.add(QueryPart.newFilter("[" + mdDim.getName() + "].[" + hName + "].[" + levelName + "]", predicate));
                        parts.add(QueryPart.newFilter(predicate));
                    }

                    //Load Measures
                    for (Element meas : q.getMeasures().getElement()){
                        String measName =  meas.getValue();
                        parts.add(QueryPart.newMeasure("[Measures].["+measName+"]"));
                    }

                    queries.add(new Query(parts));
                }

                sessions.add(new Session(queries, s.getTemplate(), filePath + "_" + count++, cubeName));
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return sessions;
    }


}
