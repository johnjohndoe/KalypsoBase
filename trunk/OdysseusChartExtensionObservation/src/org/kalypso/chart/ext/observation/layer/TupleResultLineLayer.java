package org.kalypso.chart.ext.observation.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

public class TupleResultLineLayer extends AbstractLineLayer implements ITooltipChartLayer
{
  private static String TOOLTIP_FORMAT = "%-12s %s %n%-12s %s"; //$NON-NLS-1$

  private final TupleResultDomainValueData< ? , ? > m_valueData;

  public TupleResultLineLayer( final ILayerProvider provider, final TupleResultDomainValueData< ? , ? > data, final IStyleSet styleSet )
  {
    super( provider, styleSet );

    m_valueData = data;
  }

  @Override
  public IDataRange< ? > getDomainRange( )
  {
    if( getValueData() == null || getDomainAxis() == null )
      return null;

    final IDataRange< ? > dataRange = getValueData().getDomainRange();
    final Object min = dataRange.getMin();
    final Object max = dataRange.getMax();
    if( min == null || max == null )
      return null;

    // FIXME: bad and ugly hack: getDomainRange must return numeric values
    final Number numericMin = toNumeric( min );
    final Number numericMax = toNumeric( max );
    return DataRange.create( numericMin, numericMax );
  }

  // FIXME: awful -> should not be necessary!
  private Number toNumeric( final Object value )
  {
    final IDataOperator<Object> dop = (IDataOperator<Object>) getCoordinateMapper().getDataOperator( value.getClass() );
    return dop.logicalToNumeric( value );
  }

  @Override
  public EditInfo getHover( final Point pos )
  {
    if( !isVisible() )
      return null;

    if( getValueData() == null )
      return null;

    final Object[] domainValues = getValueData().getDomainValues();
    final Object[] targetValues = getValueData().getTargetValues();
    for( int i = 0; i < domainValues.length; i++ )
    {
      if( domainValues.length != targetValues.length )
        return null;
      final Object domainValue = domainValues[i];
      final Object targetValue = targetValues[i];
      if( targetValue == null )
        continue;
      final Point pValue = getCoordinateMapper().logicalToScreen( domainValue, targetValue );
      final Rectangle hover = getHoverRect( pValue, i );
      if( hover == null )
        continue;

      if( hover.contains( pos ) )
      {
        if( pValue == null )
          return new EditInfo( this, null, null, i, getTooltip( i ), RectangleUtils.getCenterPoint( hover ) );

        return new EditInfo( this, null, null, i, getTooltip( i ), pValue );
      }
    }

    return null;
  }

  private Rectangle getHoverRect( final Point screen, final int index )
  {
    return RectangleUtils.buffer( screen );
  }

  public IObservation<TupleResult> getObservation( )
  {
    if( getValueData() == null )
      return null;
    return getValueData().getObservation();
  }

  @Override
  public IDataRange< ? > getTargetRange( final IDataRange< ? > domainIntervall )
  {
    if( getValueData() == null || getTargetAxis() == null )
      return null;
    final IDataRange< ? > dataRange = getValueData().getTargetRange();
    final Object min = dataRange.getMin();
    final Object max = dataRange.getMax();
    if( min == null || max == null )
      return null;
    return dataRange;
  }

  @Override
  public String getTitle( )
  {
    if( super.getTitle() == null && getValueData() != null )
    {
      getValueData().open();
      final IObservation<TupleResult> obs = getValueData().getObservation();
      final TupleResult tr = obs == null ? null : obs.getResult();
      if( tr != null )
      {
        final int targetComponentIndex = tr.indexOfComponent( getValueData().getTargetComponentName() );
        if( targetComponentIndex > -1 )
          return tr.getComponent( targetComponentIndex ).getName();
      }
    }
    return super.getTitle();
  }

  protected String getTooltip( final int index )
  {
    if( getValueData() == null )
      return "";
    final TupleResult tr = getValueData().getObservation().getResult();
    final int targetComponentIndex = tr.indexOfComponent( getValueData().getTargetComponentName() );
    final int domainComponentIndex = tr.indexOfComponent( getValueData().getDomainComponentName() );
    final String targetComponentLabel = ComponentUtilities.getComponentLabel( tr.getComponent( targetComponentIndex ) );
    final String domainComponentLabel = ComponentUtilities.getComponentLabel( tr.getComponent( domainComponentIndex ) );
    final Object y = tr.get( index ).getValue( targetComponentIndex );
    final Object x = tr.get( index ).getValue( domainComponentIndex );

    return String.format( TOOLTIP_FORMAT, new Object[] { domainComponentLabel, x, targetComponentLabel, y } );
  }

  private String getUnitFromComponent( final String id )
  {
    if( getValueData() == null )
      return null;
    getValueData().open();
    final IObservation<TupleResult> obs = getValueData().getObservation();
    final TupleResult tr = obs == null ? null : obs.getResult();
    if( tr != null )
    {
      final int index = tr.indexOfComponent( id );
      if( index > -1 )
        return tr.getComponent( index ).getName() + "[" + tr.getComponent( index ).getUnit() + "]";
    }
    return null;
  }

  public TupleResultDomainValueData< ? , ? > getValueData( )
  {
    return m_valueData;
  }

  @Override
  public void init( )
  {
    if( getValueData() == null )
      return;
    if( getTargetAxis().getLabels().length == 0 )
      getTargetAxis().addLabel( new TitleTypeBean( getUnitFromComponent( getValueData().getTargetComponentName() ) ) );
    if( getDomainAxis().getLabels().length == 0 )
      getDomainAxis().addLabel( new TitleTypeBean( getUnitFromComponent( getValueData().getDomainComponentName() ) ) );
  }

  @Override
  public void paint( final GC gc, ChartImageInfo chartImageInfo, IProgressMonitor monitor )
  {
    final TupleResultDomainValueData< ? , ? > data = getValueData();
    if( data == null )
      return;

    final List<Point> path = new ArrayList<Point>();
    data.open();

    final Object[] domainValues = data.getDomainValues();
    final Object[] targetValues = data.getTargetValues();

    if( domainValues.length > 0 && targetValues.length > 0 )
    {
      for( int i = 0; i < domainValues.length; i++ )
      {
        final Object domainValue = domainValues[i];
        final Object targetValue = targetValues[i];

        // we have to check if all values are correct - an incorrect value means a null value - the axis would return 0
        // in that case
        if( domainValue != null && targetValue != null )
        {
          final Point screen = getCoordinateMapper().logicalToScreen( domainValue, targetValue );
          path.add( screen );
        }
      }
    }
    paint( gc, path.toArray( new Point[] {} ) );
  }
}