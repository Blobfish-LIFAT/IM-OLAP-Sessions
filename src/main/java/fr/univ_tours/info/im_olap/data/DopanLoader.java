package fr.univ_tours.info.im_olap.data;

import com.google.gson.Gson;
import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.model.Session;
import static fr.univ_tours.info.im_olap.model.QueryPart.Type.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DopanLoader {
    static Gson gson = new Gson();

    public static Session loadFile(Path path){
        try {
            String src = new String(Files.readAllBytes(path));
            DopanSession in = gson.fromJson(src, DopanSession.class);
            Session out = new Session(new ArrayList<>(), "USER", path.getFileName().toString());
            out.setCubeName(in.getQueries().get(0).getCubeName());
            out.setUserName(in.getUser());

            for (DopanQuery q : in.getQueries()){
                Query qnew = new Query();
                qnew.addAll(q.getGroupBySet().stream().map(g -> new QueryPart(DIMENSION, g)).collect(Collectors.toSet()));
                qnew.addAll(q.getMeasures().stream().map(m -> new QueryPart(MEASURE, m)).collect(Collectors.toSet()));
                qnew.addAll(q.getSelection().stream().map(s -> new QueryPart(s.getLevel(), s.getValue())).collect(Collectors.toSet()));
                out.queries.add(qnew);
            }
            return out;
        } catch (IOException e) {
            return new Session(new ArrayList<>(), "ERROR", "NOT FOUND");
        }
    }

    public static List<Session> loadDir(String path){
        if (!Files.isDirectory(Paths.get(path))){
            System.err.printf("Warning '%s' is not a valid directory !", path);
        }
        try {
            return Files.walk(Paths.get(path)).filter(p -> p.toFile().isFile()).map(DopanLoader::loadFile).sorted(Comparator.comparing(Session::getFilename)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
