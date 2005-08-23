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
  /** Creates the listener and hooks it to the table. */
  public final static void hookControl( final TableViewer viewer )
  {
    final AbstractControlTooltipListener tableListener = new TableViewerTooltipListener( viewer );
    hookListener( viewer.getControl(), tableListener );
  }

  private final TableViewer m_viewer;

  public TableViewerTooltipListener( final TableViewer viewer )
  {
    super( viewer.getControl().getShell() );
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
