package fr.univ_tours.info.im_olap.eval;



import com.alexsxode.utilities.collection.Pair;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ResolveObjectNames {
    // File Format .csv -> userid,docid,reportid
    static String file = "data/allUsages.csv";


    public static void main(String[] args) throws Exception {
        List<String> ids = Files.lines(Paths.get(file)).map(s -> s.split(";")).map(t -> t[1] + "|" + t[2]).collect(Collectors.toList());
        Class.forName("org.mariadb.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/webi", "root", "");

        FileWriter out = new FileWriter(new File("data/baseline_intrestingness.csv"));
        List<Pair<String, List<String>>> resolved = resolve(ids, connection);
        int i = 0;
        for (String line : Files.readAllLines(Paths.get(file))){
            String userid = line.split(";")[0];
            out.write(userid + "|" + resolved.get(i).getA() + "|");
            List<String> objects = resolved.get(i).getB();
            for (int j = 0; j < objects.size() -1; ++j){
                out.write(objects.get(j) + ",");
            }
            out.write(objects.get(objects.size() - 1) + "\n");
            ++i;
        }
        out.flush();
        out.close();
    }

    public static List<Pair<String, List<String>>> resolve(List<String> agregateID, Connection db) throws SQLException {
        PreparedStatement st = db.prepareStatement("SELECT objectname FROM blocs WHERE parent = ? AND reportid = ?;");

        List<Pair<String, List<String>>> result = new ArrayList<>(agregateID.size());

        int i = 0;
        for (String id : agregateID){
            //System.out.println("Line is : " + id);
            String[] idS = id.split("\\|");
            st.setString(1, idS[0]);
            st.setInt(2, Integer.parseInt(idS[1]));
            ResultSet rs = st.executeQuery();
            ArrayList<String> tmp = new ArrayList<>();

            while (rs.next()){
                tmp.add(rs.getString("objectname"));
            }

            rs.close();
            result.add(new Pair<>(id, tmp));
            if (i%1000==0)
                System.out.println("Did " + i + " lines.");
            i++;
        }

        return result;
    }
}
