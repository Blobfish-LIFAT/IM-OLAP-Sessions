package fr.univ_tours.info.im_olap.eval;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProjectIntents {
    static String input = "data/webi/blocprofile_lda.csv";
    static String output = "data/objectnameprofile_lda.csv";

    public static void main(String[] args) throws Exception {

        Random rd = new Random();

        LinkedHashMap<String, Set<INDArray>> objects = new LinkedHashMap<>();
        Class.forName("org.mariadb.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/webi", "root", "");

        List<String[]> lines = Files.lines(Paths.get(input))/*.skip(1)*/.map(l -> l.split("\\|")).collect(Collectors.toList());

        int c = 0;
        for(String[] b: lines) {

            String[] id = b[2].split(";");
            try {
                List<String> objs = fetchObjectnames(connection, b[0], Integer.parseInt(b[1]), Integer.parseInt(id[0]));
                INDArray vec = toVec(id);
                for (String obj : objs){
                    if (objects.get(obj) == null) {
                        HashSet<INDArray> s = new HashSet<>();
                        s.add(vec);
                        objects.put(obj, s);
                    } else
                        objects.get(obj).add(vec);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (c++ % 1000 == 0)
                System.out.printf("Parsed %d lines%n", c - 1);
        }

        System.out.println("Map construction Complete");

        FileWriter out = new FileWriter(output);

        for (Map.Entry<String, Set<INDArray>> entry : objects.entrySet()) {
            String k = entry.getKey();
            Set<INDArray> v = entry.getValue();
            out.write(k + ",");
            INDArray stack = Nd4j.vstack(v);
            INDArray mean = stack.mean(0);
            //entropy of the distribution
            /*double sum = 0;
            for (int i = 0; i < mean.columns(); i++) {
                sum += mean.getDouble(i)*log2(mean.getDouble(i));
            }
            sum = -sum;
            System.out.println("Entropy for " + k + " is " + sum/log2(18));*/

            for (int i = 0; i < 18; i++) {
                out.write(String.valueOf(mean.getScalar(i)));
                if (i != 17)
                    out.write(",");
            }
            out.write("\n");
            out.flush();
        }

    }

    public static List<String> fetchObjectnames(Connection connection, String docid, int reportid, int blocid) throws SQLException {
        PreparedStatement st = connection.prepareStatement("select objectname from blocs where parent = ? AND reportid = ? AND blockid = ?;");
        ArrayList<String> ans = new ArrayList<>();
        st.setString(1, docid);
        st.setInt(2, reportid);
        st.setInt(3, blocid);
        ResultSet rs = st.executeQuery();
        while (rs.next()){
            ans.add(rs.getString("objectname"));
        }
        return ans;
    }

    public static INDArray toVec(String s){
        String[] temp = s.split(",");
        INDArray res = Nd4j.create(temp.length);
        for (int i = 0; i < temp.length; i++) {
            res.putScalar(i, Float.parseFloat(temp[i]));
        }
        return res;
    }

    public static INDArray toVec(String[] a){

        INDArray res = Nd4j.create(a.length-1);
        for (int i = 1; i < a.length; i++) {
            res.putScalar(i-1, Float.parseFloat(a[i]));
        }
        return res;
    }

}
