package org.kalypso.chart.ui.editor.commandhandler.visibility;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.chart.ui.editor.chart.visitors.ChangeVisibilityVisitor;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.view.IChartComposite;

public class ChangeVisibilityCommandHandler extends AbstractHandler implements IElementUpdater
{
  public static final String ID = "org.kalypso.chart.ui.commands.change.visibility"; // $NON-NLS-1$

  public static final String LAYER_PARAMETER = "layer.parameter"; // $NON-NLS-1$

  private static final String BUTTON_UPDATE_PARAMETER = "automatic.button.update"; // $NON-NLS-1$

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartComposite chart = ChartHandlerUtilities.getChart( context );
    if( chart == null )
      return Status.CANCEL_STATUS;

    final IChartModel model = chart.getChartModel();
    final ILayerManager layerManager = model.getLayerManager();
    layerManager.accept( new ChangeVisibilityVisitor( getParameter( event ), isSelected( event ) ) );

    return Status.OK_STATUS;
  }

  private String getParameter( final ExecutionEvent event )
  {
    return event.getParameter( LAYER_PARAMETER );
  }

  private boolean isSelected( final ExecutionEvent event )
  {
    final Object trigger = event.getTrigger();
    if( trigger instanceof Event )
    {
      final Event triggerEvent = (Event) trigger;
      final ToolItem item = (ToolItem) triggerEvent.widget;

      return item.getSelection();
    }

    // otherwise it will be enabled by default!
    return true;
  }

  /**
   * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
   */
  @Override
  public void updateElement( final UIElement element, @SuppressWarnings("rawtypes") final Map parameters )
  {
    final IUpdateElementStrategy strategy = getStrategy( parameters );
    strategy.update( element );
  }

  private IUpdateElementStrategy getStrategy( @SuppressWarnings("rawtypes") final Map parameters )
  {
    final Object paramater = parameters.get( BUTTON_UPDATE_PARAMETER );
    if( paramater == null )
      return new ManualButtonUpdateStrategy( parameters );

    final Boolean automaticUpdate = Boolean.valueOf( (String) paramater );
    if( automaticUpdate )
      return new AutomaticButtonUpdateStrategy( parameters );

    return new ManualButtonUpdateStrategy( parameters );
  }
}
