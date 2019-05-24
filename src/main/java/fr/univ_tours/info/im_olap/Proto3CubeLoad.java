package fr.univ_tours.info.im_olap;


import fr.univ_tours.info.im_olap.data.cubeloadBeans.*;
import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.Connection;
import mondrian.olap.Dimension;
import mondrian.olap.SchemaReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Proto3CubeLoad {
    private static String dataDir = "data/logs/ssb_converted";
    private static String cubeSchema = "data/cubeSchemas/ssb.xml";

    public static void main(String[] args) throws Exception{
        String path = "data/logs/ssb_converted/explorative.xml";
        System.out.println("--- Establishing Mondrian Connection ---");
        Connection connection = MondrianConfig.getSeparateConnection("data/ssb.properties");
        System.out.println("--- Connected ---");
        List<Session> testStuff = loadCubeloadXML(path, connection, "SSB");

        System.out.println("--- Loaded Cubeload Sessions ---");
        for (Session s : testStuff)
            System.out.println(s);
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
                        parts.add(QueryPart.newFilter("[" + mdDim.getName() + "].[" + hName + "].[" + levelName + "]", predicate));
                    }

                    //Load Measures
                    for (Element meas : q.getMeasures().getElement()){
                        String measName =  meas.getValue();
                        parts.add(QueryPart.newMeasure("[Measures].["+measName+"]"));
                    }

                    queries.add(new Query(parts));
                }

                sessions.add(new Session(queries, s.getProfile(), filePath + "_" + count++));
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return sessions;
    }


}
