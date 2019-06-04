package fr.univ_tours.info.im_olap;


import com.google.gson.Gson;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.data.Labels;
import fr.univ_tours.info.im_olap.model.Query;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.model.SessionGraph;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static fr.univ_tours.info.im_olap.Proto1.loadDopanSessions;


public class Proto2 {
    static String dataDir = "data/logs/dopan_converted";
    public static void main(String[] args) throws Exception{
        List<Session> sessions = loadDopanSessions();
        Map<Integer, String> mdxmap = DopanLoader.loadMDX("data/logs/dopan_converted");
        Gson gson = new Gson();
        Map<String, List<String>> map = gson.fromJson(new String(Files.readAllBytes(Paths.get("data/MDXtoSQL.json"))), Map.class);
        int total = 0, matches = 0;
        for (Session s : sessions){
            for (Query q : s.queries){
                String mdx = mdxmap.get(q.getProperties().get("id"));

                List<String> sql = map.get(mdx);
                if (!sql.isEmpty())
                    matches++;
                total++;
            }
        }

        System.out.println(matches + "/" + total);

    }
}
