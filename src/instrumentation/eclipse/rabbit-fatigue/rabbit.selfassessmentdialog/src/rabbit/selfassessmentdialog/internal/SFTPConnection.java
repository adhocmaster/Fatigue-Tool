package rabbit.selfassessmentdialog.internal;

import rabbit.selfassessmentdialog.SurveyPlugin;

public class SFTPConnection implements Runnable {
	
	String timeStamp = null;
	public static boolean uploadInProgress = false;
	
	public SFTPConnection(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public void run() {
		uploadInProgress = true;
		boolean dataUpload = SurveyStorage.sftpUpload(timeStamp);
		if(dataUpload) {
			SurveyPlugin.updateDateUploadStatus(timeStamp);
		}
		uploadInProgress = false;
	}

}