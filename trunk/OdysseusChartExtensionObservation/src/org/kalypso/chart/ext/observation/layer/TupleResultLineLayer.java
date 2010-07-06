package org.kalypso.chart.ext.observation.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.data.impl.NumberDataOperator;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.NumberComparator;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

public class TupleResultLineLayer extends AbstractLineLayer
{

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#getLegendEntries()
   */
  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    // supress PointStyle LegendEntry if LineStyle is visible
    final ILegendEntry[] le = super.getLegendEntries();
    if( le.length < 2 )
      return le;
    return new ILegendEntry[] { le[0] };
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#init()
   */
  @Override
  public void init( )
  {
    super.init();
    if( getTargetAxis().getLabel() == null )
      getTargetAxis().setLabel( getUnitFromComponent( m_data.getTargetComponentName() ) );
    if( getDomainAxis().getLabel() == null )
      getDomainAxis().setLabel( getUnitFromComponent( m_data.getDomainComponentName() ) );
  }

  protected TupleResultDomainValueData< ? , ? > m_data;

  final private IDataOperator<Number> m_dataOperator = new NumberDataOperator( new NumberComparator() );//

  final public static String TOOLTIP_FORMAT = "%-12s %10.4f [%s]%n%-12s %10.4f [%s]"; //$NON-NLS-1$

  public TupleResultLineLayer( final TupleResultDomainValueData< ? , ? > data, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( lineStyle, pointStyle );
    m_data = data;

  }

  public IObservation<TupleResult> getObservation( )
  {
    return m_data.getObservation();
  }

  private String getUnitFromComponent( final String id )
  {
    if( m_data == null )
      return null;
    m_data.open();
    final IObservation<TupleResult> obs = m_data.getObservation();
    final TupleResult tr = obs == null ? null : obs.getResult();
    if( tr != null )
    {
      final int index = tr.indexOfComponent( id );
      if( index > -1 )
        return tr.getComponent( index ).getName() + "[" + tr.getComponent( index ).getUnit() + "]";
    }
    return null;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#getTitle()
   */
  @Override
  public String getTitle( )
  {

    if( super.getTitle() == null && m_data != null )
    {
      m_data.open();
      final IObservation<TupleResult> obs = m_data.getObservation();
      final TupleResult tr = obs == null ? null : obs.getResult();
      if( tr != null )
      {
        final int targetComponentIndex = tr.indexOfComponent( m_data.getTargetComponentName() );
        if( targetComponentIndex > -1 )
          return tr.getComponent( targetComponentIndex ).getName();
      }
    }
    return super.getTitle();
  }

  @Override
  public void drawIcon( final Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );

    final ArrayList<Point> path = new ArrayList<Point>();

    path.add( new Point( 0, height / 2 ) );
    path.add( new Point( width / 5, height / 2 ) );
    path.add( new Point( width / 5 * 2, height / 4 ) );
    path.add( new Point( width / 5 * 3, height / 4 * 3 ) );
    path.add( new Point( width / 5 * 4, height / 2 ) );
    path.add( new Point( width, height / 2 ) );

    drawLine( gc, path );
    gc.dispose();

  }

  @Override
  @SuppressWarnings("unchecked")
  public void paint( final GC gc )
  {
    if( m_data == null )
      return;

    final List<Point> path = new ArrayList<Point>();

    m_data.open();

    final Object[] domainValues = m_data.getDomainValues();
    final Object[] targetValues = m_data.getTargetValues();

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
          final Point screen = getCoordinateMapper().numericToScreen( m_dataOperator.logicalToNumeric( (Number) domainValue ), m_dataOperator.logicalToNumeric( (Number) targetValue ) );
          path.add( screen );
        }
      }
    }

    drawLine( gc, path );
    drawPoints( gc, path );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    if( m_data == null )
      return null;

    final IDataRange< ? > dataRange = m_data.getDomainRange();
    final Object min = dataRange.getMin();
    final Object max = dataRange.getMax();
    if( min == null || max == null )
      return null;

    final IDataRange<Number> numRange = new DataRange<Number>( m_dataOperator.logicalToNumeric( (Number) min ), m_dataOperator.logicalToNumeric( (Number) max ) );
    return numRange;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( )
  {
    if( m_data == null )
      return null;

    final IDataRange dataRange = m_data.getTargetRange();
    final Object min = dataRange.getMin();
    final Object max = dataRange.getMax();
    if( min == null || max == null )
      return null;

    final IDataRange<Number> numRange = new DataRange<Number>( m_dataOperator.logicalToNumeric( (Number) min ), m_dataOperator.logicalToNumeric( (Number) max ) );
    return numRange;
  }

  protected String getTooltip( final int index )
  {
    final TupleResult tr = m_data.getObservation().getResult();
    final int targetComponentIndex = tr.indexOfComponent( m_data.getTargetComponentName() );
    final int domainComponentIndex = tr.indexOfComponent( m_data.getDomainComponentName() );
    final String targetComponentLabel = tr.getComponent( targetComponentIndex ).getName();
    final String domainComponentLabel = tr.getComponent( domainComponentIndex ).getName();
    final String targetComponentUnit = tr.getComponent( targetComponentIndex ).getUnit();
    final String domainComponentUnit = tr.getComponent( domainComponentIndex ).getUnit();
    final Object y = tr.get( index ).getValue( targetComponentIndex );
    final Object x = tr.get( index ).getValue( domainComponentIndex );

    return String.format( TOOLTIP_FORMAT, new Object[] { domainComponentLabel, x, domainComponentUnit, targetComponentLabel, y, targetComponentUnit } );
  }

  protected Rectangle getHoverRect( final Point screen, final int index )
  {
    return RectangleUtils.buffer( screen );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  @Override
  public EditInfo getHover( Point pos )
  {
    if( !isVisible() )
      return null;
    final Object[] domainValues = m_data.getDomainValues();
    final Object[] targetValues = m_data.getTargetValues();
    for( int i = 0; i < domainValues.length; i++ )
    {
      final Object domainValue = domainValues[i];
      final Object targetValue = targetValues[i];
      if( targetValue == null )
        continue;
      final Point pValue = getCoordinateMapper().numericToScreen( m_dataOperator.logicalToNumeric( (Number) domainValue ), m_dataOperator.logicalToNumeric( (Number) targetValue ) );
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
}
