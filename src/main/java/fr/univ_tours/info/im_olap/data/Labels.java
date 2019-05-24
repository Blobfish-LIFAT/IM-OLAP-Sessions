package fr.univ_tours.info.im_olap.data;


import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.Session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Labels {
    public static List<Session> addLabels(List<Session> toLabel, String labelFile, String... columnsLabels){
        HashMap<String, List<String[]>> map = new HashMap<>();
        List<String> prop;

        try {
            List<String> lines = Files.readAllLines(Paths.get(labelFile));
            prop = Arrays.stream(lines.get(0).split(";")).skip(2).collect(Collectors.toList());
            lines.stream().skip(1).forEach(l -> {
                String[] tmp = l.split(";");
                String key = tmp[1].replaceAll("log(-\\d*)", "log.json");
                if (!map.containsKey(key)) {
                    List<String[]> queries = new ArrayList<>();
                    queries.add(Arrays.copyOfRange(tmp, 2, tmp.length));
                    map.put(key, queries);
                } else {
                    map.get(key).add(Arrays.copyOfRange(tmp, 2, tmp.length));
                }
            });

            for (Session session : toLabel){
                if (map.containsKey(session.getFilename())){
                    try {
                        for (int i = 0; i < session.queries.size(); i++) {
                            Query current = session.queries.get(i);
                            String[] properties = map.get(session.getFilename()).get(i);
                            for (String col : columnsLabels){
                                try {
                                    current.getProperties().put(col, Integer.parseInt(properties[prop.indexOf(col)]));
                                }catch (NumberFormatException e){
                                    current.getProperties().put(col, -1);
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException e){
                        System.err.printf("Error in '%s' session, length missmatch us=%s file=%s%n", session.getFilename(), session.length(), map.get(session.getFilename()).size());
                    }

                }
            }

        } catch (IOException e) {
            System.err.println("--- Error reading CSV ---");
            e.printStackTrace();
        }
        return toLabel;
    }
}
