package org.kalypso.chart.ui.editor.commandhandler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.chart.ui.editor.chart.visitors.ChangeVisibilityVisitor;
import org.kalypso.chart.ui.editor.chart.visitors.VisibilityInitialStatusVisitor;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.view.IChartComposite;

public class ChangeVisibilityCommandHandler extends AbstractHandler implements IElementUpdater
{
  private static final String LAYER_PARAMETER = "layer.parameter"; //$NON-NLS-1$

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
    final Event trigger = (Event) event.getTrigger();
    final ToolItem item = (ToolItem) trigger.widget;

    return item.getSelection();
  }

  /**
   * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
   */
  @Override
  public void updateElement( final UIElement element, @SuppressWarnings("rawtypes") final Map parameters )
  {
    new UIJob( "" )
    {

      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        final IChartModel model = getModel();
        if( model == null )
          element.setChecked( false );
        else
        {
          final VisibilityInitialStatusVisitor visitor = new VisibilityInitialStatusVisitor( (String) parameters.get( LAYER_PARAMETER ) );

          final ILayerManager layerManager = model.getLayerManager();
          layerManager.accept( visitor );

          element.setChecked( visitor.isEnabled() );
        }

        return Status.OK_STATUS;
      }

      private IChartModel getModel( )
      {
        final IServiceLocator locator = element.getServiceLocator();
        final IEvaluationService service = (IEvaluationService) locator.getService( IEvaluationService.class );
        final IEvaluationContext context = service.getCurrentState();
        final IChartComposite chart = ChartHandlerUtilities.getChart( context );
        if( chart == null )
          return null;

        return chart.getChartModel();
      }
    }.schedule( 500 );

  }

}
