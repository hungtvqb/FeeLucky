/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.backend.database;

import mono.util.MyLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.sql.DataSource;

/**
 *
 * @author hoand
 */
public class DirectConnection implements Runnable {

    public static final int CONNECTION_TIMEOUT = 10000; //ms
    protected final static int CHECK_TIME = 30000;//20 giay
    protected boolean isRunning;
    protected Connection mConnection = null;
    protected String mId;
    protected boolean mIsConnected;
    private long mLastConnect;
    private ConnectionStrManager mConnManager;

    private DataSource dataSource = null;

    public DirectConnection(String id, String poolFile) {
        mId = id;
        isRunning = false;
        mIsConnected = false;
        mLastConnect = 0;
        mConnManager = new ConnectionStrManager(poolFile);

        try {
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        } catch (Exception e) {
            MyLog.Error("DBThread " + mId + "ERROR" + e.getMessage());
            System.exit(0);
        }
    }

    public DirectConnection(String id, Vector<String> mConnectionList) {
        mId = id;
        isRunning = false;
        mIsConnected = false;
        mLastConnect = 0;
        mConnManager = new ConnectionStrManager(mConnectionList);

        try {
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
        } catch (Exception e) {
            MyLog.Error("DBThread " + mId + "ERROR" + e.getMessage());
            System.exit(0);
        }
    }

    public DirectConnection(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DirectConnection(Connection connection) {
        this.mConnection = connection;
    }

    protected void init() {
        if (System.currentTimeMillis() - mLastConnect <= CONNECTION_TIMEOUT) {
            MyLog.Error("Wait for timeout .... ");
            return;
        }

        mLastConnect = System.currentTimeMillis();

        String conn = mConnManager.getConnectionStr();
        if (conn != null) {
            MyLog.Infor("DBThread " + mId + "Connecting to '" + conn + "'");

            if (mConnManager.getUser() != null
                    && mConnManager.getPassword() != null) {
                mConnection = connectToOraServer(mConnManager.getUrl(), mConnManager.getUser(), mConnManager.getPassword());
            } else {
                mConnection = connectToOraServer(conn);
            }
            if (mConnection != null) {
                mIsConnected = true;
                MyLog.Infor("DBThread " + mId + "CONNECTED."); // to '" + conn + "'");
            } else {
                MyLog.Error("DBThread " + mId + "CAN NOT Connect to DB"); //'" + conn + "'");
            }
        } else {
            MyLog.Infor("DBThread " + mId + "get Connection String Failed");
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        mIsConnected = dataSource != null;
    }

    public boolean isConnected() {
        if (dataSource != null) {
            return true;
        }
        return mIsConnected;
    }

    private void reconnect() {
        /* neu la datasource thi ko can reconnect*/
        if (dataSource != null) {
            return;
        }
        mIsConnected = false;
        try {
            MyLog.Debug("DBThread " + mId + "Wait 5 secs before RECONNECT to DB ....");
            Thread.sleep(5000); // wait 5s before reconnect
            MyLog.Debug("DBThread " + mId + "Reconnecting ...");
            mConnection.close();
        } catch (Exception e) {
        } finally {
            mConnection = null;
            init();
        }
    }

    private String getThreadName() {
        return "DBThread " + mId + ": ";
    }

    protected Connection connectToOraServer(String conn) {
        try {
            Connection co = DriverManager.getConnection(conn);
            MyLog.Infor("DBThread " + mId + "CONNECTED.");
            return co;
        } catch (Exception e) {
            MyLog.Error("DBThread " + mId + "Error connect to Oracle DB server" + e.getMessage());
            return null;
        }
    }

    protected Connection connectToOraServer(String conn, String user, String pass) {
        try {
            Connection co = DriverManager.getConnection(conn, user, pass);

            MyLog.Infor("DBThread " + mId + "CONNECTED.");
            return co;
        } catch (Exception e) {
            MyLog.Error("DBThread " + mId + "Error connect to Oracle DB server" + e.getMessage());
            return null;
        }
    }

    public void startConnect() {
        init();

        isRunning = true;
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        while (isRunning && dataSource == null) {
            try {
                Statement s = null;
                try {
                    String strSQL = "select 1 from dual";
                    s = mConnection.createStatement();
                    s.setQueryTimeout(30); // sec
                    java.sql.ResultSet r = s.executeQuery(strSQL);
                    r.next();
                    s.close();
                    MyLog.Debug("DBThread " + mId + "Check connection OK, sleep (s): " + (CHECK_TIME / 1000));
                } catch (Exception ex) {
                    MyLog.Error("DBThread " + mId + "Check connection ERROR" + ex.getMessage() + ", RECONNECTING .... ");
                    reconnect();
                }
                Thread.sleep(CHECK_TIME);
            } catch (Exception e) {
                MyLog.Error("DBThread " + mId + "ERROR" + e.getMessage());
                MyLog.Error(e);
            }
        }
    }

    public CallableStatement getStatement(String strSql) {
        try {
            return getDatabaseConnection().prepareCall(strSql);
        } catch (Exception ex) {
            MyLog.Error("DBThread " + mId + "ERROR" + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
            return null;
        }
    }

    public Connection getDatabaseConnection() {
        if (dataSource == null) {
            return mConnection;
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            MyLog.Error(ex);
        }
        return null;
    }

    public void closeConnection(Connection con) {
        if (dataSource == null || con == null) {
            return;
        }

        try {
            con.close();
        } catch (Exception ex) {
            MyLog.Error(ex);
        }
    }

    public boolean excuteUpdate(String strSQL) {
        MyLog.Debug(getThreadName() + "start execute'" + strSQL + "'");
        boolean result = false;
        CallableStatement s = null;
        long start = System.currentTimeMillis();
        int count = 0;
        Connection conn = null;
        try {
            conn = getDatabaseConnection();
            s = conn.prepareCall(strSQL);
            // Set timeout
            s.setQueryTimeout(30);
            count = s.executeUpdate();
            result = true;
            s.close();
        } catch (SQLException e) {
            MyLog.Error(getThreadName() + "ERROR" + e.getMessage());
            MyLog.Error(e);
            result = false;
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR" + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
            result = false;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }
        MyLog.Infor(getThreadName() + " Finish execute '" + strSQL + "'" + result + " ("
                + (System.currentTimeMillis() - start) + " ms) (Count" + count + ")");

        return result;
    }

    /**
     * Get additional date for Service
     *
     * @param msisdn
     * @param subType
     * @param subService
     * @param service
     * @param cmd
     * @param cat
     * @param shortCode
     * @param cmdType
     * @return
     */
    public long getAdditonalRegisterDate(String msisdn, int subType, String subService, String service,
            String cmd, String cat, String shortCode, int cmdType) {
        String log = "getAdditonalRegisterDate (MSISDN: " + msisdn
                + "; SubType: " + subType
                + "; Service: " + service
                + "; SubService: " + subService
                + "; ShortCode: " + shortCode
                + "; CMD: " + cmd
                + "; CAT: " + cat
                + "; CMDType: " + cmdType
                + ")";
        MyLog.Debug(getThreadName() + "start " + log);
        int result = 30;
        CallableStatement s = null;
        long start = System.currentTimeMillis();
        Connection conn = null;
        try {
            String strSQL = "{ ?=call fn_get_add_day_monfee_reg_sms(?, ?, ?, ?, ?, ?, ?, ?) }";
            conn = getDatabaseConnection();
            s = conn.prepareCall(strSQL);
            s.setQueryTimeout(30);
            s.registerOutParameter(1, java.sql.Types.VARCHAR);
            s.setString(2, msisdn);
            s.setInt(3, subType);
            s.setString(4, cmd);
            s.setString(5, cat);
            s.setString(6, subService);
            s.setString(7, service);
            s.setString(8, shortCode);
            s.setInt(9, cmdType);

            s.executeUpdate();

            result = s.getInt(1);
        } catch (SQLException e) {
            MyLog.Error(getThreadName() + "ERROR " + e.getMessage());
            MyLog.Error(e);
            result = 30;
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR " + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
            result = 30;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }

        MyLog.Infor(getThreadName() + "Finish " + log + " -> " + result + "("
                + (System.currentTimeMillis() - start) + " ms)");

        return result;
    }

    public long getAdditonalRegisterDateWS(String msisdn, int subType, String subService, String service) {
        String log = "getAdditonalRegisterDate (MSISDN: " + msisdn
                + "; SubType: " + subType
                + "; Service: " + service
                + "; SubService: " + subService
                + ")";
        MyLog.Debug(getThreadName() + "start getAdditonalRegisterDateWS" + log);
        int result = 30;
        CallableStatement s = null;
        long start = System.currentTimeMillis();
        Connection conn = null;
        try {
            String strSQL = "{ ?=call fn_get_add_day_register_ws(?, ?, ?, ?) }";
            conn = getDatabaseConnection();
            s = conn.prepareCall(strSQL);
            s.setQueryTimeout(30); // sec
            s.registerOutParameter(1, java.sql.Types.VARCHAR);
            s.setString(2, msisdn);
            s.setInt(3, subType);
            s.setString(4, subService);
            s.setString(5, service);

            s.executeUpdate();

            result = s.getInt(1);
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR " + ex.getMessage());
            MyLog.Error(ex);
            result = 30;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }

        MyLog.Infor(getThreadName() + "Finish getAdditonalRegisterDateWS" + log + " -> " + result + "("
                + (System.currentTimeMillis() - start) + " ms)");

        return result;
    }

    /**
     * Get additional day for charge monfee success
     *
     * @param msisdn
     * @param subType
     * @param subService
     * @param service
     * @return
     */
    public long getAdditonalDateMonfee(String msisdn, int subType, String subService, String service) {
        MyLog.Debug(getThreadName() + "start getAdditonalDateMonfee for '" + subType + "' - " + subService + " - " + service);
        int result = 30;
        CallableStatement s = null;
        long start = System.currentTimeMillis();
        Connection conn = null;
        try {
            String strSQL = "{ ?=call fn_get_add_day_charge_monfee(?, ?, ?, ?) }";
            conn = getDatabaseConnection();
            s = conn.prepareCall(strSQL);
            // Set timeout
            s.setQueryTimeout(30); // sec
            s.registerOutParameter(1, java.sql.Types.VARCHAR);
            s.setString(2, msisdn);
            s.setInt(3, subType);
            s.setString(4, subService);
            s.setString(5, service);

            s.executeUpdate();
            result = s.getInt(1);
        } catch (SQLException e) {
            MyLog.Error(getThreadName() + "ERROR " + e.getMessage());
            MyLog.Error(e);
            result = 30;
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR " + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
            result = 30;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }

        MyLog.Infor(getThreadName() + "Finish getAdditonalDateMonfee for'" + subType + " - " + subService + " - " + service + " -> " + result + "("
                + (System.currentTimeMillis() - start) + " ms)");

        return result;
    }

    private long getTimeStamp(ResultSet r, String column, long defaultValue) {
        long result = defaultValue;
        try {
            if (r.getTimestamp(column) != null) {
                result = r.getTimestamp(column).getTime();
            }
        } catch (Exception ex) {
            MyLog.Debug(getThreadName() + " get Timestamp for " + column + " failed, use default: " + defaultValue);
            result = defaultValue;
        }

        return result;
    }

    public int getLuckyRoullet(String msisdn, String transid, String choice) {
        int status = -1;
        MyLog.Debug(getThreadName() + "start pr_lucky_routlet for " + msisdn + ", transid:" + transid);
        CallableStatement s = null;
        long start = System.currentTimeMillis();
        Connection conn = null;
        try {
            String strSQL = "begin pr_lucky_routlet(?, ?,?,?); end;";

            conn = getDatabaseConnection();
            s = conn.prepareCall(strSQL);
            // Set timeout
            s.setQueryTimeout(30);
            s.setString(1, msisdn);
            s.setString(2, transid);
            s.setString(3, choice);
            s.registerOutParameter(4, java.sql.Types.INTEGER);
            s.execute();

            status = s.getInt(4);

        } catch (SQLException e) {
            MyLog.Error(getThreadName() + "ERROR" + e.getMessage());
            MyLog.Error(e);
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR3" + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }

        MyLog.Infor(getThreadName() + "Finish pr_lucky_routlet for " + msisdn + "' -> (" + status + ") ("
                + (System.currentTimeMillis() - start) + " ms)" + ", transid:" + transid);
        return status;
    }

    public int buyLog(String msisdn, String transid, int type) {
        int status = -1;
        MyLog.Debug(getThreadName() + "start pr_buy_log for " + msisdn + ", transid:" + transid);
        CallableStatement s = null;
        long start = System.currentTimeMillis();
        Connection conn = null;
        try {
            String strSQL = "begin pr_buy_log(?, ?,?); end;";

            conn = getDatabaseConnection();
            s = conn.prepareCall(strSQL);
            // Set timeout
            s.setQueryTimeout(30);
            s.setString(1, msisdn);
            s.setString(2, transid);
            s.setInt(3, type);
            s.execute();
        } catch (SQLException e) {
            MyLog.Error(getThreadName() + "ERROR" + e.getMessage());
            MyLog.Error(e);
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR3" + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }

        MyLog.Infor(getThreadName() + "Finish pr_buy_log for " + msisdn + "' -> (" + status + ") ("
                + (System.currentTimeMillis() - start) + " ms)" + ", transid:" + transid);
        return status;
    }

    public ArrayList<String> queryArr(String sql) {
        ArrayList<String> results = new ArrayList<>();
        MyLog.Debug(getThreadName() + "start query for " + sql);
        Statement s = null;
        long start = System.currentTimeMillis();
        Connection conn = null;
        ResultSet r = null;
        try {
            conn = getDatabaseConnection();
            s = conn.prepareCall(sql);
            // Set timeout
            s.setQueryTimeout(30);

            r = s.executeQuery(sql);
            while (r.next()) {
                String dataItem = r.getString("MSISDN") + "_" + r.getString("STATUS") + "_" + r.getString("MONFEE_SUCCESS_COUNT") + "_" + r.getString("DAYLASTCHARGE");
                MyLog.Infor("---------------" + dataItem);
                results.add(dataItem);
            }

        } catch (SQLException e) {
            MyLog.Error(getThreadName() + "ERROR" + e.getMessage());
            MyLog.Error(e);
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR3" + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception ee) {
                }
            }

            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }

        MyLog.Infor(getThreadName() + "Finish query for " + sql + "' -> ("
                + (System.currentTimeMillis() - start) + " ms)");
        return results;
    }

    public String queryReport(String sql) {
        String reportsms = "";
        Statement s = null;
        long start = System.currentTimeMillis();
        Connection conn = null;
        ResultSet r = null;
        try {
            conn = getDatabaseConnection();
            s = conn.prepareCall(sql);
            s.setQueryTimeout(30);
            r = s.executeQuery(sql);
            while (r.next()) {
                //SELECT sub_active,cost,profit,REGISTER, CANCEL, RENEW, RENEWFAIL, MONEY_RENEW, MONEY_BUY, MONEY_REGISTER from v_summery_report where trunc(report_day) = trunc(sysdate-1)

                reportsms = reportsms + " REGISTER = " + r.getString("REGISTER") + ";"
                        + "CANCEL = " + r.getString("CANCEL") + ";"
                        + "RENEW = " + r.getString("RENEW") + ";"
                        + "RENEWFAIL = " + r.getString("RENEWFAIL") + ";"
                        + "MONEY_BUY = " + r.getString("MONEY_BUY") + "KIP;"
                        + "MONEY_REGISTER = " + r.getString("MONEY_REGISTER") + "KIP;"
                        + "COST/PROFIT = " + r.getString("cost") + "KIP/" + r.getString("profit") + "KIP ;"
                        + "MONEY_REGISTER = " + r.getString("MONEY_REGISTER") + "KIP;"
                        + "MONEY_RENEW = " + r.getString("MONEY_RENEW") + "KIP;";
            }

        } catch (SQLException e) {
            MyLog.Error(getThreadName() + "ERROR" + e.getMessage());
            MyLog.Error(e);
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR3" + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception ee) {
                }
            }

            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }

        MyLog.Infor(getThreadName() + "Finish query for " + sql + "' -> ("
                + (System.currentTimeMillis() - start) + " ms)");
        return reportsms;
    }

    public Map<String, String> query(String sql) {
        Map<String, String> results = new HashMap<>();
        MyLog.Debug(getThreadName() + "start query for " + sql);
        Statement s = null;
        long start = System.currentTimeMillis();
        Connection conn = null;
        ResultSet r = null;
        try {
            conn = getDatabaseConnection();
            s = conn.prepareCall(sql);
            // Set timeout
            s.setQueryTimeout(30);

            r = s.executeQuery(sql);
            if (r.next()) {
                ResultSetMetaData meta = r.getMetaData();
                int coloumnCount = meta.getColumnCount();
                if (coloumnCount > 0) {
                    for (int i = 0; i < coloumnCount; i++) {
                        String key = meta.getColumnName((i + 1));
                        String value = r.getString((i + 1));
                        results.put(key, value);
                    }
                }
            }
        } catch (SQLException e) {
            MyLog.Error(getThreadName() + "ERROR" + e.getMessage());
            MyLog.Error(e);
        } catch (Exception ex) {
            MyLog.Error(getThreadName() + "ERROR3" + ex.getMessage());
            MyLog.Error(ex);
            reconnect();
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception ee) {
                }
            }

            if (s != null) {
                try {
                    s.close();
                } catch (Exception ee) {
                }
            }
            closeConnection(conn);
        }

        MyLog.Infor(getThreadName() + "Finish query for " + sql + "' -> ("
                + (System.currentTimeMillis() - start) + " ms)");
        return results;
    }

}
