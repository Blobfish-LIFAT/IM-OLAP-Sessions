package fr.univ_tours.info.im_olap.data;

import mondrian.olap.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MondrianConfig {
    private static Connection mondrianConnection;
    private static java.sql.Connection jdbcConnection;
    private static String defaultConfigFile = "data/olap.properties";
    private static Properties config = new Properties();

    private static void initConnection() throws ClassNotFoundException, SQLException {
        Class.forName(config.getProperty("driver"));
        Class.forName("mondrian.olap4j.MondrianOlap4jDriver");

        String mondrianString =
                "Provider=" + config.getProperty("Provider")
                        + ";Jdbc=" + config.getProperty("jdbcUrl")
                        + ";Catalog=" + config.getProperty("schemaFile")
                        + ";JdbcDrivers=" + config.getProperty("driver")
                        + ";JdbcUser=" + config.getProperty("jdbcUser")
                        + ";JdbcPassword=" + config.getProperty("jdbcPassword");

        jdbcConnection     = DriverManager.getConnection(config.getProperty("jdbcUrl"), config.getProperty("jdbcUser"), config.getProperty("jdbcPassword"));
        mondrianConnection = mondrian.olap.DriverManager.getConnection(mondrianString, null);

    }

    public static Connection getMondrianConnection() {
        if (mondrianConnection == null) {
            try {
                loadConfig();
                initConnection();
            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }

        }
        return mondrianConnection;
    }

    private static void loadConfig() throws IOException {
        String path = System.getProperty("olapConfig");
        if (path == null) path = defaultConfigFile;

        config.load(new FileInputStream(new File(path)));

    }

}
