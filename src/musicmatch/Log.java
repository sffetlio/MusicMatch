package musicmatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {

	public static final Logger logger = Logger.getLogger("MusicMatchLog");

	static {
		try {
			FileHandler fh = new FileHandler("D:\\MusicMatchLog.log", false);
			//fh.setFormatter(new XMLFormatter());
			//fh.setFormatter(new SimpleFormatter());
			fh.setFormatter(new java.util.logging.Formatter() {
				@Override
				public String format(LogRecord rec) {
					StringBuilder buf = new StringBuilder(1000);
					Calendar rightNow = Calendar.getInstance();
					buf.append(rightNow.get(Calendar.DATE));
					buf.append("/");
					buf.append(rightNow.get(Calendar.MONTH)+1);
					buf.append(" ");
					buf.append(rightNow.get(Calendar.HOUR));
					buf.append(":");
					buf.append(rightNow.get(Calendar.MINUTE));
					buf.append(":");
					buf.append(rightNow.get(Calendar.SECOND));
					buf.append(' ');
					buf.append(rec.getLevel());
					buf.append(' ');
					buf.append(formatMessage(rec));
					buf.append(System.getProperty("line.separator"));
					return buf.toString();
				}
			});
			logger.addHandler(fh);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String stackTraceToString(Throwable e) {
		String retValue = null;
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			retValue = sw.toString();
		} finally {
			try {
				if(pw != null)  pw.close();
				if(sw != null)  sw.close();
			} catch (IOException ignore) {}
		}
		return retValue;
	}
	
	public static void log(String str){
		logger.info(str);
	}
	
	public static void logSevere(String str){
		logger.severe(str);
	}
	
	public static void logWarning(String str){
		logger.warning(str);
	}
	
	public static void log(Exception e){
		logger.log(Level.OFF, stackTraceToString(e), e);
	}
	
	public static void logSevere(Exception e){
		logger.log(Level.SEVERE, stackTraceToString(e), e);
	}
	
	public static void logWarning(Exception e){
		logger.log(Level.WARNING, stackTraceToString(e), e);
	}
	
}
