package fr.univ_tours.info.im_olap;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.*;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.model.Session;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.*;


import java.util.*;

public class Dev {
    public static void main(String[] args) {

        Map<Integer, String> mdxAncIDs = DopanLoader.loadMDX("data/logs/dopan_converted");
        int i = 0;
        for (Map.Entry e : mdxAncIDs.entrySet()){
            //System.out.println(e.getValue());
            if (i++>100)
                break;
        }

        /*
        Connection olap = MondrianConfig.getMondrianConnection();
        Query q = olap.parseQuery("select NON EMPTY Crossjoin({[Measures].[Nombre total d'individus]}, {[Mode de transport.MODTRANS_Hierarchie].[Transports en commun].[Transports en commun]}) ON COLUMNS,\n" +
                "  NON EMPTY {Hierarchize({[Niveau d'instruction.Niveau_Instruction_Hierarchie].[Niveau d'instruction].Members})} ON ROWS\n" +
                "from [Cube1MobProInd]");
        Result result = olap.execute(q);
        for (Axis ax : result.getAxes()){
            System.out.println(ax.getPositions());
        }
*/
        //List<Session> sessions = DopanLoader.loadDir("data/logs/dopan_converted");
        //sessions.stream().filter(s -> s.queries.size() > 100).map(Session::getFilename).forEach(System.out::println);

        Connection con = MondrianConfig.getMondrianConnection();
        CubeUtils cb1 = new CubeUtils(con, "Cube1MobProInd");
        ///Arrays.stream(cb1.getCube().getDimensions()).filter(Dimension::isMeasures).map(d -> d.getHierarchies()[0].getLevels()).flatMap(Arrays::stream).map(level -> level.).forEach(System.out::println);

        CubeUtils cb2 = new CubeUtils(con, "Cube2MobScoInd");
        System.out.println(Arrays.stream(cb2.getCube().getDimensions()).filter(Dimension::isMeasures).count());

        CubeUtils cb3 = new CubeUtils(con, "Cube4Chauffage");
        System.out.println(Arrays.stream(cb3.getCube().getDimensions()).filter(Dimension::isMeasures).count());
    }
}
