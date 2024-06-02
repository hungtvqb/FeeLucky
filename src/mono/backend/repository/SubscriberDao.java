//package mono.backend.repository;
//
//import mono.backend.bean.Subscriber;
//import com.mongodb.DB;
//import java.util.LinkedList;
//import java.util.List;
//
//public class SubscriberDao extends GenericDao<Subscriber> {
//    
//    public Subscriber find(String msisdn, String subServiceName){
//        List<String> columnNames = new LinkedList<>();
//        columnNames.add("Msisdn");
//        columnNames.add("sub_service_name");
//        List<Object> values = new LinkedList<>();
//        values.add(msisdn);
//        values.add(subServiceName);
//        
//        return findByColumNames(columnNames, values);
//    }
//    
////    public Subscriber findByMsisdn(String msisdn){
////        return findByColumName("msisdn", msisdn);
////    }
//    
//    
//    public boolean updateStatusByMsisdn(int status, String msisdn, String subServiceName){
//        String sql = "Update " + getTableName() + " Set Status =? ,updated=sysdate WHERE Msisdn = ? and sub_service_name=?";
//    	int result = DB.update(sql, status, msisdn, subServiceName);
//    	return result > 0;
//    }
//    
//    public boolean updateRenewByMsisdn(int status, String msisdn, String subServiceName){
//        String sql = "Update " + getTableName() + " Set LAST_MONFEE_CHARGE_TIME = sysdate, " 
//                + " NEXT_MONFEE_CHARGE_TIME = sysdate + 1, "
//                + " MONFEE_SUCCESS_COUNT = MONFEE_SUCCESS_COUNT + 1, "
//                + " Status =? WHERE Msisdn = ? and sub_service_name=? ";
//    	int result = DB.update(sql, status, msisdn, subServiceName);
//    	return result > 0;
//    }
//    
//    public boolean updateRegisterByMsisdn(int status, String msisdn, String subServiceName){
//        String sql = "Update " + getTableName() + " Set REGISTER_TIME = sysdate + 1, " 
//                + " LAST_MONFEE_CHARGE_TIME = sysdate + 1,updated=sysdate, "
//                + " Status =?,content=null WHERE Msisdn = ? and sub_service_name=? ";
//    	int result = DB.update(sql, status, msisdn, subServiceName);
//    	return result > 0;
//    }
//    
//    public boolean updateCancelByMsisdn(int status, String msisdn, String subServiceName){
//        String sql = "Update " + getTableName() + " Set CANCEL_TIME = sysdate, " 
//                + " Status =? ,updated=sysdate WHERE Msisdn = ? and sub_service_name=? ";
//    	int result = DB.update(sql, status, msisdn, subServiceName);
//    	return result > 0;
//    }
//    
//    public boolean updateSolugan(String msisdn, String subServiceName, String solugan){
//        String sql = "Update " + getTableName() + " Set " 
//                + " content =? ,updated=sysdate WHERE Msisdn = ? and sub_service_name=? ";
//    	int result = DB.update(sql,solugan,  msisdn, subServiceName);
//    	return result > 0;
//    }
//    
//    public boolean updateSoluganById(String msisdn, String subServiceName, String smsId){
//        String sql = "Update " + getTableName() + " Set " 
//                + " content =(select content from sms_content where id = ?) ,updated=sysdate WHERE Msisdn = ? and sub_service_name=? ";
//    	int result = DB.update(sql,smsId,  msisdn, subServiceName);
//    	return result > 0;
//    }
//    
//}