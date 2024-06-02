/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.backend.database;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @edit hungtv
 */
public class ConnectionPoolManager {

    private static final Logger logger = Logger.getLogger(ConnectionPoolManager.class);
    private static Map<String, DataSource> connectionMap = null;
    private static Map<String, ConnectionPoolObject> connectionString = null;
    //Time out query
    public static int QUERY_TIMEOUT = 300;
    public static int TIME_BREAK = 0;
    public static int TIME_BREAK_DELETE_RECORD_TIMEOUT = 0;
    
    public static String DATABASE_ORACLE_DEFAULT = "dbapp";
    public static String DATABASE_TIMESTEN = "dbtimestend";
    
    /**
     * *
     *
     * @param dbConfigXml
     */
    public static void loadConfig(String dbConfigXml){

        if (connectionMap != null) {
            logger.info("already config");
            return;
        }
        
        connectionMap = new HashMap();
        connectionString = new HashMap();
        try {
            String content = readFile(dbConfigXml);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            Document document = db.parse(inputStream);

            Element root = document.getDocumentElement();
            NodeList listTimeOut = root.getElementsByTagName("timeout-config");
            for (int i = 0; i < listTimeOut.getLength(); i++) {
                Element ele = (Element) listTimeOut.item(i);
                NodeList listProperty = ele.getElementsByTagName("property");
                for (int j = 0; j < listProperty.getLength(); j++) {
                    Element elePro = (Element) listProperty.item(j);
                    String name = elePro.getAttribute("name");
                    String value = elePro.getTextContent();
                    switch (name) {
                        case "queryDbTimeout":
                            try {
                                ConnectionPoolManager.QUERY_TIMEOUT = Integer.parseInt(value);
                            } catch (Exception e) {
                                ConnectionPoolManager.QUERY_TIMEOUT = 60;
                            }   break;
                        case "timeBreak":
                            try {
                                ConnectionPoolManager.TIME_BREAK = Integer.parseInt(value);
                            } catch (Exception e) {
                                ConnectionPoolManager.TIME_BREAK = 90000;
                            }   break;
                        case "timeBreakDeleteRecordTimeOut":
                            try {
                                ConnectionPoolManager.TIME_BREAK_DELETE_RECORD_TIMEOUT = Integer.parseInt(value);
                            } catch (Exception e) {
                                ConnectionPoolManager.TIME_BREAK_DELETE_RECORD_TIMEOUT = 120000;
                        }   break;
                    }
                }
            }

            //
            NodeList list = root.getElementsByTagName("named-config");
            if (list.getLength() < 1) {
                throw new Exception("No named-config");
            }
            //
            for (int i = 0; i < list.getLength(); ++i) {
                Element ele = (Element) list.item(i);
                String dbName = ele.getAttribute("name");

                NodeList listProperty = ele.getElementsByTagName("property");
                if (listProperty.getLength() < 1) {
                    throw new Exception("No property");
                }
                //doc list
                Properties pro = new Properties();
                for (int j = 0; j < listProperty.getLength(); j++) {
                    Element elePro = (Element) listProperty.item(j);
                    String name = elePro.getAttribute("name");
                    String value = elePro.getTextContent();
                    pro.setProperty(name, value);
                }

                // init pool Database
                ConnectionPoolObject obj = new ConnectionPoolObject(pro);
                obj.setId(dbName);
                try {
                    connectionString.put(dbName, obj);
                    connectionMap.put(dbName, obj.createPoolConnection());
                } catch (Exception e) {
                    logger.error("==> ERROR WHEN INIT DATABASE ID=" + dbName, e);
                }
            }
        } catch (Exception ex) {
            logger.error("==> ERROR INIT DATABASE LIST", ex);
        }

    }
    
    private static String readFile(String filePath) {
        StringBuilder sb = new StringBuilder("");
        BufferedReader br = null;

        try {

            String sCurrentLine;

            br = new BufferedReader(new FileReader(filePath));

            while ((sCurrentLine = br.readLine()) != null) {
                sb.append(sCurrentLine);
            }

        } catch (IOException e) {
            logger.error("readFile: " + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    logger.error("readFile closed error: " + ex.getMessage());
                    
                }
            }
        }

        return sb.toString();
    }

    public static void reLoadConfig(String dbConfigXml) throws Exception {
        connectionMap.clear();
        connectionString.clear();
        try {

            String content = readFile(dbConfigXml);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            Document document = db.parse(inputStream);

            Element root = document.getDocumentElement();
            NodeList listTimeOut = root.getElementsByTagName("timeout-config");
            for (int i = 0; i < listTimeOut.getLength(); i++) {
                Element ele = (Element) listTimeOut.item(i);
                NodeList listProperty = ele.getElementsByTagName("property");
                for (int j = 0; j < listProperty.getLength(); j++) {
                    Element elePro = (Element) listProperty.item(j);
                    String name = elePro.getAttribute("name");
                    String value = elePro.getTextContent();
                    switch (name) {
                        case "queryDbTimeout":
                            try {
                                ConnectionPoolManager.QUERY_TIMEOUT = Integer.parseInt(value);
                            } catch (Exception e) {
                                ConnectionPoolManager.QUERY_TIMEOUT = 60;
                            }   break;
                        case "timeBreak":
                            try {
                                ConnectionPoolManager.TIME_BREAK = Integer.parseInt(value);
                            } catch (Exception e) {
                                ConnectionPoolManager.TIME_BREAK = 90000;
                            }   break;
                        case "timeBreakDeleteRecordTimeOut":
                            try {
                                ConnectionPoolManager.TIME_BREAK_DELETE_RECORD_TIMEOUT = Integer.parseInt(value);
                            } catch (Exception e) {
                                ConnectionPoolManager.TIME_BREAK_DELETE_RECORD_TIMEOUT = 120000;
                        }   break;
                    }
                }
            }

            //
            NodeList list = root.getElementsByTagName("named-config");
            if (list.getLength() < 1) {
                throw new Exception("No named-config");
            }
            //
            for (int i = 0; i < list.getLength(); ++i) {
                Element ele = (Element) list.item(i);
                String dbName = ele.getAttribute("name");

                NodeList listProperty = ele.getElementsByTagName("property");
                if (listProperty.getLength() < 1) {
                    throw new Exception("No property");
                }
                //doc list
                Properties pro = new Properties();
                for (int j = 0; j < listProperty.getLength(); j++) {
                    Element elePro = (Element) listProperty.item(j);
                    String name = elePro.getAttribute("name");
                    String value = elePro.getTextContent();
                    pro.setProperty(name, value);
                }

                // init pool Database
                ConnectionPoolObject obj = new ConnectionPoolObject(pro);
                obj.setId(dbName);
                try {
                    connectionString.put(dbName, obj);
                    connectionMap.put(dbName, obj.createPoolConnection());
                } catch (Exception e) {
                    logger.error("==> ERROR WHEN INIT DATABASE ID=" + dbName, e);
                }
            }
        } catch (Exception ex) {
            logger.error("==> ERROR INIT DATABASE LIST", ex);
            throw ex;
        }
    }

    /**
     * @param id
     * @return **
     *
     */
    public static synchronized Connection getConnection(String id) {
        Connection conn = null;
        try {
            if (connectionMap.containsKey(id)) {
                conn = ((DataSource) connectionMap.get(id)).getConnection();
            } else {
                logger.warn("PooledDataSource don't have ID: " + id);
            }
        } catch (SQLException ex) {
            logger.error("Can't connect DB, with ID: " + id, ex);
        }

        return conn;
    }

    public static Map<String, DataSource> getConnectionMap() {
        return connectionMap;
    }

    public static void setConnectionMap(HashMap<String, DataSource> connectionMap) {
        ConnectionPoolManager.connectionMap = connectionMap;
    }

    public static List<String> getListDatabaseName() {

        List<String> listTemp = new ArrayList<>();
        List<String> listReturn = new ArrayList<>();
        for (Map.Entry<String, DataSource> entry : connectionMap.entrySet()) {
            String databaseName = entry.getKey();
            listTemp.add(databaseName);
        }
        //dao nguoc chuoi cho dung thu tu
        for (int i = listTemp.size(); i > 0; i--) {
            listReturn.add(listTemp.get(i - 1));
        }
        return listReturn;
    }
    
    public static DataSource getDataSource(String databaseId) {
        return connectionMap.get(databaseId);
    }

    public static Connection getDefaultConnection(){
       return getConnection(DATABASE_ORACLE_DEFAULT);
    }
    
    public static Connection getTimestenConnection(){
       return getConnection(DATABASE_TIMESTEN);
    }

    public static DataSource getDefaltDataSource() {
        return connectionMap.get(DATABASE_ORACLE_DEFAULT);
    }
    
    public static Vector<String> getConnnectionString(){
        ConnectionPoolObject poolObject = connectionString.get(DATABASE_ORACLE_DEFAULT);
        if (poolObject == null) {
            return null;
        }
        
        return poolObject.getConnnectionString();
    }
    
    
    static DatabaseConnectionPool databaseConnectionPool;
    public static void setDatabaseConnectionPool(DatabaseConnectionPool databaseConnectionPool){
        ConnectionPoolManager.databaseConnectionPool = databaseConnectionPool;
    }
    
    public synchronized static Connection getOriginalConnection(){
       return databaseConnectionPool.getConnection().getDatabaseConnection();
    }
    
    public synchronized static void closeOriginalConnection(Connection cc){
    }

   
}
