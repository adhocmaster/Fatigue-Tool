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
package rabbit.selfassessmentdialog.ui.pref;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import rabbit.data.internal.xml.XmlPlugin;

@SuppressWarnings("restriction")
public class SurveyPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage {

  private Button start, end, fixed;
  //private Text time;
  private Spinner hour, minute;
  private Label colon;

  public SurveyPreferencePage() {
  }

  public SurveyPreferencePage(String title) {
    super(title);
  }

  public SurveyPreferencePage(String title, ImageDescriptor image) {
    super(title, image);
  }

  @Override
  public void init(IWorkbench workbench) {
  }

@Override
  public boolean performOk() {
	  if(start.getSelection()) {
		  XmlPlugin.getDefault().setSurveyTimePeriod(0);
		  XmlPlugin.getDefault().setSurveyFixedTimePeriod("12:00");
	  }
	  else if(end.getSelection()) {
		  XmlPlugin.getDefault().setSurveyTimePeriod(1);
		  XmlPlugin.getDefault().setSurveyFixedTimePeriod("16:00");
	  }
	  else if(fixed.getSelection()) {
		  XmlPlugin.getDefault().setSurveyTimePeriod(2);
		  XmlPlugin.getDefault().setSurveyFixedTimePeriod(hour.getSelection() + ":" + minute.getSelection());
	  }
    return true;
  }

@Override
  protected Control createContents(Composite parent) {
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    Composite cmp = new Composite(parent, SWT.NONE);
    cmp.setLayout(layout);
    cmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    // Contains settings for storage location:
    Group pathGroup = new Group(cmp, SWT.NONE);
    pathGroup.setText("Daily Survey Settings");
    pathGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
    pathGroup.setLayout(new GridLayout(3, false));
    {
      Label description = new Label(pathGroup, SWT.WRAP);
      description.setText("Please select a time period when you want the survey to show up" +
      		" or you can choose 'Fixed' and it will be according to you.");
      GridDataFactory.fillDefaults().span(3, 1).applyTo(description);

      start = new Button(pathGroup, SWT.RADIO);
      start.setText("Start of the Day");
      end = new Button(pathGroup, SWT.RADIO);
      end.setText("End of the Day");
      fixed = new Button(pathGroup, SWT.RADIO);
      fixed.setText("Fixed");
     /*time = new Text(pathGroup, SWT.BORDER);
      time.setText(XmlPlugin.getDefault().getSurveyFixedTimePeriod());
      time.setVisible(false);*/
      String[] time = XmlPlugin.getDefault().getSurveyFixedTimePeriod().split("[:]");
      Composite timeSpinnerCmp = new Composite(pathGroup, SWT.NONE);
      GridLayout timeSpinnerLayout = new GridLayout();
      timeSpinnerLayout.numColumns = 3;
      timeSpinnerCmp.setLayout(timeSpinnerLayout);
      
      hour = new Spinner (timeSpinnerCmp, SWT.BORDER);
      hour.setMinimum(00);
      hour.setMaximum(23);
      hour.setSelection(00);
      hour.setIncrement(1);
      hour.setVisible(false);
      hour.setTextLimit(2);
      colon = new Label(timeSpinnerCmp, SWT.NULL);
      colon.setText(":");
      colon.setVisible(false);
      hour.setSelection(Integer.parseInt(time[0]));
      minute = new Spinner (timeSpinnerCmp, SWT.BORDER);
      minute.setMinimum(00);
      minute.setMaximum(59);
      minute.setSelection(00);
      minute.setIncrement(1);
      minute.setTextLimit(2);
      minute.setVisible(false);
      minute.setSelection(Integer.parseInt(time[1]));
      
      start.addSelectionListener(new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			//time.setVisible(false);
			minute.setVisible(false);
			hour.setVisible(false);
			colon.setVisible(false);
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
      });

      end.addSelectionListener(new SelectionListener() {
  		
  		@Override
  		public void widgetSelected(SelectionEvent e) {
  			//time.setVisible(false);
  			minute.setVisible(false);
			hour.setVisible(false);
			colon.setVisible(false);
  		}
  		
  		@Override
  		public void widgetDefaultSelected(SelectionEvent e) {
  			// TODO Auto-generated method stub
  			
  		}
      });
      
      fixed.addSelectionListener(new SelectionListener() {
  		
  		@Override
  		public void widgetSelected(SelectionEvent e) {
  			//time.setVisible(true);
  			minute.setVisible(true);
			hour.setVisible(true);
			colon.setVisible(true);
  		}
  		
  		@Override
  		public void widgetDefaultSelected(SelectionEvent e) {
  			// TODO Auto-generated method stub
  			
  		}
      });
      
      if(XmlPlugin.getDefault().getSurveyTimePeriod() == 0) {
		  start.setSelection(true);
		  end.setSelection(false);
		  fixed.setSelection(false);
		  //time.setVisible(false);
		  minute.setVisible(false);
		  hour.setVisible(false);
		  colon.setVisible(false);
	  }
	  else if(XmlPlugin.getDefault().getSurveyTimePeriod() == 1) {
		  start.setSelection(false);
		  end.setSelection(true);
		  fixed.setSelection(false);
		  //time.setVisible(false);
		  minute.setVisible(false);
		  hour.setVisible(false);
		  colon.setVisible(false);
	  }
	  else if(XmlPlugin.getDefault().getSurveyTimePeriod() == 2) {
		  start.setSelection(false);
		  end.setSelection(false);
		  fixed.setSelection(true);
		  //time.setVisible(true);
		  minute.setVisible(true);
		  hour.setVisible(true);
		  colon.setVisible(true);
	  }

    }
    return cmp;
  }

  @Override
	protected void performDefaults() {
		start.setSelection(false);
		end.setSelection(false);
		fixed.setSelection(true);
		/*time.setVisible(true);
		time.setText("14:00");*/
		minute.setVisible(true);
		hour.setVisible(true);
		colon.setVisible(true);
		hour.setSelection(14);
		minute.setSelection(00);
		super.performDefaults();
	}
}
