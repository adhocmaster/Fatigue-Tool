package rabbit.selfassessmentdialog.ui.views;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
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

public class SessionFormView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "rabbit.selfassessmentdialog.ui.views.SessionFormView";

	private FormToolkit toolkit;
	private ScrolledForm form;

	
	/**
	 * The constructor.
	 */
	public SessionFormView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("DevFatigue Self-Assessment");
		GridLayout layout = new GridLayout();
		form.getBody().setLayout(layout);

		layout.numColumns = 1;
		int columnSize = 400;
		
		Label topicLabel = new Label(form.getBody(), SWT.NULL);
		Font font = new Font(topicLabel.getDisplay(), "Arial", 12, SWT.BOLD);
		topicLabel.setFont(font);
		topicLabel.setText("Last Session - Fatigue Level");
		topicLabel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		
		GridData labelGridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        labelGridData.widthHint = columnSize;
		Label label = new Label(form.getBody(), SWT.WRAP | SWT.LEFT);
		label.setText("How tired, exhausted, lethargic, sleepy vs. fresh, incapable and unproductive did you feel during the last task?");
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
		scale.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				text.setText(String.valueOf(scale.getSelection()));
				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Button submit = new Button(form.getBody(), SWT.NULL);
		submit.setText("Submit");
		
		submit.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss-SS");
					String file = "SessionLog-" + fmt.print(new DateTime());
					SurveyStorage.writeFile(file, text.getText());
					IWorkbenchPage activePage = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					IViewPart myView=activePage.findView(ID);
					if (myView != null) {
						// close the view
						activePage.hideView(myView);
					}
				} catch (Exception ex) {
					// TODO Auto-generated catch block
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
}