/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.backend.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 *
 * @author hungtv.hut
 */
public class Databases {

    public static boolean checkSubRegister(String msisdn) {
        Connection connection = null;
        PreparedStatement pre = null;
        ResultSet rs = null;
        boolean statusReturn = false;

        try {
            connection = DBPool.getInstance().getConnectionDB();
            String sqlCheckSub = "EXAMPLE";
            pre = connection.prepareStatement(sqlCheckSub);
            pre.setString(1, msisdn);
            rs = pre.executeQuery();
            while (rs.next()) {
                String sub = rs.getString("MSISDN");
                if (sub.length() > 7) {
                    statusReturn = true;
                }
            }

        } catch (SQLException e) {

        } finally {
            DBPool.closeConnection(connection);
            DBPool.closeResultSet(rs);
            DBPool.closeStatement(pre);
        }
        return statusReturn;
    }

}
