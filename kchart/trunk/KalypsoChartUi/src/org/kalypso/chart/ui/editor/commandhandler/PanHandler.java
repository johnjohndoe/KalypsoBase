package org.kalypso.chart.ui.editor.commandhandler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ElementUpdateHelper;
import org.kalypso.chart.ui.editor.mousehandler.AxisDragPanHandler;
import org.kalypso.chart.ui.editor.mousehandler.DragPanHandler;

public class PanHandler extends AbstractHandler implements IElementUpdater
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartPart chartPart = ChartHandlerUtilities.findChartComposite( context );

    if( chartPart == null )
      return null;

    final DragPanHandler plotDragPanHandler = new DragPanHandler( chartPart.getChartComposite() );
    chartPart.getPlotDragHandler().setActiveHandler( plotDragPanHandler );
    final AxisDragPanHandler axisDragPanHandler = new AxisDragPanHandler( chartPart.getChartComposite() );
    chartPart.getAxisDragHandler().setActiveHandler( axisDragPanHandler );

    ChartHandlerUtilities.updateElements( chartPart );

    return null;
  }

  /**
   * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
   */
  @SuppressWarnings("unchecked")
  public void updateElement( final UIElement element, final Map parameters )
  {
    ElementUpdateHelper.updateElement( element, parameters, DragPanHandler.class );
  }
}
