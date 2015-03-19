package rabbit.tracking.internal.trackers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import rabbit.data.internal.xml.XmlPlugin;
import rabbit.selfassessmentdialog.internal.SurveyStorage;

@SuppressWarnings("restriction")
public class CodeAnalysis implements Runnable {

	private static IPath rabbitBasePath = XmlPlugin.getDefault().getStoragePath();
	private static String command1;
	private static String command2;
	
	private boolean commandExec;

	public CodeAnalysis(boolean commandExec) {
		super();
		this.commandExec = commandExec;
	}

	public static void checkStyleAnal(String projectPath, String projectName) {
		String reportLoc = getCheckStyleStorageLocation(projectName);
		DateTime curr = new DateTime();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss-SS");
		
		checkForExecutionFiles();
		
		String reportName = "checkStyleAnal-" + fmt.print(curr) + "_Sun.xml";
		command1 = "java -jar " + FilenameUtils.concat(System.getProperty("user.home"),
				"checkstyle-6.3-all.jar") + " -c "
				+ FilenameUtils.concat(System.getProperty("user.home"),
						"sun_checks.xml") + " -f xml -o "
				+ FilenameUtils.concat(reportLoc,
						reportName) + " " + projectPath;
		System.out.println(command1);
		
		reportName = "checkStyleAnal-" + fmt.print(curr) + "_Google.xml";
		command2 = "java -jar " + FilenameUtils.concat(System.getProperty("user.home"),
				"checkstyle-6.3-all.jar") + " -c "
				+ FilenameUtils.concat(System.getProperty("user.home"),
						"google_checks.xml") + " -f xml -o "
						+ FilenameUtils.concat(reportLoc,
								reportName) + " " + projectPath;
		System.out.println(command2);
		Thread task = new Thread(new CodeAnalysis(true));
		task.start();
	}
	
	private static void checkForExecutionFiles() {
		checkForCheckStyleJar("checkstyle-6.3-all.jar", 4406603);
		checkForCheckStyleJar("sun_checks.xml", 6640);
		checkForCheckStyleJar("google_checks.xml", 9874);
	}
	
	private static void checkForCheckStyleJar(String fileName, long size) {
		
		String folder = System.getProperty("user.home");
		
		String str = FilenameUtils.concat(folder,
				fileName);
		File file = new File(str);

	    if (file.exists() && size == file.length()) {
	      return;
	    }
		
	    SurveyStorage.saveUrl(str, "http://www4.ncsu.edu/~ssarkar4/fatigue/sleep/" + fileName);		
	}

	public static void execCommands(String command) {
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
		builder.redirectErrorStream(true);
		try {
			builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getStorageLocation() {
		String path = rabbitBasePath.toString() + "//CodeAnalysis";
		File f = new File(path);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				XmlPlugin
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, XmlPlugin.PLUGIN_ID,
								"Unable to create storage location. Perhaps no write permission?\n"
										+ f.getAbsolutePath()));
			}
		}
		return path;
	}
	
	public static String getCheckStyleStorageLocation(String projectName) {
		String path = getStorageLocation() + "/CheckStyle/" + projectName;
		File f = new File(path);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				XmlPlugin
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, XmlPlugin.PLUGIN_ID,
								"Unable to create storage location. Perhaps no write permission?\n"
										+ f.getAbsolutePath()));
			}
		}
		return path;
	}

	@Override
	public void run() {
		if(commandExec) {
			execCommands(command1);
			execCommands(command2);
		}
		else
			checkForExecutionFiles();
	}

}
