/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mono.backend.database;

import com.timesten.jdbc.ObservableConnectionDS;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hungtv
 */
public class ObservableConnectionDSCustomize extends ObservableConnectionDS {
    
    public static final String SQL_TEST = "select * from dual";
    public List<Connection> connections = null;
    private int connectionIndex = 0;
    
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
                Connection c = super.getConnection();
                connections.add(c);
            }
        }

        Connection connection = connections.get(connectionIndex);
        if (testConnection(connection)) {
            return connection;
        } else {
            connections.remove(connectionIndex);
            Connection c = super.getConnection();
            connections.add(c);
            connection = c;
        }
        
        return connection;
    }

    @Override
    public void setMaxPoolSize(int i) {
        super.setMaxPoolSize(i);
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

    
}
