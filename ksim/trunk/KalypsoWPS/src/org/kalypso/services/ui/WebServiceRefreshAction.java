package org.kalypso.services.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.kalypso.services.IServiceEntry;

public class WebServiceRefreshAction implements IObjectActionDelegate
{
  private List<IServiceEntry> m_entries;
  private IViewPart m_part;

  /**
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
   *      org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart( final IAction action, final IWorkbenchPart targetPart )
  {
    m_part  = ((IViewPart)targetPart);
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run( final IAction action )
  {
    for( IServiceEntry serviceEntry : m_entries )
    {
      serviceEntry.refreshFromService();
    }
    if(m_part instanceof ServicesView) {
      ((ServicesView)m_part).refresh();
    }
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( final IAction action, final ISelection selection )
  {
    m_entries = new ArrayList<IServiceEntry>();
    final IStructuredSelection sselection = (IStructuredSelection) selection;
    final Iterator it = sselection.iterator();
    while( it.hasNext() )
    {
      m_entries.add( (IServiceEntry) it.next() );
    }
  }

}
