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

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.joda.time.DateTime;

import rabbit.data.handler.DataHandler;
import rabbit.data.store.IStorer;
import rabbit.data.store.model.ActivityEvent;

/**
 * Tracks workbench part usage.
 */
public class ActivityTracker extends AbstractPartTracker<ActivityEvent> {

	/**
	 * Constructor.
	 */
	public ActivityTracker() {
		super();
	}

	@Override
	protected IStorer<ActivityEvent> createDataStorer() {
		return DataHandler.getStorer(ActivityEvent.class);
	}

	@Override
	protected ActivityEvent tryCreateEvent(long start, long end,
			IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			final ITextEditor editor = (ITextEditor) part;
			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument doc = provider.getDocument(editor.getEditorInput());
			if (doc == null) {
				return null;
			}

			doc.addDocumentListener(new IDocumentListener() {

				@Override
				public void documentChanged(DocumentEvent event) {
					/*System.out.println("doc updated + " + event.getText() + " "
							+ event.getModificationStamp());*/
					addData(new ActivityEvent(new DateTime(), editor));
				}

				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
					// TODO Auto-generated method stub
				}
			});
			
		}
		return null;
	}
}
