package org.bce.eclipse.jface.viewers;

import org.bce.eclipse.swt.widgets.AbstractControlTooltipListener;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableViewerTooltipListener extends AbstractControlTooltipListener
{
  /** Creates the listener and hooks it to the table. 
   * @param alwaysVisible */
  public final static void hookViewer( final TableViewer viewer, final boolean alwaysVisible )
  {
    final AbstractControlTooltipListener tableListener = new TableViewerTooltipListener( viewer, alwaysVisible );
    hookListener( viewer.getControl(), tableListener );
  }

  private final TableViewer m_viewer;

  private TableViewerTooltipListener( final TableViewer viewer, final boolean alwaysVisible )
  {
    super( viewer.getControl().getShell(), alwaysVisible );
    m_viewer = viewer;
  }

  @Override
  protected String getTooltipForEvent( final Event event )
  {
    final Table table = m_viewer.getTable();
    final TableItem item = table.getItem( new Point( event.x, event.y ) );
    
    final IBaseLabelProvider labelProvider = m_viewer.getLabelProvider();
    if( item != null && labelProvider instanceof ITooltipProvider )
    {
      final ITooltipProvider tp = (ITooltipProvider)labelProvider;

      return tp.getTooltip( item.getData() );
    }
    
    return null;
  }
}
