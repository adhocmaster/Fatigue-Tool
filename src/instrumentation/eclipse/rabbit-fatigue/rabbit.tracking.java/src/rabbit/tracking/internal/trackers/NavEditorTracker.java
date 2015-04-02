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

import java.util.Set;

import javax.annotation.Nullable;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
import rabbit.data.store.model.NavEvent;
import rabbit.tracking.internal.util.WorkbenchUtil;

import com.google.common.collect.Sets;

/**
 * Tracks time spent on Java elements such as classes, methods.
 */
@SuppressWarnings("restriction")
public class NavEditorTracker extends AbstractTracker<NavEvent> {

	/**
	 * A set of all text widgets that are currently being listened to. This set
	 * is not synchronised.
	 */
	private final Set<StyledText> registeredWidgets;
	DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yy hh.mm.ss.SS aa");
	
	/**
	  * Listener to listen to keyboard input on text widgets of
	  * editors.
	  */
	private final KeyListener keyListener = new KeyListener() {
		
		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.keyCode == SWT.ARROW_UP
					|| e.keyCode == SWT.ARROW_DOWN
					|| e.keyCode == SWT.ARROW_RIGHT
					|| e.keyCode == SWT.ARROW_LEFT) {
				if (e.getSource() instanceof StyledText) {
					StyledText widget = (StyledText) e.getSource();
					DateTime curr = new DateTime();
					addData(new NavEvent(curr, fmt.print(curr), NavFileTracker.lastFile + " - Line No. " + (widget.getLineAtOffset(widget.getCaretOffset()) + 1)));
				}				
				methodTrace();
			}
		}
	};
	
	/**
	  * Listener to listen to mouse input on text widgets of
	  * editors.
	  */
	private final MouseListener mouseListener = new MouseListener() {
		
		@Override
		public void mouseUp(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void mouseDown(MouseEvent e) {
			if (e.getSource() instanceof StyledText) {
				StyledText widget = (StyledText) e.getSource();
				DateTime curr = new DateTime();
				addData(new NavEvent(curr, fmt.print(curr), NavFileTracker.lastFile + " - Line No. " + (widget.getLineIndex(e.y) + 1)));
			}
			methodTrace();			
		}
		
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private void methodTrace() {
		IWorkbenchWindow win = WorkbenchUtil.getActiveWindow();
		if (WorkbenchUtil.isActiveShell(win)) {
			final IWorkbenchPart activePart = win.getPartService()
					.getActivePart();

			if (!(activePart instanceof JavaEditor)) {
				return;
			}

			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					IJavaElement element = null;
					try {
						element = SelectionConverter
								.getElementAtOffset((JavaEditor) activePart);
						if (element != null) {
							filterData(element);
						}
					} catch (JavaModelException e) {
						// Nothing we can do.
						System.err.println(getClass().getSimpleName()
								+ e.getMessage());
					}
				}
			});
		}
	}
	
	private void filterData(IJavaElement e) {
		if (e.exists()) {
			DateTime curr = new DateTime();
			IJavaElement actual = null;
			try {
				actual = filterElement(e);
			} catch (JavaModelException ex) {
				actual = null;
				ex.printStackTrace();
			}

			if (actual != null) {
				addData(new NavEvent(curr, fmt.print(curr),
						NavFileTracker.lastFile + " - Method - " + actual.getElementName()));
			}
		}

	}
	
	private IJavaElement filterElement(@Nullable IJavaElement element)
			throws JavaModelException {

		if (element == null) {
			return null;
		}

		if (element.getElementType() == IJavaElement.METHOD) {
			if (((IType) element.getParent()).isAnonymous()) {
				return filterElement(element.getParent());
			}
			return element;
		}

		return null;
	}

	
	/**
	 * Constructor.
	 */
	public NavEditorTracker() {
		super();
		registeredWidgets = Sets.newHashSet();
	}

	@Override
	protected IStorer<NavEvent> createDataStorer() {
		return DataHandler.getStorer(NavEvent.class);
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
			        widget.removeKeyListener(keyListener);
					widget.removeMouseListener(mouseListener);
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
					widget.addKeyListener(keyListener);
					widget.addMouseListener(mouseListener);
				}
			});			
			registeredWidgets.add(widget);
		}
	}
}
