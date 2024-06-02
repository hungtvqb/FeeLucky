/**
 *
 * handsome boy
 */
package mono.backend.bean;

import java.util.UUID;

/**
 *
 */
public class SmsData {

    private String url, user, pass, msisdn, content, shortCode, alias, smsType;
    private long sendTime; //affter this time, System will send sms
    private String id;
    
    
    public SmsData(String url, String user, String pass, String msisdn,
            String content, String shortCode, String alias, String smsType) {
        UUID uuid = UUID.randomUUID();
        this.id = uuid.toString();
        this.url = url;
        this.user = user;
        this.pass = pass;
        this.msisdn = msisdn;
        this.content = content;
        this.shortCode = shortCode;
        this.alias = alias;
        this.smsType = smsType;
        sendTime = System.currentTimeMillis();
    }

    public String getId(){
        return id;
    }
    
    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getContent() {
        return content;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getAlias() {
        return alias;
    }

    public String getSmsType() {
        return smsType;
    }

    public long getSendTime() {
        return sendTime;
    }
    
    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(msisdn)
                .append("|").append(shortCode)
                .append("|").append(smsType)
                .append("|").append(user)
                .append("|").append(content);
        return sb.toString();
    }
}
