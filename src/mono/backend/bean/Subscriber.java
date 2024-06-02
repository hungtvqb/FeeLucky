///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package mono.backend.bean;
//
//import java.sql.Timestamp;
//import javax.persistence.Column;
//import javax.persistence.Table;
//
//@Table(name = "SUBSCRIBER")
//public class Subscriber extends PO{
//    public static final String PARAM_REGISTER = "0";
//    public static final String PARAM_CANCEL = "1";
//    public static final String PARAM_PENDING = "2";
//    
//    public static final int PARAM_STATUS_ACTIVE = 1;
//    public static final int PARAM_STATUS_CANCEL = 0;
//    public static final int PARAM_STATUS_PENDING = 2;
//    public static final int PARAM_STATUS_CHARGEOWN = 3;
//
//    @Column(name = "SCORE")
//    public int core;
//    
//    public Subscriber() {
//    }
//    
//    @Column(name = "ID")
//    public String ID;
//
//    public void setID(String ID) {
//        this.ID = ID;
//    }
//
//    public String getID() {
//        return ID;
//    }
//    
//    @Column(name = "MSISDN")
//    public String MSISDN;
//
//    public void setMSISDN(String MSISDN) {
//        this.MSISDN = MSISDN;
//    }
//
//    public String getMSISDN() {
//        return MSISDN;
//    }
//    
//    @Column(name = "SUB_SERVICE_NAME")
//    public String SUB_SERVICE_NAME;
//
//    public void setSUB_SERVICE_NAME(String SUB_SERVICE_NAME) {
//        this.SUB_SERVICE_NAME = SUB_SERVICE_NAME;
//    }
//
//    public String getSUB_SERVICE_NAME() {
//        return SUB_SERVICE_NAME;
//    }
//    
//    @Column(name = "STATUS")
//    public int STATUS;
//
//    public void setSTATUS(int STATUS) {
//        this.STATUS = STATUS;
//    }
//
//    public int getSTATUS() {
//        return STATUS;
//    }
//    
//    @Column(name = "REGISTER_TIME")
//    public Timestamp REGISTER_TIME;
//
//    public void setREGISTER_TIME(Timestamp REGISTER_TIME) {
//        this.REGISTER_TIME = REGISTER_TIME;
//    }
//
//    public Timestamp getREGISTER_TIME() {
//        return REGISTER_TIME;
//    }
//    
//    @Column(name = "CANCEL_TIME")
//    public Timestamp CANCEL_TIME;
//
//    public void setCANCEL_TIME(Timestamp CANCEL_TIME) {
//        this.CANCEL_TIME = CANCEL_TIME;
//    }
//
//    public Timestamp getCANCEL_TIME() {
//        return CANCEL_TIME;
//    }
//    
//    @Column(name = "DESCRIPTIONS")
//    public String DESCRIPTIONS;
//
//    public void setDESCRIPTIONS(String DESCRIPTIONS) {
//        this.DESCRIPTIONS = DESCRIPTIONS;
//    }
//
//    public String getDESCRIPTIONS() {
//        return DESCRIPTIONS;
//    }
//    
//    @Column(name = "LAST_MONFEE_CHARGE_TIME")
//    public Timestamp LAST_MONFEE_CHARGE_TIME;
//
//    public void setLAST_MONFEE_CHARGE_TIME(Timestamp LAST_MONFEE_CHARGE_TIME) {
//        this.LAST_MONFEE_CHARGE_TIME = LAST_MONFEE_CHARGE_TIME;
//    }
//
//    public Timestamp getLAST_MONFEE_CHARGE_TIME() {
//        return LAST_MONFEE_CHARGE_TIME;
//    }
//    
//    @Column(name = "NEXT_MONFEE_CHARGE_TIME")
//    public Timestamp NEXT_MONFEE_CHARGE_TIME;
//
//    public void setNEXT_MONFEE_CHARGE_TIME(Timestamp NEXT_MONFEE_CHARGE_TIME) {
//        this.NEXT_MONFEE_CHARGE_TIME = NEXT_MONFEE_CHARGE_TIME;
//    }
//
//    public Timestamp getNEXT_MONFEE_CHARGE_TIME() {
//        return NEXT_MONFEE_CHARGE_TIME;
//    }
//    
//    
//    @Column(name = "CHARGE_STATUS")
//    public int CHARGE_STATUS;
//
//    public void setCHARGE_STATUS(int CHARGE_STATUS) {
//        this.CHARGE_STATUS = CHARGE_STATUS;
//    }
//
//    public int getCHARGE_STATUS() {
//        return CHARGE_STATUS;
//    }
//    
//    @Column(name = "IS_PROMOTION")
//    public int IS_PROMOTION;
//
//    public void setIS_PROMOTION(int IS_PROMOTION) {
//        this.IS_PROMOTION = IS_PROMOTION;
//    }
//
//    public int getIS_PROMOTION() {
//        return IS_PROMOTION;
//    }
//    
//    @Column(name = "MONFEE_SUCCESS_COUNT")
//    public int MONFEE_SUCCESS_COUNT;
//
//    public void setMONFEE_SUCCESS_COUNT(int MONFEE_SUCCESS_COUNT) {
//        this.MONFEE_SUCCESS_COUNT = MONFEE_SUCCESS_COUNT;
//    }
//
//    public int getMONFEE_SUCCESS_COUNT() {
//        return MONFEE_SUCCESS_COUNT;
//    }
//    
//    @Column(name = "LAST_SEND_NOTIFY")
//    public Timestamp LAST_SEND_NOTIFY;
//
//    public void setLAST_SEND_NOTIFY(Timestamp LAST_SEND_NOTIFY) {
//        this.LAST_SEND_NOTIFY = LAST_SEND_NOTIFY;
//    }
//
//    public Timestamp getLAST_SEND_NOTIFY() {
//        return LAST_SEND_NOTIFY;
//    }
//
//    
//    public String param; // 
//    public String amount;
//    
//    public String getParam() {
//        return param;
//    }
//
//    public void setParam(String param) {
//        this.param = param;
//    }
//
//    public String getAmount() {
//        return amount;
//    }
//
//    public void setAmount(String amount) {
//        this.amount = amount;
//    }
//
//    @Override
//    public String toString() {
//        return MSISDN + "-" + SUB_SERVICE_NAME;
//    }
//
//    
//    
//    public int getCore() {
//        return core;
//    }
//
//    public void setCore(int core) {
//        this.core = core;
//    }
//    
//}
