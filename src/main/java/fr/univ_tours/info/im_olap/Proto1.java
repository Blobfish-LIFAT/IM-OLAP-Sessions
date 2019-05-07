package fr.univ_tours.info.im_olap;

import fr.univ_tours.info.im_olap.data.DopanLoader;
import fr.univ_tours.info.im_olap.model.Session;
import org.olap4j.mdx.ParseTreeNode;
import org.olap4j.mdx.parser.*;
import org.olap4j.mdx.parser.impl.DefaultMdxParserImpl;

public class Proto1 {
    // I know you don't like static variables but it's easier for now since type are not set yet
    // just use function arguments


    public static void main(String[] args) {

        //TODO this is only skeleton for the tests

        String path = "data/";
        loadSessions(path);

        Session test = DopanLoader.loadFile("/home/alex/IdeaProjects/IM-OLAP-Sessions/data/logs/dopan_converted/dibstudent03--2016-09-25--15-56.log.json");

        String qex = "SELECT NON EMPTY {Hierarchize({{[Measures].[Surface du logement (moyenne)], [Measures].[Consomattion chauffage annuelle (min)], [Measures].[Consomattion chauffage annuelle (max)]}})} ON COLUMNS, NON EMPTY {Hierarchize({[Type d'activite du referent.REF_TYPACT_Hierarchie_1].[Type d'actvite].Members})} ON ROWS from [Cube4Chauffage];";


    }

    /**
     * Loads sessions into memory
     */
    private static void loadSessions(String path) {

    }
}
