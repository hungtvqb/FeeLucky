package mono.backend.database;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class DBPool {

    private static DBPool mPool;

    private BoneCP connectionPoolDB = null;

    private DBPool() {

    }

    public void load(String fileConfig, String dbPoolName) {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (Exception e) {
            return;
        }
        try {

            if (dbPoolName != null && !dbPoolName.isEmpty()) {
                InputStream inputStream = new FileInputStream(fileConfig);
                BoneCPConfig configDB = new BoneCPConfig(inputStream, dbPoolName);
                connectionPoolDB = new BoneCP(configDB);
                inputStream.close();
            }
        } catch (Exception e) {
            System.out.println("Exception at DBPool : " + e.getMessage());
        }
    }

    public Connection getConnectionDB() {
        try {
            return connectionPoolDB.getConnection();
        } catch (SQLException e) {
        }
        return null;
    }

    public static void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
        } catch (Exception ex) {
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
        } catch (Exception e) {
        }
    }

    public static void closeConnection(Connection con) {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
        } catch (Exception e) {
        }
    }

    public static DBPool getInstance() {
        if (mPool == null) {
            mPool = new DBPool();
        }
        return mPool;
    }
}
