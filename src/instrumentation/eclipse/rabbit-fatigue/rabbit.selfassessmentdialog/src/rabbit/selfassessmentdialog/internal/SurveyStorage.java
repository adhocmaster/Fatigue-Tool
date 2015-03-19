package rabbit.selfassessmentdialog.internal;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import rabbit.data.internal.xml.XmlPlugin;
import rabbit.selfassessmentdialog.SurveyPlugin;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SurveyStorage {

	public static String makeResultDir() {
		String folder = FilenameUtils.concat(XmlPlugin.getDefault()
				.getStoragePathRoot().toOSString(), "Assessment");
		folder = FilenameUtils.concat(folder, "Results");
		File dir = new File(folder);
		// Create the new directory and check read/write permissions:
		boolean dirCreated = dir.exists();
		if (!dirCreated) {
			dirCreated = dir.mkdirs();
		}
		if (!dirCreated || !dir.canRead() || !dir.canWrite()) {
			MessageDialog
					.openError(
							new Shell(),
							"Error",
							"Error occurred while "
									+ "accessing the new directory, please select another DevFatigue directory.");
			return null;
		}
		return folder;
	}

	public static boolean writeFile(String file, String data) {
		String folder = makeResultDir();
		if (folder == null)
			return false;
		String logFilePath = FilenameUtils.concat(folder, file);

		File logFile = new File(logFilePath);

		boolean fileExists = logFile.exists();
		if (fileExists) {
			logFile.delete();
		}

		try {
			logFile.createNewFile();

			FileWriter fw = new FileWriter(logFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(data);
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean sftpUpload(String timeStamp) {
		String SFTPHOST = "104.131.193.224";
		int SFTPPORT = 22;
		String SFTPUSER = "studydata";
		String SFTPPASS = "datastudy";
		String SFTPWORKINGDIR = "/home/studydata/";

		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		String filePath = compressDir(XmlPlugin.getDefault().getStoragePathRoot()
				.toOSString());
		String uploadPath = System.getProperty("user.name") + "_Data_"
				+ timeStamp + "_survey.zip";
		File f = new File(filePath);
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			session.setPassword(SFTPPASS);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(SFTPWORKINGDIR);			
			channelSftp.put(new FileInputStream(f), uploadPath);
			System.out.println("File uploaded");
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		finally {
			if(f.exists()) f.deleteOnExit();
		}
		
		return true;

	}

	public static boolean ftpUpload(String timeStamp) {
		final int BUFFER_SIZE = 4096;
		String ftpUrl = "ftp://%s:%s@%s/%s;type=i";
        String host = "127.0.0.1:21";
		String user = "user";
		String pass = "12345";
		/*String host = "104.131.193.224";
		String user = "root";
		String pass = "kpuggsqsewwt";*/
		
		String filePath = compressDir(XmlPlugin.getDefault().getStoragePathRoot()
				.toOSString());
		String uploadPath = "/" + System.getProperty("user.name") + "_Data_"
				+ timeStamp + "_survey.zip";
		
		File file = new File(filePath);
		
        ftpUrl = String.format(ftpUrl, user, pass, host, uploadPath);
        System.out.println("Upload URL: " + ftpUrl);
 
        try {
            URL url = new URL(ftpUrl);
            URLConnection conn = url.openConnection();
            OutputStream outputStream = conn.getOutputStream();
            FileInputStream inputStream = new FileInputStream(filePath);
 
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            inputStream.close();
            outputStream.close();
 
            System.out.println("File uploaded");
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        if(file.exists()) file.delete();
        
		return true;
	}

	private static String compressDir(String osString) {
		
		String zipFilePath = FilenameUtils.concat(XmlPlugin.getDefault().getStoragePathRoot()
				.toOSString(),"data.zip");
		File f = new File(zipFilePath);
		if(f.exists()) f.delete();
		
		ZipHelper zippy = new ZipHelper();
        try {
            zippy.zipDir(osString,zipFilePath);
        } catch(IOException e2) {
            System.err.println(e2);
        }
		
		return zipFilePath;
	}
	
	public static boolean saveUrl(final String filename, final String urlString) {
	    BufferedInputStream in = null;
	    FileOutputStream fout = null;
	    try {
	        in = new BufferedInputStream(new URL(urlString).openStream());
	        fout = new FileOutputStream(filename);

	        final byte data[] = new byte[1024];
	        int count;
	        while ((count = in.read(data, 0, 1024)) != -1) {
	            fout.write(data, 0, count);
	        }
	        return true;
	    }
	    catch (Exception e) {
	    	return false;
	    }	    
	    finally {
			try {
				if (in != null) {
					in.close();
				}
				if (fout != null) {
					fout.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
