package org.kalypso.chart.ext.observation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.RectangleUtils;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.ComponentUtilities;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.ext.base.layer.HoverIndex;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.figure.IPaintable;
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

  private HoverIndex m_infoIndex = null;

  public TupleResultLineLayer( final ILayerProvider provider, final TupleResultDomainValueData< ? , ? > data, final IStyleSet styleSet )
  {
    super( provider, styleSet );

    m_valueData = data;

    m_valueData.setLayer( this );
  }

  @Override
  public void dispose( )
  {
    m_valueData.close();

    super.dispose();
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
    final IDataOperator<Object> dop = (IDataOperator<Object>)getCoordinateMapper().getDataOperator( value.getClass() );
    return dop.logicalToNumeric( value );
  }

  @Override
  public final EditInfo getHover( final Point pos )
  {
    if( m_infoIndex == null )
      return null;

    return m_infoIndex.findElement( pos );
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
    if( super.getTitle() == null && m_valueData != null )
    {
      final IObservation<TupleResult> obs = m_valueData.getObservation();
      final TupleResult tr = obs == null ? null : obs.getResult();
      if( tr != null )
      {
        final int targetComponentIndex = tr.indexOfComponent( m_valueData.getTargetComponentName() );
        if( targetComponentIndex > -1 )
          return tr.getComponent( targetComponentIndex ).getName();
      }
    }

    return super.getTitle();
  }

  protected String getTooltip( final IRecord record )
  {
    final IComponent domainComponent = m_valueData.getDomainComponent();
    final IComponent targetComponent = m_valueData.getTargetComponent();

    if( domainComponent == null || targetComponent == null )
      return null;

    final Object domainValue = m_valueData.getDomainValue( record );
    final Object targetValue = m_valueData.getTargetValue( record );

    if( domainValue == null || targetValue == null )
      return null;

    final String domainComponentLabel = ComponentUtilities.getComponentLabel( m_valueData.getDomainComponent() );
    final String targetComponentLabel = ComponentUtilities.getComponentLabel( m_valueData.getTargetComponent() );

    // FIXME: better tooltip formatting
//    final TooltipFormatter tooltip = new TooltipFormatter( comment );
//    tooltip.addLine( stationLabel, station.toString() );
//    tooltip.addLine( okLabel, ok.toString() );
//    tooltip.addLine( ukLabel, uk.toString() );
//    tooltip.addLine( widthLabel, bw.toString() );

    return String.format( TOOLTIP_FORMAT, new Object[] { domainComponentLabel, domainValue, targetComponentLabel, targetValue } );
  }

  private String getUnitFromComponent( final String id )
  {
    if( getValueData() == null )
      return null;

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

  protected TupleResultDomainValueData< ? , ? > getValueData( )
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
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    final TupleResultDomainValueData< ? , ? > data = getValueData();
    if( data == null )
      return;

    final List<Point> path = new ArrayList<>();

    final IObservation<TupleResult> observation = data.getObservation();
    if( observation == null )
      return;

    /* recreate hover info on every paint */
    clearInfoIndex();

    final TupleResult result = observation.getResult();

    int recordIndex = 0;
    for( final IRecord record : result )
    {
      final Point screen = paintRecord( record, recordIndex++ );
      if( screen != null )
        path.add( screen );
    }

    // TODO: ugly...
    paint( gc, path.toArray( new Point[] {} ) );
  }

  protected void clearInfoIndex( )
  {
    m_infoIndex = new HoverIndex();
  }

  private Point paintRecord( final IRecord record, final int recordIndex )
  {
    final Object domainValue = m_valueData.getDomainValue( record );
    final Object targetValue = m_valueData.getTargetValue( record );

    if( domainValue == null || targetValue == null )
      return null;

    // we have to check if all values are correct - an incorrect value means a null value - the axis would return 0
    // in that case
    final Point screen = getCoordinateMapper().logicalToScreen( domainValue, targetValue );

    addInfo( screen, record, recordIndex );

    return screen;
  }

  private void addInfo( final Point screen, final IRecord record, final int recordIndex )
  {
    // FIXME: lets have a nice figure!
    final IPaintable hoverFigure = null;

    final String tooltip = getTooltip( record );
    final Rectangle hover = RectangleUtils.buffer( screen, 5 );

    final EditInfo info = new EditInfo( this, hoverFigure, null, recordIndex, tooltip, null );
    addInfoElement( hover, info );
  }

  protected void addInfoElement( final Rectangle bounds, final EditInfo info )
  {
    m_infoIndex.addElement( bounds, info );
  }

  void onObservationChanged( )
  {
    m_infoIndex = null;

    getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
  }
}