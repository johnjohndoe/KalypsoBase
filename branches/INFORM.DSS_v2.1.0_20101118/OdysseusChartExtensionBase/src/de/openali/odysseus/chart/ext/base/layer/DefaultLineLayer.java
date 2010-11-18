package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.ITabularDataContainer;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class DefaultLineLayer extends AbstractLineLayer
{

  private final ITabularDataContainer m_dataContainer;

  public DefaultLineLayer( ITabularDataContainer data, ILineStyle lineStyle, IPointStyle pointStyle )
  {
    super( lineStyle, pointStyle );
    m_dataContainer = data;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @Override
  public void paint( GC gc )
  {
    final ITabularDataContainer dataContainer = getDataContainer();
    if( dataContainer != null )
    {
      dataContainer.open();

      final Object[] domainData = dataContainer.getDomainValues();
      final Object[] targetData = dataContainer.getTargetValues();

      if( domainData.length > 0 )
      {

        IDataOperator dopDomain = getDomainAxis().getDataOperator( domainData[0].getClass() );
        IDataRange<Number> dr = getDomainAxis().getNumericRange();
        IDataOperator dopTarget = getTargetAxis().getDataOperator( targetData[0].getClass() );

        Number max = dr.getMax();
        Number min = dr.getMin();

        final ArrayList<Point> path = new ArrayList<Point>();

        ORIENTATION ori = getDomainAxis().getPosition().getOrientation();
        for( int i = 0; i < domainData.length; i++ )
        {
          // nur zeichnen, wenn linie innerhalb des Sichtbarkeitsintervalls liegt
          boolean setPoint = false;
          final Object domVal = domainData[i];

          if( dopDomain.logicalToNumeric( domVal ).doubleValue() > min.doubleValue() && dopDomain.logicalToNumeric( domVal ).doubleValue() < max.doubleValue() )
          {
            setPoint = true;
          }
          else if( dopDomain.logicalToNumeric( domVal ).doubleValue() < min.doubleValue() && i < domainData.length - 1 )
          {
            Object next = domainData[i + 1];
            if( dopDomain.logicalToNumeric( next ).doubleValue() > min.doubleValue() )
            {
              setPoint = true;
            }
          }
          else if( dopDomain.logicalToNumeric( domVal ).doubleValue() > max.doubleValue() && i > 0 )
          {
            Object prev = domainData[i - 1];
            if( dopDomain.logicalToNumeric( prev ).doubleValue() < max.doubleValue() )
            {
              setPoint = true;
            }

          }

          if( setPoint )
          {
            final Object targetVal = targetData[i];
            final int domScreen = getDomainAxis().numericToScreen( dopDomain.logicalToNumeric( domVal ) );
            final int valScreen = getTargetAxis().numericToScreen( dopTarget.logicalToNumeric( targetVal ) );
            // Koordinaten switchen
            Point unswitched = new Point( domScreen, valScreen );
            path.add( new Point( ori.getX( unswitched ), ori.getY( unswitched ) ) );
          }
        }
        drawLine( gc, path );
        drawPoints( gc, path );
      }
      else
      {
        Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has no data to draw." );
      }
    }
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has not yet been opened" );
    }
  }

  protected ITabularDataContainer getDataContainer( )
  {
    return m_dataContainer;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  @SuppressWarnings("unchecked")
  public IDataRange<Number> getDomainRange( )
  {
    IDataRange domainRange = m_dataContainer.getDomainRange();
    Object max = domainRange.getMax();
    IDataOperator dop = getDomainAxis().getDataOperator( max.getClass() );
    return new DataRange<Number>( dop.logicalToNumeric( domainRange.getMin() ), dop.logicalToNumeric( domainRange.getMax() ) );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  @SuppressWarnings("unchecked")
  public IDataRange<Number> getTargetRange(IDataRange<Number> domainIntervall )
  {
    IDataRange targetRange = m_dataContainer.getTargetRange();
    Object max = targetRange.getMax();
    IDataOperator top = getTargetAxis().getDataOperator( max.getClass() );
    return new DataRange<Number>( top.logicalToNumeric( targetRange.getMin() ), top.logicalToNumeric( targetRange.getMax() ) );
  }

}
