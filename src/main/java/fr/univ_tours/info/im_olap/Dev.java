package fr.univ_tours.info.im_olap;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.model.QueryPart;

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
    }
}
