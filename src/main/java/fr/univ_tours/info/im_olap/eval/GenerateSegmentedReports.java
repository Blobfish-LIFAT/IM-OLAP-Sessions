package fr.univ_tours.info.im_olap.eval;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateSegmentedReports {
    static String input = "/home/alex/Downloads/popular_reports.csv", output = "/home/alex/Downloads/truncated2.csv";

    public static void main(String[] args) throws Exception {
        List<String> ids = Files.lines(Paths.get(input)).map(s -> s.split(";")).map(t -> t[0] + "|" + t[1] + "|" + t[2]).collect(Collectors.toList());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/webi", "root", "");

        String query = "SELECT blockid, objectname FROM blocs WHERE parent = ? AND reportid = ?;";
        PreparedStatement st = connection.prepareStatement(query);
        List<Report> reports = new ArrayList<>();
        FileWriter out = new FileWriter(new File(output));

        int i = 0;
        for (String id : ids){
            String docid = id.split("\\|")[1];
            String reportid = id.split("\\|")[2];

            st.setString(1, docid);
            st.setInt(2, Integer.parseInt(reportid));

            Report r = new Report();

            ResultSet rs = st.executeQuery();
            while (rs.next()){
                int b = rs.getInt("blockid");
                String s = rs.getString("objectname");
                ArrayList<String> tmp = new ArrayList<>();tmp.add(s);
                r.addBlock(new Bloc(b, tmp));
            }
            if (r.blocs.size() > 6) {
                String userid = id.split("\\|")[0];
                for (int j = 0; j < 6; j++) {
                    out.write(userid + "|" + docid + "|" + reportid + "|");
                    for (int o = 0; o <= r.blocs.size() - 1 - j; o++){
                        for (int k = 0; k < r.blocs.get(o).objectnames.size(); k++) {
                          out.write(r.blocs.get(o).objectnames.get(k));
                          if (k != r.blocs.get(o).objectnames.size() - 1)
                              out.write(",");
                        }
                        if (o != r.blocs.size() - 1 - j){
                            out.write(",");
                        }
                    }
                    out.write("\n");
                    out.flush();
                }
            }
            i++;
            if (i%1000==0){
                System.out.println("did " + i + " lines");
            }
        }

    }


    static class Report{
        ArrayList<Bloc> blocs;

        public Report() {
            this.blocs = new ArrayList<>();
        }

        public void addBlock(Bloc b){
            boolean test = false;
            for (Bloc bl : blocs){
                if (b.id == bl.id) {
                    bl.objectnames.addAll(b.objectnames);
                    test = true;
                }
            }
            if (!test)
                blocs.add(b);
        }

        @Override
        public String toString() {
            return "Report{" +
                    "blocs=" + blocs +
                    '}';
        }
    }
    static class Bloc{
        int id;

        @Override
        public String toString() {
            return "Bloc{" +
                    "id=" + id +
                    ", objectnames=" + objectnames +
                    '}';
        }

        ArrayList<String> objectnames;

        public Bloc(int id, ArrayList<String> objectnames) {
            this.id = id;
            this.objectnames = objectnames;
        }
    }
}

