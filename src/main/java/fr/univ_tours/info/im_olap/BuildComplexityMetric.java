package fr.univ_tours.info.im_olap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import org.dom4j.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        setExplainMode(con, true);

        Map<String, List<String>> mdxMap = gson.fromJson(new String(Files.readAllBytes(Paths.get(mdxMapFile))), Map.class);

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

        com.google.common.io.Files.write(gson.toJson(ops).getBytes(), new File(outFile));

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
