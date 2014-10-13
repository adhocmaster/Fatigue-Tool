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

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;

import java.util.ArrayList;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import rabbit.data.handler.DataHandler;
import rabbit.data.store.IStorer;
import rabbit.data.store.model.FocusEvent;
import rabbit.tracking.internal.util.WorkbenchUtil;

import com.google.common.collect.Sets;

/**
 * Tracks time spent on Java elements such as classes, methods.
 */
@SuppressWarnings("restriction")
public class FocusMouseTracker extends AbstractTracker<FocusEvent> {

	/**
	 * A set of all text widgets that are currently being listened to. This set
	 * is not synchronised.
	 */
	private final Set<StyledText> registeredParts;	
	private boolean mouseUpFlag = true;
	private ArrayList<Integer[]> mouseCoords = new ArrayList<Integer[]>();
	DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yy hh.mm.ss aa");
	
	/**
	 * Listener to listen to mouse input on text widgets of
	 * editors.
	 */
	private final MouseListener mouseFocuslistener = new MouseListener() {

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			DateTime curr = new DateTime();
			addData(new FocusEvent(curr, fmt.print(curr), "Mouse Double Click"));			
		}

		@Override
		public void mouseDown(MouseEvent e) {
			DateTime curr = new DateTime();
			addData(new FocusEvent(curr, fmt.print(curr), "Mouse Down"));
			mouseUpFlag = false;
			calulateMouseVelocity();
			mouseCoords.clear();
		}

		@Override
		public void mouseUp(MouseEvent e) {
			DateTime curr = new DateTime();
			addData(new FocusEvent(curr, fmt.print(curr), "Mouse Up"));
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
				double temp = Math.sqrt(Math.pow((coord2[0] - coord1[0]), 2.0) + Math.pow((coord2[1] - coord1[1]), 2.0));
				sum += temp;
			}			
			addData(new FocusEvent(curr, fmt.print(curr), "Mouse Velocity - " + sum));
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
		}
	};

	/**
	 * Constructor.
	 */
	public FocusMouseTracker() {
		super();
		registeredParts = Sets.newHashSet();
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
		IWorkbenchWindow win = WorkbenchUtil.getActiveWindow();		
	    if (WorkbenchUtil.isActiveShell(win)) {
	    	register(win);
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
		if (registeredParts.contains(widget)) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					widget.removeMouseListener(mouseFocuslistener);
					widget.removeMouseMoveListener(mouseMoveFocuslistener);
					mouseCoords.clear();
					mouseCoords = null;
				}
			});
			registeredParts.remove(widget);
		}
	}


	/**
	 * Registers the given editor to be tracked. Has no effect if the editor is
	 * already registered.
	 * 
	 * @param iWorkbenchPart
	 *            The editor.
	 */
	private synchronized void register(IWorkbenchWindow window) {
		
		window.getPartService().getActivePart().getSite().getShell().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				System.out.println("hello");
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		/*if (!registeredParts.contains(iWorkbenchPart)) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					mouseCoords = new ArrayList<Integer[]>();
					widget.addMouseListener(mouseFocuslistener);
					widget.addMouseMoveListener(mouseMoveFocuslistener);
				}
			});			
			registeredParts.add(widget);
		}*/
	}
}
