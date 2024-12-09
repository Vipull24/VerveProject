package org.verve.logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

public class LogToFile {

    protected static final Logger logger = Logger.getLogger("ApplicationLogger");
    /**
     * log Method
     * enable to log all exceptions to a file and display user message on demand
     * @param ex
     * @param level
     * @param msg
     */
    public static void log(Exception ex, String level, String msg){

        Map<Integer, Boolean> mp = Collections.synchronizedMap(new ConcurrentHashMap<>());

        FileHandler fh = null;
        try {
            fh = new FileHandler("verve.log",true);
            logger.addHandler(fh);
            Formatter formatter = new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                        Calendar cal = new GregorianCalendar();
                        cal.setTimeInMillis(record.getMillis());
                        return logTime.format(cal.getTime())
                                + " || "
                                + record.getMessage() + "\n";
                    }
                };
            fh.setFormatter(formatter);

            switch (level) {
                case "severe":
                    logger.log(Level.SEVERE, msg, ex);
                    break;
                case "warning":
                    logger.log(Level.WARNING, msg, ex);
                    break;
                case "info":
                    logger.log(Level.INFO, msg, ex);
                    break;
                default:
                    logger.log(Level.CONFIG, msg, ex);
                    break;
            }
        } catch (IOException | SecurityException ex1) {
            logger.log(Level.SEVERE, null, ex1);
        } finally{
            if(fh!=null)fh.close();
        }
    }
}
