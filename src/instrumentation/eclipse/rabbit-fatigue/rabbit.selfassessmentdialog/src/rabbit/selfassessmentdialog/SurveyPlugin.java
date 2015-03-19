package rabbit.selfassessmentdialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.framework.BundleContext;

import rabbit.data.internal.xml.XmlPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class SurveyPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "rabbit.selfassessmentdialog"; //$NON-NLS-1$

	// The shared instance
	private static SurveyPlugin plugin;
	
	private static Hashtable<String, String> logStatusList;
	
	/**
	 * The constructor
	 */
	public SurveyPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		logStatusList = new Hashtable<String, String>();
		checkLogsForDailySurvey();
	}

	private void checkLogsForDailySurvey() {
		String logFilePath = FilenameUtils.concat(XmlPlugin.getDefault()
				.getStoragePathRoot().toOSString(), "survey.log");
		
		File logFile = new File(logFilePath);
		
	    boolean fileExists = logFile.exists();
	    if (fileExists) {
	    	readLogFile(logFilePath);
	    }
	}

	private void readLogFile(String logFilePath) {
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(logFilePath));
 
			while ((sCurrentLine = br.readLine()) != null) {
				String[] date = sCurrentLine.split("[,]");
				logStatusList.put(date[0], date[1]);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}	
	}
	
	public static String checkForPendingData() {
		Iterator<Map.Entry<String, String>> it = logStatusList.entrySet().iterator();
		while (it.hasNext()) {
		  Map.Entry<String, String> entry = it.next();
		  if(entry.getValue().equals("false")) {
			  return entry.getKey();
		  }
		}		
		return null;	
	}
	
	private void writeLogFile() {
		String logFilePath = FilenameUtils.concat(XmlPlugin.getDefault()
				.getStoragePathRoot().toOSString(), "survey.log");
		
		File logFile = new File(logFilePath);
		
	    boolean fileExists = logFile.exists();
	    if (fileExists) {
	    	logFile.delete();
	    }
		
		try {
			logFile.createNewFile();
 
			FileWriter fw = new FileWriter(logFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			Iterator<Map.Entry<String, String>> it = logStatusList.entrySet().iterator();
			while (it.hasNext()) {
			  Map.Entry<String, String> entry = it.next();
			  bw.write(entry.getKey() + "," + entry.getValue());
			  bw.write("\n");
			}
			
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean checkDate(DateTime date) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		if(logStatusList.containsKey(fmt.print(date))) {
			return true;
		}
		return false;
	}
	
	public static void updateDateLog(DateTime date) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		logStatusList.put(fmt.print(date), "false");
	}
	
	public static void updateDateLog(String date) {
		logStatusList.put(date, "false");
	}
	
	public static void updateDateUploadStatus(DateTime date) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		if(logStatusList.containsKey(fmt.print(date))) {
			logStatusList.remove(fmt.print(date));
		}
		logStatusList.put(fmt.print(date), "true");
	}
	
	public static void updateDateUploadStatus(String date) {
		if(logStatusList.containsKey(date)) {
			logStatusList.remove(date);
		}
		logStatusList.put(date, "true");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		writeLogFile();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static SurveyPlugin getDefault() {
		return plugin;
	}

}
