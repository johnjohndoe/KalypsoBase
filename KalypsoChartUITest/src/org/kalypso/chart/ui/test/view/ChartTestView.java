package org.kalypso.chart.ui.test.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.kalypso.chart.ui.view.ChartView;

/**
 * 
 * a view which automatically displays the first chart of a selected .kod file
 * 
 * @author burtscher1
 */
public class ChartTestView extends ChartView implements ISelectionListener {

	private IFile m_file = null;

	public ChartTestView() {

	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {
		getSite().getPage().addSelectionListener(this);
		super.createPartControl(parent);
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof TreeSelection) {
			TreeSelection ts = (TreeSelection) selection;
			Object firstElement = ts.getFirstElement();
			if (firstElement instanceof IFile) {
				System.out.println();
				IFile file = (IFile) firstElement;
				if (file.equals(m_file)) {
					return;
				}
				if (file.getFileExtension().equals("kod")) {
					m_file = file;
					setInput(file);
				}
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		getSite().getPage().removeSelectionListener(this);
	}
}
