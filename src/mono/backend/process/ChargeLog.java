/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.backend.process;

import java.util.Date;

/**
 *
 * @author hungtv
 */
public class ChargeLog {

    private String mobile;
    private Date timeCharge;
    private String response;
    private String price;
    private long mss;

    public ChargeLog(String msisdn, String response) {
        this.mobile = msisdn;
        this.timeCharge = new Date();
        this.response = response;
    }

    public ChargeLog(String mobile, String response, String price, long mss) {
        this.mobile = mobile;
         this.timeCharge = new Date();
        this.response = response;
        this.price = price;
        this.mss = mss;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public long getMss() {
        return mss;
    }

    public void setMss(int mss) {
        this.mss = mss;
    }

    
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Date getTimeCharge() {
        return timeCharge;
    }

    public void setTimeCharge(Date timeCharge) {
        this.timeCharge = timeCharge;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

}
