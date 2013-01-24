package org.kalypso.contribs.eclipse.jface.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.kalypso.contribs.eclipse.swt.widgets.AbstractControlTooltipListener;

public class ColumnViewerTooltipListener extends AbstractControlTooltipListener
{
  /**
   * Creates the listener and hooks it to the table.
   * 
   * @param alwaysVisible
   */
  public final static void hookViewer( final ColumnViewer viewer, final boolean alwaysVisible )
  {
    final AbstractControlTooltipListener tableListener = new ColumnViewerTooltipListener( viewer, alwaysVisible );
    hookListener( viewer.getControl(), tableListener );
  }

  private final ColumnViewer m_viewer;

  private ColumnViewerTooltipListener( final ColumnViewer viewer, final boolean alwaysVisible )
  {
    super( viewer.getControl().getShell(), alwaysVisible );
    m_viewer = viewer;
  }

  @Override
  protected String getTooltipForEvent( final Event event )
  {
    final ViewerCell cell = m_viewer.getCell( new Point( event.x, event.y ) );
    if( cell == null )
      return null;

    final Widget item = cell.getItem();
    final IBaseLabelProvider labelProvider = m_viewer.getLabelProvider();
    if( item != null && labelProvider instanceof ITooltipProvider )
    {
      final ITooltipProvider tp = (ITooltipProvider) labelProvider;

      return tp.getTooltip( item.getData() );
    }

    return null;
  }
}
