package org.kalypso.chart.ui.editor.commandhandler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ChartPartListener;
import org.kalypso.chart.ui.editor.ElementUpdateHelper;
import org.kalypso.chart.ui.editor.mousehandler.DragZoomInHandler;

public class ZoomInHandler extends AbstractHandler implements IElementUpdater
{
  private final Class m_handlerClass = DragZoomInHandler.class;

  @Override
  public Object execute( ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );

    final IChartPart chartPart = (IChartPart) part.getAdapter( IChartPart.class );
    if( chartPart == null )
      return null;
    else
    {
      DragZoomInHandler dragZoomInHandler = new DragZoomInHandler( chartPart.getChartComposite() );
      chartPart.getChartDragHandler().setActiveHandler( dragZoomInHandler );
      ChartPartListener.updateElements( part );
    }
    return null;
  }

  /**
   * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
   */
  public void updateElement( UIElement element, Map parameters )
  {
    ElementUpdateHelper.updateElement( element, parameters, m_handlerClass );
  }
}
