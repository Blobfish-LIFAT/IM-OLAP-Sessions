package fr.univ_tours.info.im_olap.model;

import fr.univ_tours.info.im_olap.model.QueryPart.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LoadSessions {

    public static  List<Session> loadFromDir(String path){
        if (!Files.isDirectory(Paths.get(path))){
            System.err.printf("Warning '%s' is not a valid directory !", path);
        }
        try {
            return Files.walk(Paths.get(path)).filter(p -> p.toFile().isFile()).map(LoadSessions::loadSession).sorted(Comparator.comparing(a -> a.filename)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static Session loadSession(Path p){
        try {
            List<String> lines = Files.lines(p).collect(Collectors.toList());
            Session current = null;
            Query q = null;
            int n = 0;
            for (String line : lines){
                if (line.startsWith("Session")){
                    current = new Session(new ArrayList<>(), line.split("template: ")[1], p.getFileName().toString());
                    q = new Query();
                    n = 1;
                }else {
                    if (n == 1){
                        q = new Query();
                    } else if (n == 2){
                        for (String dimension : line.split(", "))
                            q.dimensions.add(new QueryPart(Type.DIMENSION, dimension));
                    } else if (n == 3){
                        for (String filter : line.split(", ")) {
                            if (!filter.equals(""))
                                q.filters.add(new QueryPart(Type.FILTER, filter));
                        }
                    } else if (n == 4){
                        for (String measure : line.split(", "))
                            q.measures.add(new QueryPart(Type.MEASURE, measure));
                    } else if (n == 5){
                        if (current == null)
                            System.out.println("debug");
                        current.queries.add(q);
                        n = 1; continue;
                    }
                    n++;
                }
            }
            return current;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
