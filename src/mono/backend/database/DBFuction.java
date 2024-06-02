/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.backend.database;

import java.util.ArrayList;
import java.util.Calendar;
import mono.util.CONST;
import mono.util.MyLog;
import redis.clients.jedis.Jedis;

/**
 *
 * @author hungtv
 */
public class DBFuction {

    public static boolean updateExprire(String msisdn, int numberCharge) {
        boolean respones = false;
        DirectConnection connect = null;
        String sql = "";
        if (numberCharge >= 3) {
            sql = "UPDATE subscriber SET status=1, NEXT_MONFEE_CHARGE_TIME = trunc(sysdate) +1,LAST_MONFEE_CHARGE_TIME = sysdate, MONFEE_SUCCESS_COUNT = 0 WHERE MSISDN = " + msisdn;

        } else {
            sql = "UPDATE subscriber SET status=1, NEXT_MONFEE_CHARGE_TIME = trunc(sysdate) +1,LAST_MONFEE_CHARGE_TIME = sysdate, MONFEE_SUCCESS_COUNT = MONFEE_SUCCESS_COUNT + 1 WHERE MSISDN = " + msisdn;
        }
        try {
            connect = DatabaseConnectionPool.getInstance().getConnection();
            respones = connect.excuteUpdate(sql);
        } catch (Exception e) {
        }
        return respones;
    }

    public static boolean updateStatus(String msisdn, int status) {
        boolean respones = false;
        DirectConnection connect = null;
        String sql = "";
        sql = "UPDATE subscriber SET status = " + status + " WHERE MSISDN = " + msisdn;
        try {
            connect = DatabaseConnectionPool.getInstance().getConnection();
            respones = connect.excuteUpdate(sql);
        } catch (Exception e) {
        }
        return respones;
    }

    public static boolean updateCancel(String msisdn, int status) {
        boolean respones = false;
        DirectConnection connect = null;
        String sql = "";
        sql = "UPDATE subscriber SET status = " + status + ",CANCEL_TIME= sysdate WHERE MSISDN = " + msisdn;
        try {
            connect = DatabaseConnectionPool.getInstance().getConnection();
            respones = connect.excuteUpdate(sql);
        } catch (Exception e) {
        }
        return respones;
    }

    public static void selectFeeOracle() {
        DirectConnection connect = null;
        Jedis jedis = null;
        String sql = "SELECT MSISDN,STATUS,MONFEE_SUCCESS_COUNT,EXTRACT(DAY FROM (sysdate-LAST_MONFEE_CHARGE_TIME)) AS DAYLASTCHARGE FROM subscriber WHERE STATUS in (1,2) and NEXT_MONFEE_CHARGE_TIME < trunc(sysdate)";
        try {
            connect = DatabaseConnectionPool.getInstance().getConnection();
            jedis = RedisConnectPool.getInstance().getConnectionRedis();
            ArrayList<String> dataM = connect.queryArr(sql);
            System.out.println("size = " + dataM.size());
            MyLog.Infor("size = " + dataM.size());
            for (String string : dataM) {
//                System.out.println(string);
                MyLog.Infor(string);
                jedis.lpush(CONST.R_QUEUE_CHARGE_FEE, string);
            }
        } catch (Exception e) {

        }
    }

    public static String reportService() {
        DirectConnection connect = null;
        String report = "";
//        String sql = "SELECT REGISTER, CANCEL, RENEW, RENEWFAIL, MONEY_RENEW, MONEY_BUY, MONEY_REGISTER from v_summery_report where trunc(report_day) = trunc(sysdate)";
        String sql = "SELECT REGISTER, CANCEL, RENEW, RENEWFAIL, MONEY_RENEW, MONEY_BUY, MONEY_REGISTER,cost,profit from v_summery_report where trunc(report_day) = trunc(sysdate)";
        try {
            connect = DatabaseConnectionPool.getInstance().getConnection();
            report = connect.queryReport(sql);

        } catch (Exception e) {

        }
        return report;
    }

    public static void main(String[] args) {

        Calendar cal = Calendar.getInstance();
        Calendar callend = Calendar.getInstance();
        long x;
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(year, month, day, 0, 0, 0);
        cal.add(Calendar.DATE, -1);

        callend.set(year, month, day, 23, 59, 59);
        callend.add(Calendar.DATE, -1);
    }
}
