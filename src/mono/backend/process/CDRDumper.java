/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.backend.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 *
 * @author hoand
 */
public class CDRDumper implements Runnable {
    private Logger log = Logger.getLogger(CDRDumper.class);
    
    private String mCDRLocation;
    private String mFileNamePrefix;
    private String mFileNameExtension;
    private int mDumpSequence;
    private int mMaxRecord;
    private boolean mSpreadByDate;
    
    private Vector<String> mCDRQueue;
    private long mLastDump;
    private final Object mLock = new Object();
    private boolean mIsRunning;
    private String cdrThreadName;
    private String cdrConfigFile;
    
    public CDRDumper(String cdrConfigFile) {
        mIsRunning = false;
        this.cdrConfigFile = cdrConfigFile;
        loadConfig(cdrConfigFile);
    }
    
    private String getThreadName() {
        return "CDRDumper (" + cdrThreadName + ") ";
    }
    
    private void loadConfig(String cdrConfigFile) {
        try {
            Properties prop = new Properties();
            FileInputStream gencfgFile = new FileInputStream(cdrConfigFile);
            prop.load(gencfgFile);
            
            cdrThreadName = prop.getProperty("cdr_name");
            
            // Connect to radius server
            mCDRLocation = prop.getProperty("cdr_location");
            if (mCDRLocation == null || mCDRLocation.isEmpty())
                mCDRLocation = "cdr";

            mCDRLocation += ("/" + cdrThreadName);
            log.info(getThreadName() + "CDR Location: " + mCDRLocation);
            
            File f = new File(mCDRLocation);
            if (!f.exists() || !f.isDirectory()) {
                if (f.mkdir()) {
//                    log.debug(getThreadName() + "Create dir: " + mCDRLocation);
                } else {
//                    log.error(getThreadName() + "Cannot create dir: " + mCDRLocation);
                }
            }
            
            mFileNamePrefix = prop.getProperty("cdr_prefix");
            if (mFileNamePrefix == null || mFileNamePrefix.isEmpty())
                mFileNamePrefix = "mps_sse_";

            log.info(getThreadName() + "CDR Prefix: " + mFileNamePrefix);
            
            mFileNameExtension = prop.getProperty("cdr_extension");
            if (mFileNameExtension == null || mFileNameExtension.isEmpty())
                mFileNameExtension = ".txt";

            log.info(getThreadName() + "CDR Extension: " + mFileNameExtension);
            
            try {
                String tt = prop.getProperty("dump_sequence");
                mDumpSequence = Integer.parseInt(tt.trim()) * 1000;
            } catch (Exception e) {
                mDumpSequence = 60000;
            }
            log.info(getThreadName() + "Dump Sequence: " + mDumpSequence);
            
            try {
                String tt = prop.getProperty("max_record");
                mMaxRecord = Integer.parseInt(tt.trim()) * 1000;
            } catch (Exception e) {
                mMaxRecord = 1000;
            }
            log.info(getThreadName() + "Max Sequence: " + mMaxRecord);
            
            try {
                String tt = prop.getProperty("spread_date");
                mSpreadByDate = Boolean.parseBoolean(tt);
            } catch (Exception e) {
                mSpreadByDate = true;
            }
            log.info(getThreadName() + "Spread by date: " + mSpreadByDate);
            
            gencfgFile.close();
        } catch (Exception ex) {
            log.error(ex);
        }
    }
    
    public void start() {
        if (!mIsRunning) {
            mLastDump = System.currentTimeMillis();
            mIsRunning = true;
            
            mCDRQueue = new Vector<String>();
            Thread t = new Thread(this);
            t.start();
        }
    }
    
    public void stop() {
        mIsRunning = false;
    }

    private String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date now = new Date(System.currentTimeMillis());
        return mFileNamePrefix + cdrThreadName + "_" + format.format(now) + mFileNameExtension;
    }
    
    private String getExportFolder() {
        if (mSpreadByDate) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date now = new Date(System.currentTimeMillis());
            
            File f = new File(mCDRLocation + "/" + format.format(now));
            if (!f.exists() || !f.isDirectory()) {
                if (f.mkdir()) {
//                    log.debug(getThreadName() + "Created dir: " + f.getAbsolutePath());
                    return f.getAbsolutePath();
                } else {
//                    log.debug(getThreadName() + "Can NOT Created dir: " + f.getAbsolutePath());
                    return mCDRLocation;
                }
            } else {
                return f.getAbsolutePath();
            }
        } else {
            return mCDRLocation;
        }
    }
    
    public void addCDR(String cdr) {
        synchronized (mLock) {
            mCDRQueue.add(cdr);
//            log.debug(getThreadName() + "CDR DUMPER: Add '" + cdr + "'");
        }
    }
    
    public String getCDR() {
        synchronized (mLock) {
            if (!mCDRQueue.isEmpty()) {
                String cdr = mCDRQueue.firstElement();
                mCDRQueue.remove(0);
                
                return cdr;
            } else {
                return null;
            }
        }
    }
    
    private void dump() {
        if (mCDRQueue.size() > 0) {
            FileOutputStream out = null;
            BufferedWriter buff = null;
            try {
                String fname = getExportFolder() + "/" + getFileName();
                out = new FileOutputStream(fname);
                buff = new BufferedWriter(new OutputStreamWriter(out));
                
                int count = 0;
                String cdr = getCDR();
                
                while (count < mMaxRecord && cdr != null) {
                    buff.write(cdr + "\n"); //XXX
                    count ++;
                    cdr = getCDR();
                }
                
                log.info(getThreadName() + "Dumped CDR to: '" + fname + "'");
            } catch (Exception ex) {
                log.error(ex);
            } finally {
                try {
                    buff.close();
                    out.close();
                } catch (Exception ee) {}
            }
        }
    }
    
    private void process() {
        long now = System.currentTimeMillis();
        if (mCDRQueue.size() > mMaxRecord || (now - mLastDump) > mDumpSequence) {
            dump();
            mLastDump = System.currentTimeMillis();
        }
    }

    @Override
    public void run() {
        while (mIsRunning) {
            try {
                process();
                Thread.sleep(1000);
            } catch (Exception ex) {
                log.error(ex);
            }
        }
    }
}
