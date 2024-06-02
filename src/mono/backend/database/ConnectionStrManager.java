/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mono.backend.database;

import mono.util.TextSecurity;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 *
 * @author hoand
 */
public class ConnectionStrManager {
//    private static ConnectionStrManager mMe = null;
    private Vector<String> mConnectionList;
    private int mCurrentPointer;
//    private String mConfFile;
    
    public ConnectionStrManager(String confFile) {
        this(confFile, true);
    }
    
     public ConnectionStrManager(String confFile, boolean loadFile) {
        mConnectionList = new Vector<String>();
        mCurrentPointer = 0;
//        mConfFile = confFile;
        if (loadFile) {
            loadFile(confFile);
        }
    }
    
     public ConnectionStrManager(Vector<String> mConnectionList) {
       this.mConnectionList = mConnectionList;
    }
     
//    public static ConnectionStrManager getInstance(String connectionFile) {
//        if (mMe == null) {
//            mMe = new ConnectionStrManager();
//            mMe.loadFile(connectionFile);
//        }
//
//        return mMe;
//    }
    //hungtv45
    private void loadFile(String connectionFile) {
        try {
            FileInputStream fis;
            BufferedReader s;
            String line;
            int count;

            mConnectionList.clear();
            fis = new FileInputStream(connectionFile);
            s = new BufferedReader(new InputStreamReader(fis));

            TextSecurity sec = TextSecurity.getInstance();
            
            line = s.readLine();
            count = 1;
            while (line != null) {
                if (line != null) {
                    line = line.trim();
                    if (line.length() > 0 && line.charAt(0) != '#') {
                        mConnectionList.add(sec.Decrypt(line));
                    }
                }
                line = s.readLine();
                count ++;
            }

            s.close();

        } catch (Exception ex) {
            mConnectionList.clear();
        }
    }
    
    public String getConnectionStr() {
        mCurrentPointer++;
        if (mCurrentPointer >= mConnectionList.size()) {
            mCurrentPointer = 0;
        }

        if (mConnectionList.isEmpty()) {
            return null;
        } else {
            return mConnectionList.elementAt(mCurrentPointer);
        }
    }
    
    public String getUrl(){
        if (mConnectionList.size() > 0) {
            return mConnectionList.get(0);
        }
        
        return null;
    }
    
    public String getUser(){
        if (mConnectionList.size() > 1) {
            return mConnectionList.get(1);
        }
        
        return null;
    }
    
    public String getPassword(){
        if (mConnectionList.size() > 2) {
            return mConnectionList.get(2);
        }
        
        return null;
    }
    
}
