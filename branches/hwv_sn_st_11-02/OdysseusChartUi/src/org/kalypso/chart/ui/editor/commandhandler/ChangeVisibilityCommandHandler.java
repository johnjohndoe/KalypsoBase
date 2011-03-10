package org.kalypso.chart.ui.editor.commandhandler;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import org.kalypso.commons.java.lang.Objects;

import com.google.common.base.Strings;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.view.IChartComposite;

public class ChangeVisibilityCommandHandler extends AbstractHandler implements IElementUpdater
{
  public static final String ID = "org.kalypso.chart.ui.commands.change.visibility"; // $NON-NLS-1$

  public static final String LAYER_PARAMETER = "layer.parameter"; // $NON-NLS-1$

  public static final String LAYER_FILTER = "layer.filter"; // $NON-NLS-1$

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

    layerManager.accept( new ChangeVisibilityVisitor( getParameter( event ), getFilters( event ), enabled ) );

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

  private IChartLayerFilter[] getFilters( final ExecutionEvent event )
  {
    final String parameter = event.getParameter( LAYER_FILTER );
    if( Strings.isNullOrEmpty( parameter ) )
      return new IChartLayerFilter[] {};

    final Set<IChartLayerFilter> filters = new LinkedHashSet<IChartLayerFilter>();

    final String[] parameters = parameter.split( ";" );
    for( final String filterIdentifier : parameters )
    {
      final IChartLayerFilter filter = OdysseusChartFramework.getDefault().findFilter( filterIdentifier );
      if( Objects.isNotNull( filter ) )
        filters.add( filter );
    }

    return filters.toArray( new IChartLayerFilter[] {} );
  }

  /**
   * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
   */
  @Override
  public void updateElement( final UIElement element, @SuppressWarnings("rawtypes") final Map parameters )
  {
    final IChartModel model = getModel( element );
    if( Objects.isNull( model ) )
      element.setChecked( false );
    else
    {
      final String parameter = (String) parameters.get( ChangeVisibilityCommandHandler.LAYER_PARAMETER );
      final ILayerManager layerManager = model.getLayerManager();

      final VisibilityInitialStatusVisitor visitor = new VisibilityInitialStatusVisitor( parameter );
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
