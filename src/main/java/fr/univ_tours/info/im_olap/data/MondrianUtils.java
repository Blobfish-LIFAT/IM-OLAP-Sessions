package fr.univ_tours.info.im_olap.data;

import mondrian.olap.Connection;
import mondrian.olap.Cube;
import mondrian.olap.Schema;

public class MondrianUtils {
    Connection con;

    public MondrianUtils(Connection con) {
        this.con = con;
    }

    public Cube getCubeByName(String name){
        Schema schema = con.getSchema();

        for (Cube c : schema.getCubes()){
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }
}
