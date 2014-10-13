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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.joda.time.DateTime;

import rabbit.data.handler.DataHandler;
import rabbit.data.store.IStorer;
import rabbit.data.store.model.ProjectEvent;

import com.google.common.collect.Sets;

/**
 * Tracks command executions.
 */
public class ImportTracker extends AbstractTracker<ProjectEvent> implements
    IExecutionListener {

  /*
   * We only record commands that have been successfully executed, therefore we
   * only use postExecuteSuccess(String, Object) to record the event.
   * 
   * Note that preExecute(String, ExecutionEvent) will always be called when a
   * command is called to be execute, even if the command is non-executable at
   * that moment. For example, when there is nothing to undo in an editor, the
   * "Undo" menu is disabled, but if the user uses Ctrl+Z, the undo command will
   * still be called. Therefore we don't use preExecute(String, ExecutionEvent).
   */

  /**
   * The last recorded ExecutionEvent, to be updated every time
   * {@link #preExecute(String, ExecutionEvent)} is called. We need this because
   * {@link #postExecuteSuccess(String, Object)} does not have an
   * {@link ExecutionEvent} parameter. This variable is null from the beginning.
   */
  private ExecutionEvent lastEvent;
  private final Set<String> registeredImports;
  private String saveCommand = "org.eclipse.ui.file.save";
  
  /** Constructor. */
  public ImportTracker() {
    super();
    registeredImports = Sets.newHashSet();
    lastEvent = null;
  }

  @Override
  public void notHandled(String commandId, NotHandledException exception) {
  }

  @Override
  public void postExecuteFailure(String commandId, ExecutionException e) {
  }

  @Override
  public void postExecuteSuccess(String commandId, Object returnValue) {
	if (lastEvent != null && lastEvent.getCommand().getId().equals(commandId)) {
      addData(new ProjectEvent(new DateTime(), lastEvent.getCommand().getId(), "Command"));
	}
	if (lastEvent != null && lastEvent.getCommand().getId().equals(commandId) && commandId.equals(saveCommand)) {
      if(FileTracker.lastFile!=null && FileTracker.lastFile.toString().contains(".java")) {
    	  IWorkspace workspace = ResourcesPlugin.getWorkspace();
    	    IWorkspaceRoot root = workspace.getRoot();
    	    // Get all projects in the workspace
    	    IProject[] projects = root.getProjects();
    	    // Loop over all projects
    	    for (IProject project : projects) {
    	      try {
    	    	  if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
    	    	      IJavaProject javaProject = JavaCore.create(project);
    	    	      IPackageFragment[] packages = javaProject.getPackageFragments();
    	    	      for (IPackageFragment mypackage : packages) {
    	    	        if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
    	    	          for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
    	    	        	  String currUnit = "";
    	    	        	  if (mypackage.getElementName().equals("")) {
    	    	        		  currUnit = "/" + project.getName() + "/src/" + unit.getElementName();
    	    	        	  }
    	    	        	  else {
    	    	        		  currUnit = "/" + project.getName() + "/src/" + mypackage.getElementName() + "/" + unit.getElementName();
    	    	        	  }
    	    	        	  if (currUnit.equals(FileTracker.lastFile.toString())) {
    	    	        		 for (IImportDeclaration dec : unit.getImports()) {
    	    	        		  	String importName = FileTracker.lastFile.toString() + "}";
    	    	        		  	importName += dec.getElementName();
    	    	        		  	if (!registeredImports.contains(importName)) {
    	    	        		  		DateTime curr = new DateTime();
    	    	        				addData(new ProjectEvent(curr, importName, "Import"));
    	    	        		  		registeredImports.add(importName);
    	    	        		  	}
    	    	        		 }
    	    	        		 break;
    	    	        	  }
    	    	           }

    	    	        }

    	    	      }
    	    	    }
    	      } catch (CoreException e) {
    	        e.printStackTrace();
    	      }
    	    }
      }
    }
  }

  @Override
  public void preExecute(String commandId, ExecutionEvent event) {
    lastEvent = event;
  }

  @Override
  protected IStorer<ProjectEvent> createDataStorer() {
    return DataHandler.getStorer(ProjectEvent.class);
  }

  @Override
  protected void doDisable() {
    getCommandService().removeExecutionListener(this);
  }

  @Override
  protected void doEnable() {
    getCommandService().addExecutionListener(this);
  }

  private ICommandService getCommandService() {
    return (ICommandService) PlatformUI.getWorkbench().getService(
        ICommandService.class);
  }

}
