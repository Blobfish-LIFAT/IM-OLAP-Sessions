package fr.univ_tours.info.im_olap;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLExctractor {
    static String logFile = "data/log.out";
    static String outFile = "data/MDXtoSQL.json";
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static Pattern sqlPattern = Pattern.compile("executing sql \\[(.*)]");
    static Pattern mdxIDPattern = Pattern.compile("mondrian.mdx {2}- (\\d*)");
    static Pattern mdxExctract = Pattern.compile("DEBUG mondrian.mdx  - (\\d*): (.*)");


    public static void main(String[] args) throws Exception{
        HashMap<String, List<String>> map = new HashMap<>();
        try (BufferedReader file = new BufferedReader(new FileReader(logFile))){
            String line;
            List<String> statements = new ArrayList<>();
            String MDX = "";
            int logID = -1;
            boolean filling = false;

            while ((line = file.readLine()) != null){
                if (filling){
                    if (!line.isEmpty())
                        MDX += line + "\n";
                    else {
                        MDX = MDX.substring(0, MDX.length()-1);
                        filling = false;
                    }
                    continue;
                }

                if (line.contains("mondrian.mdx")){
                    if (line.contains(": exec: "))
                        continue;
                    Matcher idm = mdxIDPattern.matcher(line);
                    idm.find();
                    int newID = Integer.parseInt(idm.group(1));
                    if (logID == newID){
                        continue;
                    }else {
                        if (logID != -1) {
                            insertOrAppend(map, statements, MDX);
                        }

                        logID = newID;
                        statements = new ArrayList<>();
                        Matcher mdxm = mdxExctract.matcher(line);
                        mdxm.find();
                        MDX = mdxm.group(2) + "\n";
                        filling = true;
                        continue;
                    }
                }

                Matcher sqlm = sqlPattern.matcher(line);
                if (sqlm.find()){
                    statements.add(sqlm.group(1) + ";");
                    continue;
                }
                if (line.contains("executing sql [") && !line.contains("]")){
                    String tmp = "";
                    boolean exit = false;
                    while (!exit){
                        line = file.readLine();
                        tmp += line;
                        if (line.contains("]"))
                            exit = true;
                    }
                    statements.add(tmp.replace(']', ';'));
                }
            }

            insertOrAppend(map, statements, MDX);
        }

        Files.write(gson.toJson(map).getBytes(), new File(outFile));

        debugStats(map);

    }

    private static void debugStats(HashMap<String, List<String>> map) {
        AtomicInteger c = new AtomicInteger(0);
        map.forEach((k, v) -> {
            if (v.isEmpty())
                c.incrementAndGet();
        });
        System.out.printf("Couldn't find SQL for %s queries our of %s !", c.get(), map.keySet().size());
    }

    private static <K, V> void insertOrAppend(HashMap<K, List<V>> map, List<V> statements, K MDX) {
        if (map.get(MDX) == null)
            map.put(MDX, statements);
        else {
            map.get(MDX).addAll(statements);
        }
    }
}
