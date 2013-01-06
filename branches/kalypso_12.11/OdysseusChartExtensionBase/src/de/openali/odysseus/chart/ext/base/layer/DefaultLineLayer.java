package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.ITabularDataContainer;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * @author alibu
 */
public class DefaultLineLayer extends AbstractLineLayer
{
  private final ITabularDataContainer< ? , ? > m_dataContainer;

  public DefaultLineLayer( final ILayerProvider provider, final ITabularDataContainer< ? , ? > data, final IStyleSet styleSet )
  {
    super( provider, styleSet );
    m_dataContainer = data;
  }

  @SuppressWarnings( "rawtypes" )
  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    final ITabularDataContainer< ? , ? > dataContainer = getDataContainer();
    if( dataContainer == null )
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getIdentifier() + "is unable to open data." );
      return;
    }
    dataContainer.open();

    final Object[] domainData = dataContainer.getDomainValues();
    final Object[] targetData = dataContainer.getTargetValues();

    if( domainData.length > 0 )
    {
      final IDataRange<Double> dr = getDomainRange();
      final Double max = dr.getMax();
      final Double min = dr.getMin();
      final ICoordinateMapper coordinateMapper = getCoordinateMapper();
      final int screenMin = coordinateMapper.numericToScreen( min, 0.0 ).x;
      final int screenMax = coordinateMapper.numericToScreen( max, 0.0 ).x;
      final List<Point> path = new ArrayList<>();
      for( int i = 0; i < domainData.length; i++ )
      {
        final Object domVal = domainData[i];
        final Object targetVal = targetData[i];
        final Point screen = getCoordinateMapper().logicalToScreen( domVal, targetVal );
        if( screen.x > screenMin && screen.x < screenMax )
          path.add( screen );
      }
      paint( gc, path.toArray( new Point[] {} ) );
    }
  }

  protected ITabularDataContainer< ? , ? > getDataContainer( )
  {
    return m_dataContainer;
  }

  @Override
  public IDataRange<Double> getDomainRange( )
  {
    return getNumericRange( getDomainAxis(), m_dataContainer.getDomainRange() );
  }

  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    return getNumericRange( getTargetAxis(), m_dataContainer.getTargetRange() );
  }
}