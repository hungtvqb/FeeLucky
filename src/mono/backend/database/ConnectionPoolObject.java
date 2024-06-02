/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.backend.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.timesten.jdbc.ObservableConnectionDS;
import mono.util.TextSecurity;
import mono.util.MyLog;
import java.util.Properties;
import java.util.Vector;
import javax.sql.DataSource;

/**
 *
 * @author TuanNS3_S
 * @since 14/05/2013
 */
public class ConnectionPoolObject {
    public static final String URL_PREFIX_TIMESTEND = "jdbc:timesten";
    public static final String URL_PREFIX_ORACEL = "jdbc:oracle";
    
    private Properties pro;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataSource createPoolConnection() throws Exception {
        String url = getProperty("connection");
        if (url == null || "".equals(url)) {
            throw new Exception("ERROR in property: connection is null");
        }
        
        if (url.startsWith(URL_PREFIX_TIMESTEND)) {
            return createDatasource();
        }
        
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        
        // <editor-fold defaultstate="collapsed" desc="Cau hinh bat buoc">
        //
        if (getProperty("driver") != null && !getProperty("driver").equals("")) {
            try {
                cpds.setDriverClass(getProperty("driver"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: driver");
            }
        } else {
            throw new Exception("ERROR in property: driver is null");
        }
        //
        if (getProperty("connection") != null && !getProperty("connection").equals("")) {
            try {
                cpds.setJdbcUrl(getProperty("connection"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: connection");
            }
        } else {
            throw new Exception("ERROR in property: connection is null");
        }
        //
        if (getProperty("username") != null && !getProperty("username").equals("")) {
            try {
                cpds.setUser(getProperty("username"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: username");
            }
        } else {
            throw new Exception("ERROR in property: username is null");
        }
        //
        if (getProperty("password") != null && !getProperty("password").equals("")) {
            try {
                String password = getProperty("password");
                cpds.setPassword(password);
            } catch (Exception ex) {
                throw new Exception("ERROR in property: password");
            }
        } else {
            throw new Exception("ERROR in property: password is null");
        }
        //set readtimeout
        if (getProperty("readTimeout") != null) {
            try {
                int readTimeout = Integer.parseInt(getProperty("readTimeout"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: readTimeout must be an Integer");
            }
            Properties prop = new Properties();
            prop.setProperty("oracle.jdbc.ReadTimeout", getProperty("readTimeout"));
            prop.setProperty("user", getProperty("username"));
            prop.setProperty("password", getProperty("password"));
            cpds.setProperties(prop);
        } else {
            throw new Exception("ERROR in property: readTimeout is null");
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Cau hinh khong bat buoc">
        //
        if (getProperty("checkoutTimeout") != null) {
            try {
                cpds.setCheckoutTimeout(Integer.parseInt(getProperty("checkoutTimeout")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: checkoutTimeout");
            }
        }
        //
        if (getProperty("acquireIncrement") != null) {
            try {
                cpds.setAcquireIncrement(Integer.parseInt(getProperty("acquireIncrement")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: acquireIncrement");
            }
        }
        if (getProperty("acquireRetryAttempts") != null) {
            try {
                cpds.setAcquireRetryAttempts(Integer.parseInt(getProperty("acquireRetryAttempts")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: acquireRetryAttempts");
            }
        }
        if (getProperty("acquireRetryDelay") != null) {
            try {
                cpds.setAcquireRetryDelay(Integer.parseInt(getProperty("acquireRetryDelay")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: acquireRetryDelay");
            }
        }
        if (getProperty("autoCommitOnClose") != null) {
            try {
                cpds.setAutoCommitOnClose(Boolean.parseBoolean(getProperty("autoCommitOnClose")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: autoCommitOnClose");
            }
        }
        if (getProperty("automaticTestTable") != null) {
            try {
                cpds.setAutomaticTestTable(getProperty("automaticTestTable"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: automaticTestTable");
            }
        }
        if (getProperty("breakAfterAcquireFailure") != null) {
            try {
                cpds.setBreakAfterAcquireFailure(Boolean.parseBoolean("breakAfterAcquireFailure"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: breakAfterAcquireFailure");
            }
        }
        if (getProperty("checkoutTimeout") != null) {
            try {
                cpds.setCheckoutTimeout(Integer.parseInt(getProperty("checkoutTimeout")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: checkoutTimeout");
            }
        }
        if (getProperty("connectionCustomizerClassName") != null) {
            try {
                cpds.setConnectionCustomizerClassName(getProperty("connectionCustomizerClassName"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: connectionCustomizerClassName");
            }
        }
        if (getProperty("connectionTesterClassName") != null) {
            try {
                cpds.setConnectionTesterClassName(getProperty("connectionTesterClassName"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: connectionTesterClassName");
            }
        }
        if (getProperty("dataSourceName") != null) {
            try {
                cpds.setDataSourceName(getProperty("dataSourceName"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: dataSourceName");
            }
        }
        if (getProperty("debugUnreturnedConnectionStackTraces") != null) {
            try {
                cpds.setDebugUnreturnedConnectionStackTraces(Boolean.parseBoolean(getProperty("debugUnreturnedConnectionStackTraces")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: debugUnreturnedConnectionStackTraces");
            }
        }
        if (getProperty("factoryClassLocation") != null) {
            try {
                cpds.setFactoryClassLocation(getProperty("factoryClassLocation"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: factoryClassLocation");
            }
        }
        if (getProperty("forceIgnoreUnresolvedTransactions") != null) {
            try {
                cpds.setForceIgnoreUnresolvedTransactions(Boolean.parseBoolean(getProperty("forceIgnoreUnresolvedTransactions")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: forceIgnoreUnresolvedTransactions");
            }
        }
        if (getProperty("idleConnectionTestPeriod") != null) {
            try {
                cpds.setIdleConnectionTestPeriod(Integer.parseInt(getProperty("idleConnectionTestPeriod")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: idleConnectionTestPeriod");
            }
        }
        if (getProperty("initialPoolSize") != null) {
            try {
                cpds.setInitialPoolSize(Integer.parseInt(getProperty("initialPoolSize")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: initialPoolSize");
            }
        }
        if (getProperty("maxAdministrativeTaskTime") != null) {
            try {
                cpds.setMaxAdministrativeTaskTime(Integer.parseInt(getProperty("maxAdministrativeTaskTime")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxAdministrativeTaskTime");
            }
        }
        if (getProperty("maxConnectionAge") != null) {
            try {
                cpds.setMaxConnectionAge(Integer.parseInt(getProperty("maxConnectionAge")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxConnectionAge");
            }
        }
        if (getProperty("maxIdleTime") != null) {
            try {
                cpds.setMaxIdleTime(Integer.parseInt(getProperty("maxIdleTime")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxIdleTime");
            }
        }
        if (getProperty("maxIdleTimeExcessConnections") != null) {
            try {
                cpds.setMaxIdleTimeExcessConnections(Integer.parseInt(getProperty("maxIdleTimeExcessConnections")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxIdleTimeExcessConnections");
            }
        }
        if (getProperty("maxPoolSize") != null) {
            try {
                cpds.setMaxPoolSize(Integer.parseInt(getProperty("maxPoolSize")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxPoolSize");
            }
        }
        if (getProperty("maxStatements") != null) {
            try {
                cpds.setMaxStatements(Integer.parseInt(getProperty("maxStatements")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxStatements");
            }
        }
        if (getProperty("maxStatementsPerConnection") != null) {
            try {
                cpds.setMaxStatementsPerConnection(Integer.parseInt(getProperty("maxStatementsPerConnection")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxStatementsPerConnection");
            }
        }
        if (getProperty("minPoolSize") != null) {
            try {
                cpds.setMinPoolSize(Integer.parseInt(getProperty("minPoolSize")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: minPoolSize");
            }
        }
        if (getProperty("numHelperThreads") != null) {
            try {
                cpds.setNumHelperThreads(Integer.parseInt(getProperty("numHelperThreads")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: numHelperThreads");
            }
        }
        if (getProperty("overrideDefaultUser") != null) {
            try {
                cpds.setOverrideDefaultUser(getProperty("overrideDefaultUser"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: overrideDefaultUser");
            }
        }
        if (getProperty("overrideDefaultPassword") != null) {
            try {
                cpds.setOverrideDefaultPassword(getProperty("overrideDefaultPassword"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: overrideDefaultPassword");
            }
        }
        if (getProperty("preferredTestQuery") != null) {
            try {
                cpds.setPreferredTestQuery(getProperty("preferredTestQuery"));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: preferredTestQuery");
            }
        }
        if (getProperty("propertyCycle") != null) {
            try {
                cpds.setPropertyCycle(Integer.parseInt(getProperty("propertyCycle")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: propertyCycle");
            }
        }
        if (getProperty("statementCacheNumDeferredCloseThreads") != null) {
            try {
                cpds.setStatementCacheNumDeferredCloseThreads(Integer.parseInt(getProperty("statementCacheNumDeferredCloseThreads")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: statementCacheNumDeferredCloseThreads");
            }
        }
        if (getProperty("testConnectionOnCheckin") != null) {
            try {
                cpds.setTestConnectionOnCheckin(Boolean.parseBoolean(getProperty("testConnectionOnCheckin")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: testConnectionOnCheckin");
            }
        }
        if (getProperty("testConnectionOnCheckout") != null) {
            try {
                cpds.setTestConnectionOnCheckout(Boolean.parseBoolean(getProperty("testConnectionOnCheckout")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: testConnectionOnCheckout");
            }

        }
        if (getProperty("unreturnedConnectionTimeout") != null) {
            try {
                cpds.setUnreturnedConnectionTimeout(Integer.parseInt(getProperty("unreturnedConnectionTimeout")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: unreturnedConnectionTimeout");
            }
        }
        // </editor-fold>

        return cpds;
    }
    
    private DataSource createDatasource() throws Exception{
//        ObservableConnectionDS cpds = new ObservableConnectionDS();
        ObservableConnectionDS cpds = new ObservableConnectionDSCustomize();
        cpds.setUrl(getProperty("connection"));
        cpds.setUser(getProperty("username"));
        String password = getProperty("password");
        cpds.setPassword(password);
        password = getProperty("oracle_password");
        cpds.setOraclePassword(password);
        if (getProperty("initialPoolSize") != null) {
            try {
                cpds.setInitialPoolSize(Integer.parseInt(getProperty("initialPoolSize")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: initialPoolSize");
            }
        }
        
        if (getProperty("maxIdleTime") != null) {
            try {
                cpds.setMaxIdleTime(Integer.parseInt(getProperty("maxIdleTime")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxIdleTime");
            }
        }
        
        if (getProperty("maxPoolSize") != null) {
            try {
                cpds.setMaxPoolSize(Integer.parseInt(getProperty("maxPoolSize")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: maxPoolSize");
            }
        }
      
        if (getProperty("minPoolSize") != null) {
            try {
                cpds.setMinPoolSize(Integer.parseInt(getProperty("minPoolSize")));
            } catch (Exception ex) {
                throw new Exception("ERROR in property: minPoolSize");
            }
        }
        return cpds;
    }
    
    
    public ConnectionPoolObject(Properties pro) {
        this.pro = pro;
    }

    public Properties getPro() {
        return pro;
    }

    public void setPro(Properties pro) {
        this.pro = pro;
    }

    public String getProperty(String key) {
        return this.pro.getProperty(key);
    }

    public void setProperty(String key, String value) {
        this.pro.setProperty(key, value);
    }
    
     public Vector<String> getConnnectionString() {
        Vector<String> vector = new Vector<String>();
        String url = getProperty("connection");
        String username = getProperty("username");
        String password = getPassword();
        
        vector.add(url);
        vector.add(username);
        vector.add(password);
        return vector;
    }
    
    private String getPassword() {
        String password = getProperty("password");
        String tmp_password = TextSecurity.getInstance().Decrypt(password);
        if (tmp_password != null && !tmp_password.isEmpty()) {
            password = tmp_password;
        }
        return password;
    }
}
