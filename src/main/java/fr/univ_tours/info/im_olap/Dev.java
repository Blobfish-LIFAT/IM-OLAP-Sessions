package fr.univ_tours.info.im_olap;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.model.QueryPart;
import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import mondrian.olap.Axis;
import mondrian.olap.Connection;
import mondrian.olap.Query;
import mondrian.olap.Result;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Dev {
    public static void main(String[] args) {

        Map<Integer, String> mdxAncIDs = DopanLoader.loadMDX("data/logs/dopan_converted");
        int i = 0;
        for (Map.Entry e : mdxAncIDs.entrySet()){
            System.out.println(e.getValue());
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


    }
}
