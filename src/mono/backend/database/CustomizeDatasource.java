/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mono.backend.database;

import mono.GlobalConfig;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.openide.util.Exceptions;

/**
 *
 * @author hungtv
 */
public class CustomizeDatasource implements DataSource, Runnable{
    Logger logger = Logger.getLogger(CustomizeDatasource.class.getSimpleName());
    org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CustomizeDatasource.class.getSimpleName());
    
    public static final String SQL_TEST = "select * from dual";
    public List<Connection> connections = null;
    private int connectionIndex = 0;
    PrintWriter writer;
    
    public CustomizeDatasource(){
    }
    
    @Override
    public synchronized Connection getConnection() throws SQLException {
        connectionIndex++;
        if (connectionIndex == getMaxPoolSize()) {
            connectionIndex = 0;
        }

        if (connections == null) {
            int length = getMaxPoolSize();
            connections = new ArrayList<Connection>(length);
            for (int i = 0; i< length; i++) {
                Connection c = ConnectionPoolManager.getDefaltDataSource().getConnection();
                connections.add(c);
            }
        }

        Connection connection = connections.get(connectionIndex);
        if (testConnection(connection)) {
            return connection;
        } else {
            connections.remove(connectionIndex);
            Connection c = ConnectionPoolManager.getDefaltDataSource().getConnection();
            connections.add(c);
            connection = c;
        }
        
        return connection;
    }

    private boolean testConnection(Connection connection) {
        if (connection == null) {
            return false;
        }
        boolean value = false;

        Statement ps = null;
        try {
            ps = connection.createStatement();
            ps.executeQuery(SQL_TEST);
            value = true;
        } catch (Exception ex) {
            value  = false;
        } finally {
            if (ps != null){
                try {
                    ps.close();
                } catch (Exception ex) {
                }
            }
        }
        return value;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return writer;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.writer = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return logger;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        try {
            if (iface == null) {
                return null;
            }
            return iface.newInstance();
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    private int getMaxPoolSize() {
        return GlobalConfig.getInt("db_pool_size", 10);
    }

    @Override
    public void run() {
        while(true) {
            try{
                Thread.sleep(10);
                prcessReLive();
                Thread.sleep(30000);
            }catch(Exception ex){}
        }
    }
    
    private void prcessReLive(){
        if (connections == null) {
            return ;
        }
        for (Connection connection : connections) {
            boolean connect = testConnection(connection) ;
            log.debug("test db connection=>" + String.valueOf(connect) ); 
        }
    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }
    
}
