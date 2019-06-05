package fr.univ_tours.info.im_olap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import org.dom4j.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;


public class BuildComplexityMetric {

    static String outFile = "data/complexity.json";
    static String mdxMapFile = "data/MDXtoSQL.json";
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static Map uris = new HashMap();
    static {
        uris.put("ms", "http://schemas.microsoft.com/sqlserver/2004/07/showplan");
    }



    public static void main(String[] args) throws Exception{
        Map<Integer, String> mdxAncIDs = DopanLoader.loadMDX("data/logs/dopan_converted");

        MondrianConfig.getMondrianConnection();
        Connection con = MondrianConfig.getJdbcConnection();

        Map<String, List<String>> mdxMap = gson.fromJson(new String(Files.readAllBytes(Paths.get(mdxMapFile))), Map.class);

        //Map<Integer, List<String>> ops = extractOps(mdxAncIDs, con, mdxMap);
        //com.google.common.io.Files.write(gson.toJson(ops).getBytes(), new File(outFile));
        Map<String, List<String>> ops = gson.fromJson(new String(Files.readAllBytes(Paths.get(outFile))), HashMap.class);

        List<Session> sessions = DopanLoader.loadDir("data/logs/dopan_converted");
        HashMap<Integer, Query> queries = new HashMap<>();
        for (Session s : sessions){
            for (Query q : s.queries){
                queries.put((Integer) q.getProperties().get("id"), q);
            }
        }

        System.out.println("querry_id,total_operators,distinct_operators,ascii_len,part_number");
        ops.forEach((id, values) -> {
            //Integer idI = id;
            Integer idI = Integer.parseInt(id);
            int n1 = new HashSet<>(values).size();
            System.out.printf("%s,%s,%s,%s,%s%n", idI.toString(), values.size(), n1, mdxAncIDs.get(idI).length(), queries.get(idI).getAllParts().size());
        });

    }

    private static Map<Integer, List<String>> extractOps(Map<Integer, String> mdxAncIDs, Connection con, Map<String, List<String>> mdxMap) throws SQLException, IOException {
        setExplainMode(con, true);
        Map<Integer, List<String>> ops = new HashMap<>();
        for (Map.Entry entry : mdxAncIDs.entrySet()){
            int key = (int) entry.getKey();
            String mdxQuery = (String) entry.getValue();

            List<String> sqlStatements = mdxMap.get(mdxQuery);
            for (String sqlQuery : sqlStatements){
                Statement preparedCall = con.createStatement();
                ResultSet resultSet = preparedCall.executeQuery(sqlQuery);
                resultSet.next();
                String xml = resultSet.getString(1);

                insertOrAppend(ops, getOps(xml), key);



            }
        }
        setExplainMode(con, false);
        return ops;
    }

    private static List<String> getOps(String xml) {
        List<String> ops = new ArrayList<>();

        try {
            Document document = DocumentHelper.parseText(xml);

            XPath xPath = document.createXPath("//ms:RelOp");
            xPath.setNamespaceURIs(uris);
            xPath.selectNodes(document).stream().map(n -> ((Element) n).attributeValue("PhysicalOp")).map(Object::toString).forEach(s -> ops.add((String) s));

        } catch (DocumentException e) {
            e.printStackTrace();
        }
        System.out.println(ops);
        return ops;
    }

    private static void setExplainMode(Connection con, boolean status) throws SQLException {
        con.createStatement().execute("SET SHOWPLAN_XML " + (status ? "ON;" : "OFF;"));
    }

    private static <K, V> void insertOrAppend(Map<K, List<V>> map, List<V> statements, K key) {
        if (map.get(key) == null)
            map.put(key, statements);
        else {
            map.get(key).addAll(statements);
        }
    }
}
