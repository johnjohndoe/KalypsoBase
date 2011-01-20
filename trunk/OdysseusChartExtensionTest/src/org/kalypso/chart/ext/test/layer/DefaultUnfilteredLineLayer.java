package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.ext.base.layer.DefaultLineLayer;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.ITabularDataContainer;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class DefaultUnfilteredLineLayer extends DefaultLineLayer
{

  public DefaultUnfilteredLineLayer( final ILayerProvider provider, final ITabularDataContainer data, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider, data, lineStyle, pointStyle );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void paint( final GC gc )
  {
    final ITabularDataContainer dataContainer = getDataContainer();
    if( dataContainer != null )
    {
      dataContainer.open();

      final Object[] domainData = dataContainer.getDomainValues();
      final Object[] targetData = dataContainer.getTargetValues();

      final IDataOperator dopDomain = getDomainAxis().getDataOperator( domainData[0].getClass() );
      final IDataOperator dopTarget = getTargetAxis().getDataOperator( targetData[0].getClass() );

      final ArrayList<Point> path = new ArrayList<Point>();
      final ORIENTATION ori = getDomainAxis().getPosition().getOrientation();
      for( int i = 0; i < domainData.length; i++ )
      {
        final Object domVal = domainData[i];
        final Object targetVal = targetData[i];
        final int domScreen = getDomainAxis().numericToScreen( dopDomain.logicalToNumeric( domVal ) );
        final int valScreen = getTargetAxis().numericToScreen( dopTarget.logicalToNumeric( targetVal ) );
        // Koordinaten switchen
        final Point unswitched = new Point( domScreen, valScreen );
        path.add( new Point( ori.getX( unswitched ), ori.getY( unswitched ) ) );
      }

      drawLine( gc, path );
      drawPoints( gc, path );

    }
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has not yet been opened" );
    }
  }

}
