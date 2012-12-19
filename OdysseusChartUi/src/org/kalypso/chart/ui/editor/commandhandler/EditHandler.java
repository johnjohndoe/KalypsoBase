package org.kalypso.chart.ui.editor.commandhandler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ElementUpdateHelper;
import org.kalypso.chart.ui.editor.mousehandler.DragEditHandler;

import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;

public class EditHandler extends AbstractHandler implements IElementUpdater
{
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartComposite chart = ChartHandlerUtilities.getChart( context );
    if( chart == null )
      return Status.CANCEL_STATUS;

    final IChartHandlerManager handler = chart.getPlotHandler();
    handler.activatePlotHandler( new DragEditHandler( chart ) );

    final IChartPart part = ChartHandlerUtilities.findChartComposite( context );
    if( part != null )
      ChartHandlerUtilities.updateElements( part );

    return Status.OK_STATUS;
  }

  @Override
  public void updateElement( final UIElement element, final Map parameters )
  {
    ElementUpdateHelper.updateElement( element, DragEditHandler.class );
  }
}