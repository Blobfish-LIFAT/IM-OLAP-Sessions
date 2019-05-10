package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.mondrian.MondrianConfig;
import fr.univ_tours.info.im_olap.mondrian.CubeUtils;
import mondrian.olap.Connection;
import mondrian.olap.Dimension;
import mondrian.olap.Member;
import mondrian.rolap.RolapCubeLevel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Dev {
    public static void main(String[] args) {
        Connection olap = MondrianConfig.getMondrianConnection();
        CubeUtils mdutils = new CubeUtils(olap, "Cube1MobProInd");
        Dimension d = mdutils.getDimensionByName("Commune de residence");
        RolapCubeLevel l = (RolapCubeLevel) d.getHierarchies()[0].getLevels()[1];

        List<List<Member>> res = Arrays.stream(d.getHierarchies()[0].getLevels()).map(mdutils::fetchMembers).collect(Collectors.toList());
    }
}
