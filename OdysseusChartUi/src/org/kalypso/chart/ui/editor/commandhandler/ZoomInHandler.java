package org.kalypso.chart.ui.editor.commandhandler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ElementUpdateHelper;
import org.kalypso.chart.ui.editor.mousehandler.DragZoomInHandler;

import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;

public class ZoomInHandler extends AbstractHandler implements IElementUpdater
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartComposite chart = ChartHandlerUtilities.getChartChecked( context );

    final IChartHandlerManager plotHandler = chart.getPlotHandler();
    plotHandler.activatePlotHandler( new DragZoomInHandler( chart ) );

    final IChartPart part = ChartHandlerUtilities.findChartComposite( context );
    if( part != null )
      ChartHandlerUtilities.updateElements( part );

    return Status.OK_STATUS;
  }

  @Override
  public void updateElement( final UIElement element, final Map parameters )
  {
    ElementUpdateHelper.updateElement( element, DragZoomInHandler.class );
  }
}