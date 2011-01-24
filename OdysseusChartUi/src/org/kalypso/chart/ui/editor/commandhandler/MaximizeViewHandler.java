package org.kalypso.chart.ui.editor.commandhandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.kalypso.chart.ui.IChartPart;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * This handler sets all axis ranges in a way that all relations which were set up by a user - e.g. by panning or
 * zooming single layers / axes - are kept; for example, if there are two layers
 * 
 * @author burtscher1
 */
public class MaximizeViewHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  @SuppressWarnings("unchecked")
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final IChartPart chartPart = ChartHandlerUtilities.findChartComposite( context );
    if( chartPart == null )
      return null;

    final IChartModel model = chartPart.getChartComposite().getChartModel();
    final Set<IAxis> horAxes = new HashSet<IAxis>();
    final Set<IAxis> vertAxes = new HashSet<IAxis>();
    final IAxis[] axes = model.getMapperRegistry().getAxes();
    final Set<IDataRange<Number>> numericRangesHor = new HashSet<IDataRange<Number>>();
    final Set<IDataRange<Number>> numericRangesVert = new HashSet<IDataRange<Number>>();
    final Set<Number> screenMinsHor = new HashSet<Number>();
    final Set<Number> screenMaxsHor = new HashSet<Number>();
    final Set<Number> screenMinsVert = new HashSet<Number>();
    final Set<Number> screenMaxsVert = new HashSet<Number>();
    for( final IAxis axis : axes )
    {
      final List<IChartLayer> layers = model.getAxis2Layers().get( axis );
      if( layers == null )
      {
        continue;
      }

      final List<IDataRange<Number>> ranges = new ArrayList<IDataRange<Number>>( layers.size() );

      for( final IChartLayer layer : layers )
      {
        if( layer.isVisible() )
        {
          IDataRange<Number> range = null;
          if( axis == layer.getCoordinateMapper().getDomainAxis() )
            range = layer.getDomainRange();
          else if( axis == layer.getCoordinateMapper().getTargetAxis() )
            range = layer.getTargetRange( null );

          if( range != null )
            ranges.add( range );
        }
      }
      IDataRange<Number> mergedDataRange = ChartUtilities.mergeDataRanges( ranges.toArray( new IDataRange[ranges.size()] ) );

      if( mergedDataRange == null )
      {
        // if mergedDataRange is null, we keep the old range - if there
        // is any
        if( axis.getNumericRange() != null )
        {
          continue;
        }
        else
        {
          // otherwise, we use a default range
          mergedDataRange = new ComparableDataRange<Number>( new Number[] { 0, 1 } );
        }
      }
      int smin;
      int smax;
      if( axis.getDirection().equals( DIRECTION.POSITIVE ) )
      {
        smin = axis.numericToScreen( mergedDataRange.getMin() );
        smax = axis.numericToScreen( mergedDataRange.getMax() );
      }
      else
      {
        smin = axis.numericToScreen( mergedDataRange.getMax() );
        smax = axis.numericToScreen( mergedDataRange.getMin() );
      }

      if( axis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
      {
        horAxes.add( axis );
        numericRangesHor.add( mergedDataRange );
        screenMinsHor.add( smin );
        screenMaxsHor.add( smax );
      }
      else
      {
        vertAxes.add( axis );
        numericRangesVert.add( mergedDataRange );
        screenMinsVert.add( smin );
        screenMaxsVert.add( smax );
      }
    }

    // Horizontales Minumum berechnen
    final IDataRange<Number> screenMinHorTmp = new ComparableDataRange<Number>( screenMinsHor.toArray( new Number[] {} ) );
    final int horMin = (int) Math.floor( screenMinHorTmp.getMin().doubleValue() );
    // Horizontales Maximum berechnen
    final IDataRange<Number> screenMaxHorTmp = new ComparableDataRange<Number>( screenMaxsHor.toArray( new Number[] {} ) );
    final int horMax = (int) Math.ceil( screenMaxHorTmp.getMax().doubleValue() );
    // Vertikales Minimum berechnen
    final IDataRange<Number> screenMinVertTmp = new ComparableDataRange<Number>( screenMinsVert.toArray( new Number[] {} ) );
    final int vertMin = (int) Math.floor( screenMinVertTmp.getMin().doubleValue() );
    // Vertikales Maximum berechnen
    final IDataRange<Number> screenMaxVertTmp = new ComparableDataRange<Number>( screenMaxsVert.toArray( new Number[] {} ) );
    final int vertMax = (int) Math.ceil( screenMaxVertTmp.getMax().doubleValue() );

    // neue Ranges setzen
    for( final IAxis axis : horAxes )
    {
      final IDataRange<Number> newRange = new DataRange<Number>( axis.screenToNumeric( horMin ), axis.screenToNumeric( horMax ) );
      axis.setNumericRange( newRange );
    }
    for( final IAxis axis : vertAxes )
    {
      final IDataRange<Number> newRange = new DataRange<Number>( axis.screenToNumeric( vertMin ), axis.screenToNumeric( vertMax ) );
      axis.setNumericRange( newRange );
    }

    return null;
  }
}