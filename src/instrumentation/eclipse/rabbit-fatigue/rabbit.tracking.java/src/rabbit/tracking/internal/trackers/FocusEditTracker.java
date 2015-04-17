/*
 * Copyright 2010 The Rabbit Eclipse Plug-in Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package rabbit.tracking.internal.trackers;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import rabbit.data.handler.DataHandler;
import rabbit.data.internal.xml.XmlPlugin;
import rabbit.data.store.IStorer;
import rabbit.data.store.model.FocusEvent;
import rabbit.selfassessmentdialog.SurveyPlugin;
import rabbit.selfassessmentdialog.internal.SFTPConnection;
import rabbit.selfassessmentdialog.ui.views.DailyFormView;
import rabbit.selfassessmentdialog.ui.views.SessionFormView;

import com.google.common.collect.Sets;

/**
 * Tracks time spent on Java elements such as classes, methods.
 */
@SuppressWarnings("restriction")
public class FocusEditTracker extends AbstractTracker<FocusEvent> {

	/**
	 * A set of all text widgets that are currently being listened to. This set
	 * is not synchronised.
	 */
	private final Set<StyledText> registeredWidgets;	
	private boolean mouseUpFlag = true;
	private ArrayList<Integer[]> mouseCoords = new ArrayList<Integer[]>();
	private DateTime lastTime = null;
	private DateTime lastActive = null;
	DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yy hh.mm.ss.SS aa");
	
	/**
	 * Listener to listen to mouse wheel input on text widgets of
	 * editors.
	 */
	private final MouseWheelListener mouseWheelListener = new MouseWheelListener() {

		@Override
		public void mouseScrolled(MouseEvent e) {
			if (e.getSource() instanceof StyledText) {
				StyledText widget = (StyledText) e.getSource();
				DateTime curr = new DateTime();
				addData(new FocusEvent(curr, fmt.print(curr), "Mouse Scroll - Line No. " + (widget.getLineIndex(e.y) + 1)));
			}
		}
	};

	/**
	 * Listener to listen to keyboard input on text widgets of
	 * editors.
	 */
	private final KeyListener keyFocuslistener = new KeyListener() {

		@Override
		public void keyPressed(KeyEvent e) {
			DateTime curr = new DateTime();
			if (e.keyCode == 8) {
				addData(new FocusEvent(curr, fmt.print(curr), "Key Down - Backspace"));
			}
			else if (e.keyCode == 13) {
				addData(new FocusEvent(curr, fmt.print(curr), "Key Down - newLine"));
			}
			else {				
				addData(new FocusEvent(curr, fmt.print(curr), "Key Down - " + e.keyCode));
			}
			try {
				checkAndDeleteZippedSurveyFile();
				openSurveyOnNewSession();
				uploadPendingData();
				checkForDailySurvey();
				Thread task = new Thread(new CodeAnalysis(false));
				task.start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			DateTime curr = new DateTime();
			if (e.keyCode == 8) {
				addData(new FocusEvent(curr, fmt.print(curr), "Key Up - Backspace"));
			}
			else if (e.keyCode == 13) {
				addData(new FocusEvent(curr, fmt.print(curr), "Key Up - newLine"));
			}
			else {
				addData(new FocusEvent(curr, fmt.print(curr), "Key Up - " + e.keyCode));
			}
		}
	};
	
	/**
	 * Listener to listen to mouse input on text widgets of
	 * editors.
	 */
	private final MouseListener mouseFocuslistener = new MouseListener() {

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			DateTime curr = new DateTime();
			addData(new FocusEvent(curr, fmt.print(curr), "Mouse Double Click - " + e.x +","+e.y));			
		}

		@Override
		public void mouseDown(MouseEvent e) {
			DateTime curr = new DateTime();
			addData(new FocusEvent(curr, fmt.print(curr), "Mouse Down - " + e.x +","+e.y));
			mouseUpFlag = false;
			calulateMouseVelocity();
			mouseCoords.clear();
			lastTime = curr;
		}

		@Override
		public void mouseUp(MouseEvent e) {
			DateTime curr = new DateTime();
			addData(new FocusEvent(curr, fmt.print(curr), "Mouse Up - " + e.x +","+e.y));
			mouseUpFlag = true;
		}


	};
	
	private void calulateMouseVelocity() {

		if (mouseCoords != null && mouseCoords.size() > 0) {
			double sum = 0.0;
			DateTime curr = new DateTime();
			for (int index = mouseCoords.size() - 1; index > 0; index--) {
				Integer[] coord2 = mouseCoords.get(index);
				Integer[] coord1 = mouseCoords.get(index - 1);
				double temp = Math.sqrt(Math.pow((coord2[0] - coord1[0]), 2.0)
						+ Math.pow((coord2[1] - coord1[1]), 2.0));
				sum += temp;
			}

			if (lastTime != null) {
				Interval diff = new Interval(lastTime, curr);
				//System.out.println(diff.toDurationMillis());
				double velocity = (double) (sum / diff.toDurationMillis());
				addData(new FocusEvent(curr, fmt.print(curr),
						"Mouse Velocity - " + velocity));
				addData(new FocusEvent(curr, fmt.print(curr),
						"Mouse Acceleration - "
								+ (double) (velocity / diff.toDurationMillis())));
			}
		}
	}
	
	/**
	 * Listener to listen to mouse move input on text widgets of
	 * editors.
	 */
	private final MouseMoveListener mouseMoveFocuslistener = new MouseMoveListener() {

		@Override
		public void mouseMove(MouseEvent e) {
			if (mouseUpFlag) {
				Integer[] coord = new Integer[2];
				coord[0] = e.x;
				coord[1] = e.y;
				mouseCoords.add(coord);
			}
			try {
				checkAndDeleteZippedSurveyFile();
				openSurveyOnNewSession();
				uploadPendingData();
				checkForDailySurvey();
				Thread task = new Thread(new CodeAnalysis(false));
				task.start();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	/**
	 * Constructor.
	 */
	public FocusEditTracker() {
		super();
		registeredWidgets = Sets.newHashSet();
	}

	@Override
	protected IStorer<FocusEvent> createDataStorer() {
		return DataHandler.getStorer(FocusEvent.class);
	}

	@Override
	protected void doDisable() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			deregister(window);
		}
	}

	@Override
	protected void doEnable() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			register(window);
		}
	}
	
	@Override
	public void saveData() {
	  super.saveData();
	}

	/**
	 * Removes the workbench window so that it's no longer being tracked.
	 * 
	 * @param window
	 *            The workbench window.
	 */
	private void deregister(IWorkbenchWindow window) {
		for (IWorkbenchPage page : window.getPages()) {
			for (IEditorReference ref : page.getEditorReferences()) {
				IEditorPart editor = ref.getEditor(false);
				if (editor instanceof JavaEditor) {
					deregister((JavaEditor) editor);
				}
			}
		}
	}

	/**
	 * Removes the editor no that it's no longer being tracked.
	 * 
	 * @param editor
	 *            The editor.
	 */
	private synchronized void deregister(final JavaEditor editor) {
		final StyledText widget = editor.getViewer().getTextWidget();
		if (registeredWidgets.contains(widget)) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					widget.removeKeyListener(keyFocuslistener);
					widget.removeMouseListener(mouseFocuslistener);
					widget.removeMouseWheelListener(mouseWheelListener);
					widget.removeMouseMoveListener(mouseMoveFocuslistener);
					mouseCoords.clear();
					mouseCoords = null;
				}
			});
			registeredWidgets.remove(widget);
		}
	}

	/**
	 * Registers the given workbench window to be tracked.
	 * 
	 * @param window
	 *            The workbench window.
	 */
	private void register(IWorkbenchWindow window) {
		for (IWorkbenchPage page : window.getPages()) {
			for (IEditorReference ref : page.getEditorReferences()) {
				IEditorPart editor = ref.getEditor(false);
				if (editor instanceof JavaEditor) {
					register((JavaEditor) editor);
				}
			}
		}
	}

	/**
	 * Registers the given editor to be tracked. Has no effect if the editor is
	 * already registered.
	 * 
	 * @param editor
	 *            The editor.
	 */
	private synchronized void register(final JavaEditor editor) {
		final StyledText widget = editor.getViewer().getTextWidget();
		if (!registeredWidgets.contains(widget)) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					mouseCoords = new ArrayList<Integer[]>();
					widget.addKeyListener(keyFocuslistener);
					widget.addMouseListener(mouseFocuslistener);
					widget.addMouseWheelListener(mouseWheelListener);
					widget.addMouseMoveListener(mouseMoveFocuslistener);
				}
			});			
			registeredWidgets.add(widget);
		}
	}
	
	private void checkAndDeleteZippedSurveyFile() {
		try {
			String zipFilePath = FilenameUtils.concat(
					System.getProperty("user.home"), "data_devFatigue.zip");
			File f = new File(zipFilePath);
			if (f.exists())
				f.delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void uploadPendingData() {
		try {
			if (SFTPConnection.uploadInProgress)
				return;
			String pending = checkForPendingData();
			if (pending == null)
				return;
			Thread sftpThread = new Thread(new SFTPConnection(pending));
			sftpThread.start();
			/*
			 * boolean dataUpload = SurveyStorage.sftpUpload(pending);
			 * if(dataUpload) { SurveyPlugin.updateDateUploadStatus(pending); }
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String checkForPendingData() {
		return SurveyPlugin.checkForPendingData();
	}

	private void checkForDailySurvey() {
		DateTime curr = new DateTime();
		if(SurveyPlugin.checkDate(curr)) {
			return;
		}
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yy ");
		String date = fmt.print(curr);
		DateTime thresholdTime;
		
		if (XmlPlugin.getDefault().getSurveyTimePeriod() == 0) {
			date = date.concat("12:00");
		} else if (XmlPlugin.getDefault().getSurveyTimePeriod() == 1) {
			date = date.concat("16:00");
		} else if (XmlPlugin.getDefault().getSurveyTimePeriod() == 2) {
			date = date.concat(XmlPlugin.getDefault().getSurveyFixedTimePeriod());
		}
		date = date.concat(":00");
		DateTimeFormatter datefmt = DateTimeFormat.forPattern("dd-MMM-yy HH:mm:ss");
		thresholdTime = datefmt.parseDateTime(date);
		
		if(curr.isAfter(thresholdTime)) {
			try {
				IWorkbenchPage activePage = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				if (activePage.findView(DailyFormView.ID) == null) {
					// open the view
					activePage.showView(DailyFormView.ID);
					// and maximize it
					activePage.toggleZoom(activePage
							.findViewReference(DailyFormView.ID));
				}
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void openSurveyOnNewSession() {
		if(lastActive == null) {
			lastActive = new DateTime();
			try {
				IWorkbenchPage activePage = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				if (activePage.findView(SessionFormView.ID) == null) {
					// open the view
					activePage.showView(SessionFormView.ID);
					// and maximize it
					activePage.toggleZoom(activePage
							.findViewReference(SessionFormView.ID));
				}
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			return;
		}
		else {
			DateTime curr = new DateTime();
			Interval diff = new Interval(lastActive, curr);
			long diffInMins = diff.toDurationMillis() / 60000;
			lastActive = curr;
			if(diffInMins>=15.00) {
				try {
					IWorkbenchPage activePage = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					if (activePage.findView(SessionFormView.ID) == null) {
						// open the view
						activePage.showView(SessionFormView.ID);
						// and maximize it
						activePage.toggleZoom(activePage
								.findViewReference(SessionFormView.ID));
					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			return;
		}
	  }
}
