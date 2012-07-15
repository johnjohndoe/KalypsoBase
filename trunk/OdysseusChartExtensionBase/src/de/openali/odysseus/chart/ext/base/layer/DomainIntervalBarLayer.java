package de.openali.odysseus.chart.ext.base.layer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.ext.base.data.AbstractDomainIntervalValueData;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;

/**
 * @author alibu
 */
public class DomainIntervalBarLayer extends AbstractBarLayer
{
  private final AbstractDomainIntervalValueData< ? , ? > m_dataContainer;

  public DomainIntervalBarLayer( final ILayerProvider provider, final AbstractDomainIntervalValueData< ? , ? > data, final IAreaStyle areaStyle )
  {
    super( provider, areaStyle );
    m_dataContainer = data;
  }

  @Override
  @SuppressWarnings({ "rawtypes" })
  public void paint( final GC gc, IProgressMonitor monitor )
  {
    final AbstractDomainIntervalValueData< ? , ? > dataContainer = getDataContainer();

    if( dataContainer != null )
    {
      dataContainer.open();

      final Object[] domainStartComponent = dataContainer.getDomainDataIntervalStart();
      final Object[] domainEndComponent = dataContainer.getDomainDataIntervalEnd();
      final Object[] targetComponent = dataContainer.getTargetValues();
      final IDataOperator dopDomain = new DataOperatorHelper().getDataOperator( domainStartComponent[0].getClass() );
      final IDataOperator dopTarget = new DataOperatorHelper().getDataOperator( targetComponent[0].getClass() );
      if( dopDomain == null || dopTarget == null )
        return;
      for( int i = 0; i < domainStartComponent.length; i++ )
      {
        final Object startValue = domainStartComponent[i];
        final Object endValue = domainEndComponent[i];
        final Object targetValue = targetComponent[i];

        final Point p1 = getCoordinateMapper().logicalToScreen( startValue, targetValue );
        final Point p2 = getCoordinateMapper().logicalToScreen( endValue, targetValue );
        final Rectangle rect = new Rectangle( p1.x, p1.y, p2.x - p1.x, getTargetAxis().getScreenHeight() - p2.y );
        paint( gc, rect );
      }
    }
  }

  protected AbstractDomainIntervalValueData< ? , ? > getDataContainer( )
  {
    return m_dataContainer;
  }

  @Override
  public IDataRange< ? > getTargetRange( final IDataRange< ? > domainIntervall )
  {
    final IDataRange< ? > targetRange = getDataContainer().getTargetRange();

    if( targetRange == null )
      return null;
    final IDataOperator dop = new DataOperatorHelper().getDataOperator( getTargetAxis().getDataClass() );
    return DataRange.create( dop.logicalToNumeric( targetRange.getMin() ), dop.logicalToNumeric( targetRange.getMax() ) );
  }

  @Override
  public IDataRange< ? > getDomainRange( )
  {
    final IDataRange< ? > domainRange = getDataContainer().getDomainRange();
    if( domainRange == null )
      return null;
    final IDataOperator dop = new DataOperatorHelper().getDataOperator( getDomainAxis().getDataClass() );
    return DataRange.create( dop.logicalToNumeric( domainRange.getMin() ), dop.logicalToNumeric( domainRange.getMax() ) );
  }
}