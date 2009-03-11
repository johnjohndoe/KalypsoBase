package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.ITabularDataContainer;
import de.openali.odysseus.chart.framework.model.mapper.impl.CoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class DefaultDoubleUnfilteredLineLayer extends DefaultDoubleLineLayer
{

  public DefaultDoubleUnfilteredLineLayer( ITabularDataContainer<Double, Double> data, ILineStyle lineStyle, IPointStyle pointStyle )
  {
    super( data, lineStyle, pointStyle );
  }

  @Override
  public void paint( GC gc )
  {
    final ITabularDataContainer<Double, Double> dataContainer = getDataContainer();
    if( dataContainer != null )
    {
      dataContainer.open();

      final Double[] domainData = dataContainer.getDomainValues();
      final Double[] targetData = dataContainer.getTargetValues();

      final List<Point> path = new ArrayList<Point>();

      CoordinateMapper km = new CoordinateMapper( getDomainAxis(), getTargetAxis() );

      for( int i = 0; i < domainData.length; i++ )
      {
        final double domVal = domainData[i].doubleValue();
        final double targetVal = targetData[i].doubleValue();
        path.add( km.numericToScreen( domVal, targetVal ) );
      }

      // Zeichnen
      drawLine( gc, path );
      drawPoints( gc, path );

    }
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has not yet been opened" );
    }
  }

}
