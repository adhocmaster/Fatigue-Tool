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

import rabbit.data.handler.DataHandler;
import rabbit.data.store.IStorer;
import rabbit.data.store.model.ActivityEvent;
import rabbit.data.store.model.FocusEvent;
import rabbit.data.store.model.JavaEvent;
import rabbit.tracking.internal.IdleDetector;
import rabbit.tracking.internal.TrackingPlugin;
import rabbit.tracking.internal.util.Recorder;
import rabbit.tracking.internal.util.WorkbenchUtil;

import com.google.common.collect.Sets;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Tracks time spent on Java elements such as classes, methods.
 */
@SuppressWarnings("restriction")
public class FocusTracker extends AbstractTracker<FocusEvent> {

	/**
	 * A set of all text widgets that are currently being listened to. This set
	 * is not synchronised.
	 */
	private final Set<StyledText> registeredWidgets;

	/**
	 * Listener to listen to keyboard input and mouse input on text widgets of
	 * editors.
	 */
	private final Listener focuslistener = new Listener() {

		// This listener used to provide compatibility with Eclipse 3.4,
		// otherwise
		// org.eclipse.swt.custom.CaretListener might be a better option
		// (Eclipse
		// * 3.5+).

		@Override
		public void handleEvent(Event event) {
			IWorkbenchWindow win = WorkbenchUtil.getActiveWindow();
			if (WorkbenchUtil.isActiveShell(win)) {
				//System.out.println("click/key");
				focusActivityAddData();
			}
		}
	};
	
	private void focusActivityAddData() {
		DateTime curr = new DateTime();
		//System.out.println(curr.getHourOfDay());
		addData(new FocusEvent(curr, String.valueOf(curr.getHourOfDay())));	
	}

	/**
	 * Constructor.
	 */
	public FocusTracker() {
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
					widget.removeListener(SWT.KeyDown, focuslistener);
					widget.removeListener(SWT.MouseDown, focuslistener);
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
					widget.addListener(SWT.KeyDown, focuslistener);
					widget.addListener(SWT.MouseDown, focuslistener);
				}
			});			
			registeredWidgets.add(widget);
		}
	}
}
