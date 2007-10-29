package org.kalypso.chart.ui.editor.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.mouse.DragPanHandler;

public class PanHandler extends AbstractHandler implements IElementUpdater
{

  @Override
  public Object execute( ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );

    final ChartComposite chart = (ChartComposite) part.getAdapter( ChartComposite.class );
    if( chart == null && part instanceof IChartPart )
      return null;
    else
    {
      DragPanHandler dph = new DragPanHandler( chart );
      Command command = event.getCommand();
      ((IChartPart) part).getChartDragHandler().setActiveHandler( dph, command );
    }
    return null;
  }

  /**
   * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
   */
  public void updateElement( UIElement element, Map parameters )
  {
    // chart suchen, rausfinden, welches tool aktiv

    element.setChecked( true );
  }

}
