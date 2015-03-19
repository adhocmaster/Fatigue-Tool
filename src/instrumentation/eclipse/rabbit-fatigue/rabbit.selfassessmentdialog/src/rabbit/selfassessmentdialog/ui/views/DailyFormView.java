package rabbit.selfassessmentdialog.ui.views;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import rabbit.data.internal.xml.XmlPlugin;
import rabbit.selfassessmentdialog.SurveyPlugin;
import rabbit.selfassessmentdialog.internal.SFTPConnection;
import rabbit.selfassessmentdialog.internal.SurveyStorage;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class DailyFormView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "rabbit.selfassessmentdialog.ui.views.DailyFormView";

	private FormToolkit toolkit;
	private ScrolledForm form;
	
	private ArrayList<Object> results;
	private String version;

	/**
	 * The constructor.
	 */
	public DailyFormView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		DateTime curr = new DateTime();
		form.setText("DevFatigue Self-Assessment - " + fmt.print(curr));
		GridLayout layout = new GridLayout();
		form.getBody().setLayout(layout);
		
		results = new ArrayList<Object>();
		version = "";
		
		/*Hyperlink link = toolkit.createHyperlink(form.getBody(), "Click here.",
				SWT.WRAP);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link activated!");
			}
		});*/

		layout.numColumns = 1;
		int columnSize = 400;
		
		String filePath = downloadAndReturnFile(); 
		
		File fXmlFile = new File(filePath);
		//File fXmlFile = new File("F:\\Study_Time\\Courses\\Thesis\\Literature\\Sleep\\Test\\survey.xml");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	 
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
		//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		version = doc.getDocumentElement().getAttribute("version");
		 
		NodeList quesList = doc.getElementsByTagName("question");
	 
		System.out.println("----------------------------");
	 
		for (int temp = 0; temp < quesList.getLength(); temp++) {
	 
			Node quesNode = quesList.item(temp);
	 
			if (quesNode.getNodeType() == Node.ELEMENT_NODE) {
	 
				Element eElement = (Element) quesNode;
				
				String question = "Q" + (temp+1) + ". " +eElement.getElementsByTagName("quesText").item(0).getTextContent();
				String type = eElement.getAttribute("type");
				if(type.equals("text")) {
					Label label = new Label(form.getBody(), SWT.NULL);
					label.setText(question);
					label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					Text text = new Text(form.getBody(), SWT.BORDER);
					
					GridData textGridData = new GridData(GridData.FILL_HORIZONTAL);
			        textGridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
			        textGridData.widthHint = columnSize;
			        text.setLayoutData(textGridData);
					results.add(text);
				}
				else if(type.equals("radio")) {
					Label label = new Label(form.getBody(), SWT.NULL);
					label.setText(question);
					label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					Composite composite = new Composite(form.getBody(), SWT.NULL);
					composite.setLayout(new RowLayout());
					composite.setLayoutData(new GridData());
					composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					int numOfOptions = eElement.getElementsByTagName("option").getLength();

					final Text text = new Text(form.getBody(), SWT.BORDER);
					text.setVisible(false);
					Button buttons[] = new Button[numOfOptions];
					for(int index = 0; index<numOfOptions;index++) {
						buttons[index] = new Button(composite, SWT.RADIO);
						buttons[index].setText(eElement.getElementsByTagName("option").item(index).getTextContent());
						buttons[index].setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
						buttons[index].addSelectionListener(new SelectionListener() {
							
							@Override
							public void widgetSelected(SelectionEvent e) {
								Button curr = (Button) e.getSource();
								if(curr.getSelection()) {
									text.setText(curr.getText());
								}
								
							}
							
							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
								// TODO Auto-generated method stub
								
							}
						});
					}
					results.add(text);
				}
				else if(type.equals("combo")) {
					Label label = new Label(form.getBody(), SWT.NULL);
					label.setText(question);
					label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					
					Combo comboDropDown = new Combo(form.getBody(), SWT.DROP_DOWN | SWT.BORDER);
					comboDropDown.setLayoutData(new GridData());
					int numOfOptions = eElement.getElementsByTagName("option").getLength();
					for(int index = 0; index<numOfOptions;index++) {
						comboDropDown.add(eElement.getElementsByTagName("option").item(index).getTextContent());
					}
					comboDropDown.select(0);
					results.add(comboDropDown);
				}
				else if(type.equals("slider")) {
					Label topicLabel = new Label(form.getBody(), SWT.NULL);
					Font font = new Font(topicLabel.getDisplay(), "Arial", 12, SWT.BOLD);
					topicLabel.setFont(font);
					topicLabel.setText(eElement.getElementsByTagName("topic").item(0).getTextContent());
					topicLabel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					
					GridData labelGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		            labelGridData.widthHint = columnSize;
					Label label = new Label(form.getBody(), SWT.WRAP | SWT.LEFT);
					label.setText(question);
					label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					label.setLayoutData(labelGridData);
					
					Composite composite = new Composite(form.getBody(), SWT.NULL);
					composite.setLayout(new RowLayout());
					composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					Label label1 = new Label(composite, SWT.NULL);
					label1.setText("Low : 0");
					label1.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					final Scale scale = new Scale (composite, SWT.BORDER);
					//scale.setSize (200, 64);
					scale.setMaximum (10);
					
					scale.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					Label label2 = new Label(composite, SWT.NULL);
					label2.setText("High : 10");
					label2.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					Label labelTmp = new Label(composite, SWT.NULL);
					labelTmp.setText("        ");
					labelTmp.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					final Text text = new Text(composite, SWT.BORDER);
					text.setEditable(false);
					
					GridData textGridData = new GridData(GridData.FILL_HORIZONTAL);
			        textGridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
			        textGridData.widthHint = 3;
			        RowData textRowData = new RowData();
			        textRowData.width = 30;
			        text.setLayoutData(textRowData);
			        text.setText("0");
					results.add(text);
					scale.addSelectionListener(new SelectionListener() {
						
						public void widgetSelected(SelectionEvent e) {
							text.setText(String.valueOf(scale.getSelection()));
							
						}
						
						public void widgetDefaultSelected(SelectionEvent e) {
							// TODO Auto-generated method stub
							
						}
					});
				}
			}
		}
		
		Button submit = new Button(form.getBody(), SWT.NULL);
		submit.setText("Submit and Upload");
		
		submit.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
					DateTimeFormatter fmt1 = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss-SS");
					final DateTime curr = new DateTime();
					String file = "DailyLog_" + fmt.print(curr);
					//Consolidate the result
					String text = "";
					{
						text = text.concat(fmt1.print(curr));
						text = text.concat("\n");
						
						text = text.concat(version);
						text = text.concat("\n");
						
						for(Object result : results) {
							if(result instanceof Text) {
								text = text.concat(((Text) result).getText());
								text = text.concat("\n");
							}
							else if(result instanceof Combo) {
								Combo com = (Combo) result;
								text = text.concat(com.getItem(com.getSelectionIndex()));
								text = text.concat("\n");
							}
							else if(result instanceof String) {
								text = text.concat((String) result);
								text = text.concat("\n");
							}
						}
					}
					
					boolean surveyResultSaved = SurveyStorage.writeFile(file, text);
					if(surveyResultSaved) {
						SurveyPlugin.updateDateLog(curr);
						
						//Upload on FTP
						Job job = new Job("Upload Usage Data") {

							@Override
							protected IStatus run(final IProgressMonitor monitor) {
								
								// set total number of work units
								monitor.beginTask("Uploading Data", 100);
								monitor.worked(20);
								if(SFTPConnection.uploadInProgress) {
									monitor.done();
									return Status.OK_STATUS;
								}
								Thread sftpThread = new Thread(new SFTPConnection(fmt.print(curr)));
								sftpThread.start();
								/*final boolean dataUpload = SurveyStorage.sftpUpload(fmt.print(curr));*/
								monitor.worked(50);
								if (monitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
								monitor.done();			
								return Status.OK_STATUS;
							}							
						};
						job.schedule();
					}
					else {
						MessageBox msg = new MessageBox(new Shell());
						msg.setMessage("Error saving survey results on drive!");
						msg.open();
					}
					
					IWorkbenchPage activePage = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					IViewPart myView=activePage.findView(ID);
					if (myView != null) {
						// close the view
						activePage.hideView(myView);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		/*viewer.getControl().setFocus();*/
		form.setFocus();
	}
	
	@SuppressWarnings("restriction")
	public String downloadAndReturnFile() {
		String folder = /*FilenameUtils.concat(XmlPlugin.getDefault()
				.getStoragePathRoot().toOSString(), "Assessment");*/
				XmlPlugin.getDefault()
				.getStoragePathRoot().toOSString();
		File dir = new File(folder);
		String str = FilenameUtils.concat(folder,
		        "survey.xml");
		String strTemp = FilenameUtils.concat(folder,
		        "temp.xml");
		// Create the new directory and check read/write permissions:
	    boolean dirCreated = dir.exists();
	    if (!dirCreated) {
	      dirCreated = dir.mkdirs();
	    }
	    if (!dirCreated || !dir.canRead() || !dir.canWrite()) {
	      MessageDialog.openError(new Shell(), "Error", "Error occurred while " +
	      		"accessing the new directory, please select another DevFatigue directory.");
	      return null;
	    }
		
		boolean downloaded = SurveyStorage.saveUrl(strTemp, "http://www4.ncsu.edu/~ssarkar4/fatigue/sleep/survey.xml");
		
		if(downloaded) {
			try {
				FileUtils.copyFile(new File(strTemp),new File(str));
				FileUtils.deleteQuietly(new File(strTemp));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return str;
	}
}