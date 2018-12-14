package fr.univ_tours.li.jaligon.falseto.Generics;

import java.util.HashSet;

import mondrian.olap.Cube;
import mondrian.olap.Dimension;
import mondrian.olap.DriverManager;
import mondrian.olap.Hierarchy;



public class Connection {
    /**
     * attribut permettant à un utilisateur de rentrer ses propres paramètres
     */
    private falseto_params fp;
    
    public static String connectionString;
    private static String provider;
    private static String dbms;
    private static String jdbc;
    private static String schema;
    private static String driver;
    private static String user;
    private static String password;
    private static String cubeName;
    private static mondrian.olap.Connection cnx;
    private static Cube cube;
    private static String ncube = "0";
    private static HashSet<Hierarchy> cubeHiers = new HashSet<Hierarchy>();
    
    /**
     * Set the parameters found in the class falseto_params.
     * Open a database connection and a cube connection with theses parameters.
     * @throws Exception
     */
    public Connection() throws Exception {
        fp= new falseto_params();
        
        try {
            provider    = fp.getProvider();
            dbms        = fp.getDbms();
            jdbc        = fp.getJdbc();
            schema      = fp.getSchema();
            driver      = fp.getDriver();
            user        = fp.getUser();
            password    = fp.getPassword();
            cubeName    = fp.getCubeName();
            ncube       = fp.getNcube();
            connectionString = fp.getConnectionString();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("OLAP connection failed. Please check in the class falseto_params if the given informations are correct.");
        }
    }
    
    
    
    /**
     * Open a database connection and a cube connection with the name of the file necessery for the connection.
     * @param filename the connection file name.
     */
    public Connection(String filename) {
        this.fp= new falseto_params(filename);

            try {
                provider    = fp.getProvider();
                dbms        = fp.getDbms();
                jdbc        = fp.getJdbc();
                schema      = fp.getSchema();
                driver      = fp.getDriver();
                user        = fp.getUser();
                password    = fp.getPassword();
                cubeName    = fp.getCubeName();
                ncube       = fp.getNcube();
                connectionString = fp.getConnectionString();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("OLAP connection failed. Please check in the class falseto_params if the given informations are correct.");
            }

        
    }

    /**Open a new Mondrian connection*/
    public void open()
    {
        System.out.println(connectionString);
        System.out.println("Connecting ...");
        cnx = DriverManager.getConnection(connectionString, null);
        System.out.println(cnx.getConnectString());
        System.out.println("Connected.");
        cube = cnx.getSchema().getCubes()[Integer.valueOf(ncube)];
        getHierarchy();
    }
    /**
     * Found the hierarchy of the cube.
     */
    private void getHierarchy() {
        Dimension[] cubeDims = cube.getDimensions();
        
        for (int i = 0; i < cubeDims.length; i++) {
            Hierarchy[] tmpH = cubeDims[i].getHierarchies();
            for (int j = 0; j < tmpH.length; j++) {
                cubeHiers.add(tmpH[j]);
            }
        }
    }
    
    /**
     *
     * @return the hierarchies of the cube.
     */
    public static HashSet<Hierarchy> getCubeHiers() {
        return cubeHiers;
    }
    
    /**
     *
     * @return the mondrian connection.
     */
    public static mondrian.olap.Connection getCnx() {
        return cnx;
    }
    
    /**
     *
     * @return the mondrian cube.
     */
    public static Cube getCube() {
        return cube;
    }
    
    /**
     *
     * @return the cube name.
     */
    public static String getCubeName() {
        return cubeName;
    }
    
    /**
     *
     * @return the dbms name.
     */
    public static String getDbms() {
        return dbms;
    }
    
    /**
     *
     * @return the driver name.
     */
    public static String getDriver() {
        return driver;
    }
    
    
    /**
     *
     * @return the jdbc name.
     */
    public static String getJdbc() {
        return jdbc;
    }
    
    /**
     *
     * @return the password used to dbms connection.
     */
    public static String getPassword() {
        return password;
    }
    
    /**
     *
     * @return the provider for the olap cube.
     */
    public static String getProvider() {
        return provider;
    }
    
    /**
     *
     * @return the schema name.
     */
    public static String getSchema() {
        return schema;
    }
    
    /**
     *
     * @return the user name used to dbms connection.
     */
    public static String getUser() {
        return user;
    }
    
    public  void close(){
        cnx.close();
    }
}
