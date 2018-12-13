/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package fr.univ_tours.li.jaligon.falseto.Generics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Salim IGUE
 */
public class falseto_params {
    private String connectionString;
    private String provider;
    private String dbms;
    private String jdbc;
    private String schema;
    private String driver;
    private String user;
    private String password;
    private String cubeName;
    private String ncube;
    private String testFilePath;
    /**
     * 
     */
    public falseto_params() {
        this.dbms           =  "mysql";
        this.schema         =  "data/schema.xml";
        this.user           =  "alex";
        this.cubeName       =  "SSB";
        this.ncube          =  "0";
        this.testFilePath   =  "test/";
        this.jdbc           =  "jdbc:mysql://home.alexc.ovh:3306/tp_mondrian";
        this.provider       =  "Mondrian";
        this.password       =  "***REMOVED***";
        this.driver         =  "com.mysql.jdbc.Driver";
        
       this.connectionString="Provider=" + this.provider
                + ";Jdbc=" + this.jdbc
                + ";Catalog=" + this.schema
                + ";JdbcDrivers=" + this.driver
                + ";JdbcUser=" + this.user
                + ";JdbcPassword=" + this.password;
        
    }
    /**
     * Constructeur permettant de charger les paramètres de connexion à partir
     * du fichier olapConnection
     * @param filename nom du fichier
     */
    public falseto_params(String filename){
          File f = new File(filename);
            System.out.println("absolute path: "+f.getAbsolutePath());
        
        try {
            Properties p = new Properties();
          FileInputStream fs = new FileInputStream(Generics.CONNECTION_PROPERTIES);
          p.load(fs);

            provider = p.getProperty("provider");
            dbms = p.getProperty("dbms");
            jdbc = p.getProperty("jdbc");
            schema = p.getProperty("schema");
            driver = p.getProperty("driver");
            user = p.getProperty("user");
            password = p.getProperty("password");
            cubeName = p.getProperty("cubeName");
            ncube=p.getProperty("ncube");
            connectionString = "Provider=" + provider
                    + ";Jdbc=" + jdbc
                    + ";Catalog=" + schema
                    + ";JdbcDrivers=" + driver
                    + ";JdbcUser=" + user
                    + ";JdbcPassword=" + password;
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("OLAP connection failed. Please check olapConnection.properties exists or given information are correct.");
            //System.exit(2);
        }
    }
    
    public String getNcube() {
        return ncube;
    }
    
    public String getTestFilePath() {
        return testFilePath;
    }

    public String getConnectionString() {
        return connectionString;
    }
       
    public String getProvider() {
        return provider;
    }
    
    public String getDbms() {
        return dbms;
    }
    
    public String getJdbc() {
        return jdbc;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public String getDriver() {
        return driver;
    }
    
    public String getUser() {
        return user;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getCubeName() {
        return cubeName;
    }
    
    
    
    
}
