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
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

public class TupleResultLineLayer extends AbstractLineLayer
{
  private final TupleResultDomainValueData< ? , ? > m_valueData;

  final public static String TOOLTIP_FORMAT = "%-12s %s %n%-12s %s"; //$NON-NLS-1$

  public TupleResultLineLayer( final ILayerProvider provider, final TupleResultDomainValueData< ? , ? > data, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider, lineStyle, pointStyle );

    m_valueData = data;
  }

  public TupleResultLineLayer( final ILayerProvider provider, final TupleResultDomainValueData< ? , ? > data, final IStyleSet styleSet )
  {
    super( provider, styleSet );

    m_valueData = data;
  }

  @Override
  public void drawIcon( final GC gc, final Point size )
  {
    final ArrayList<Point> path = new ArrayList<Point>();

    path.add( new Point( 0, size.y / 2 ) );
    path.add( new Point( size.x / 5, size.y / 2 ) );
    path.add( new Point( size.x / 5 * 2, size.y / 4 ) );
    path.add( new Point( size.x / 5 * 3, size.y / 4 * 3 ) );
    path.add( new Point( size.x / 5 * 4, size.y / 2 ) );
    path.add( new Point( size.x, size.y / 2 ) );
    final ILineStyle ls = getLineStyle();
    final PolylineFigure lf = new PolylineFigure();
    lf.setStyle( ls );
    lf.setPoints( path.toArray( new Point[] {} ) );
    lf.paint( gc );
  }

  @Override
  public void drawIcon( final Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );
    drawIcon( gc, new Point( width, height ) );
    gc.dispose();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
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
    return dataRange;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer#getHover(org.eclipse.swt.graphics.Point)
   */
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

  protected Rectangle getHoverRect( final Point screen, final int index )
  {
    return RectangleUtils.buffer( screen );
  }

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

  public IObservation<TupleResult> getObservation( )
  {
    if( getValueData() == null )
      return null;
    return getValueData().getObservation();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
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

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#getTitle()
   */
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

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#init()
   */
  @Override
  public void init( )
  {
    super.init();
    if( getValueData() == null )
      return;
    if( getTargetAxis().getLabels().length == 0 )
      getTargetAxis().addLabel( new TitleTypeBean( getUnitFromComponent( getValueData().getTargetComponentName() ) ) );
    if( getDomainAxis().getLabels().length == 0 )
      getDomainAxis().addLabel( new TitleTypeBean( getUnitFromComponent( getValueData().getDomainComponentName() ) ) );
  }

  @Override
  public void paint( final GC gc )
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
