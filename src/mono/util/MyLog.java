/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mono.util;

import org.apache.log4j.*;

/**
 *
 * @author Administrator
 */
public class MyLog {
    
    String logPath;
    private static final Logger logger = Logger.getLogger(MyLog.class);

//    public MyLog(String logFile) {
//        this.logPath = logFile;
//        PropertyConfigurator.configure(logPath);
//        logger = Logger.getRootLogger();
        
//        PatternLayout layout = new PatternLayout();
//        String conversionPattern = "[%p] %d %c %M - %m%n";
//        layout.setConversionPattern(conversionPattern);
//        DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
//        rollingAppender.setLayout(layout);
//   
//        logger.addAppender(rollingAppender);
//    }

    public static void Debug(String logString) {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        String clsName = ste.getClassName();
        clsName = clsName.substring(clsName.lastIndexOf(".") + 1, clsName.length());
        if (clsName.isEmpty()) {
            return;
        }
        //get a proper instance
        Logger subLog = logger.getLogger(clsName);
        if (subLog == null) {
            return;
        }
//        subLog.log(Level.DEBUG, " (" + ste.getFileName() + "," + ste.getLineNumber() + "," + ste.getMethodName() + " )" + logString);
    }

    public static void Infor(String logString) {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        String clsName = ste.getClassName();
        clsName = clsName.substring(clsName.lastIndexOf(".") + 1, clsName.length());
        if (clsName.isEmpty()) {
            return;
        }
        //get a proper instance
        Logger subLog = logger.getLogger(clsName);
        if (subLog == null) {
            return;
        }
        subLog.log(Level.INFO, " (" + ste.getFileName() + "," + ste.getLineNumber() + "," + ste.getMethodName() + " )" + logString);
    }

    public static void Error(String logString) {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        String clsName = ste.getClassName();
        clsName = clsName.substring(clsName.lastIndexOf(".") + 1, clsName.length());
        if (clsName.isEmpty()) {
            return;
        }
        //get a proper instance
        Logger subLog = logger.getLogger(clsName);
        if (subLog == null) {
            return;
        }
        subLog.log(Level.ERROR, " (" + ste.getFileName() + "," + ste.getLineNumber() + "," + ste.getMethodName() + " )" + logString);
    }

    public static void Fatal(String logString) {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        String clsName = ste.getClassName();
        clsName = clsName.substring(clsName.lastIndexOf(".") + 1, clsName.length());
        if (clsName.isEmpty()) {
            return;
        }
        //get a proper instance
        Logger subLog = logger.getLogger(clsName);
        if (subLog == null) {
            return;
        }
        subLog.log(Level.FATAL, " (" + ste.getFileName() + "," + ste.getLineNumber() + "," + ste.getMethodName() + " )" + logString);
    }

    public static void Warning(String logString) {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        String clsName = ste.getClassName();
        clsName = clsName.substring(clsName.lastIndexOf(".") + 1, clsName.length());
        if (clsName.isEmpty()) {
            return;
        }
        //get a proper instance
        Logger subLog = logger.getLogger(clsName);
        if (subLog == null) {
            return;
        }
        subLog.log(Level.WARN, " (" + ste.getFileName() + "," + ste.getLineNumber() + "," + ste.getMethodName() + " )" + logString);
    }
    
    public static void Error(Exception ex) {
        logger.error(ex.getMessage(), ex);
    }
}
