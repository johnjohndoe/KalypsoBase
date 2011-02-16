package org.kalypso.chart.ui.editor.commandhandler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.chart.ui.editor.chart.visitors.ChangeVisibilityVisitor;
import org.kalypso.chart.ui.editor.chart.visitors.VisibilityInitialStatusVisitor;
import org.kalypso.chart.ui.editor.commandhandler.utils.CommandHandlerUtils;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.view.IChartComposite;

public class ChangeVisibilityCommandHandler extends AbstractHandler implements IElementUpdater
{
  public static final String ID = "org.kalypso.chart.ui.commands.change.visibility"; // $NON-NLS-1$

  public static final String LAYER_PARAMETER = "layer.parameter"; // $NON-NLS-1$

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartComposite chart = ChartHandlerUtilities.getChart( context );
    if( chart == null )
      return Status.CANCEL_STATUS;

    final IChartModel model = chart.getChartModel();
    final ILayerManager layerManager = model.getLayerManager();
    final boolean enabled = CommandHandlerUtils.isEnabled( event );

    layerManager.accept( new ChangeVisibilityVisitor( getParameter( event ), enabled ) );

    callAdditionalVisitors( layerManager );

    return Status.OK_STATUS;
  }

  protected void callAdditionalVisitors( @SuppressWarnings("unused") final ILayerManager layerManager )
  {
    // overwrite to execute additional visitors
  }

  private String getParameter( final ExecutionEvent event )
  {
    return event.getParameter( LAYER_PARAMETER );
  }

  /**
   * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
   */
  @Override
  public void updateElement( final UIElement element, @SuppressWarnings("rawtypes") final Map parameters )
  {
    final IChartModel model = getModel( element );

    if( model == null )
      element.setChecked( false );
    else
    {
      final VisibilityInitialStatusVisitor visitor = new VisibilityInitialStatusVisitor( (String) parameters.get( ChangeVisibilityCommandHandler.LAYER_PARAMETER ) );

      final ILayerManager layerManager = model.getLayerManager();
      layerManager.accept( visitor );

      element.setChecked( visitor.isEnabled() );
    }
  }

  private IChartModel getModel( final UIElement element )
  {
    final IServiceLocator locator = element.getServiceLocator();
    final IEvaluationService service = (IEvaluationService) locator.getService( IEvaluationService.class );
    final IEvaluationContext context = service.getCurrentState();
    final IChartComposite chart = ChartHandlerUtilities.getChart( context );
    if( chart == null )
      return null;

    return chart.getChartModel();
  }
}
