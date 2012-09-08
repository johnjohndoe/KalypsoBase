/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.diagview.jfreechart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Spacer;
import org.jfree.ui.TextAnchor;
import org.kalypso.contribs.java.lang.reflect.ClassUtilities;
import org.kalypso.contribs.java.lang.reflect.ClassUtilityException;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.diagview.AxisMapping;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewCurve;
import org.kalypso.ogc.sensor.diagview.DiagViewCurve.AlarmLevel;
import org.kalypso.ogc.sensor.diagview.DiagramAxis;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.template.ObsViewItem;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;

/**
 * A plot for IObservation.
 *
 * @author schlienger
 */
public class ObservationPlot extends XYPlot
{
  /** maps the diagram axis (from the template) to the chart axis */
  private final transient Map<DiagramAxis, ValueAxis> m_diag2chartAxis = new HashMap<>();

  /** maps the chart axis to its position in the plot */
  private final transient Map<ValueAxis, Integer> m_chartAxes2Pos = new HashMap<>();

  /** maps the diagram axes (from the template) to a dataset */
  private final transient Map<DiagramAxis, CurveDataset> m_diagAxis2ds = new HashMap<>();

  /** maps the diagram curve to the data serie */
  private final transient Map<DiagViewCurve, XYCurveSerie> m_curve2serie = new HashMap<>();

  /** maps the series to their datasets */
  private final transient Map<XYCurveSerie, CurveDataset> m_serie2dataset = new HashMap<>();

  private final transient Map<Double, AlarmLevelPlotElement> m_yConsts = new HashMap<>();

  private final transient Map<Long, Marker> m_markers = new HashMap<>();

  private TimeZone m_timezone;

  /**
   * Constructor.
   */
  public ObservationPlot( final DiagView view ) throws SensorException
  {
    // space between axes and data area
    setAxisOffset( new Spacer( Spacer.ABSOLUTE, 5, 5, 5, 5 ) );

    // standard renderer
    setRenderer( new StandardXYItemRenderer( StandardXYItemRenderer.LINES ) );

    final TimeZone viewzone = view.getTimezone();
    final TimeZone timezone = viewzone == null ? KalypsoCorePlugin.getDefault().getTimeZone() : viewzone;
    setTimezone( timezone );

    final ObsViewItem[] curves = view.getItems();
    for( final ObsViewItem element : curves )
      addCurve( (DiagViewCurve) element );

    setNoDataMessage( Messages.getString( "org.kalypso.ogc.sensor.diagview.jfreechart.ObservationPlot.1" ) ); //$NON-NLS-1$
  }

  public final void dispose( )
  {
    clearCurves();
  }

  /**
   * Adds a diagram axis and configures it for the use in this plot.
   *
   * @param axis
   *          can be null, if present it is used to define a best suited formatter for the chart axis
   */
  private synchronized void addDiagramAxis( final DiagramAxis diagAxis, final IAxis axis ) throws SensorException
  {
    if( diagAxis == null )
      throw new IllegalArgumentException( "DiagramAxis is null" ); //$NON-NLS-1$

    try
    {
      final String axisType = axis == null ? null : axis.getType();
      final String axisClass = TimeseriesUtils.getAxisClassFor( axisType );
      if( axisClass == null )
      {
        final String msg = String.format( "No Axis-Class defined for type '%s'. Must be defined in timeseries.ini or /KalypsoCore/src/org/kalypso/ogc/sensor/timeseries/resource/config.properties", axisType ); //$NON-NLS-1$
        throw new SensorException( msg );
      }

      final String axisLabel = diagAxis.toFullString();
      // Small hack:_ if label is null, we need to instantiate with an string, else the reflection does not work.
      final String[] arguments = axisLabel == null ? new String[] { "" } : new String[] { axisLabel }; //$NON-NLS-1$
      final ValueAxis vAxis = (ValueAxis) ClassUtilities.newInstance( axisClass, ValueAxis.class, ObservationPlot.class.getClassLoader(), arguments );
      if( axisLabel == null )
        vAxis.setLabel( null );

      setTimezone( vAxis );
      vAxis.setInverted( diagAxis.isInverted() );

      if( diagAxis.getLowerMargin() != null )
        vAxis.setLowerMargin( diagAxis.getLowerMargin().doubleValue() );

      if( diagAxis.getUpperMaring() != null )
        vAxis.setUpperMargin( diagAxis.getUpperMaring().doubleValue() );

      final AxisLocation loc = getLocation( diagAxis );

      if( diagAxis.getDirection().equals( DiagramAxis.DIRECTION_HORIZONTAL ) )
      {
        final int pos = getAdequateDomainPos();
        setDomainAxis( pos, vAxis );
        setDomainAxisLocation( pos, loc );

        m_chartAxes2Pos.put( vAxis, new Integer( pos ) );
      }
      else
      {
        final int pos = getAdequateRangePos();
        setRangeAxis( pos, vAxis );
        setRangeAxisLocation( pos, loc );

        m_chartAxes2Pos.put( vAxis, new Integer( pos ) );
      }

      m_diag2chartAxis.put( diagAxis, vAxis );
    }
    catch( final ClassUtilityException e )
    {
      throw new SensorException( e );
    }
  }

  /**
   * @return adequate position for a new domain axis
   */
  private int getAdequateDomainPos( )
  {
    final int count = getDomainAxisCount();
    if( count == 0 )
      return 0;

    for( int i = 0; i < count; i++ )
      if( getDomainAxis( i ) == null )
        return i;

    return count;
  }

  /**
   * @return adequate position for a new range axis
   */
  private int getAdequateRangePos( )
  {
    final int count = getRangeAxisCount();
    if( count == 0 )
      return 0;

    for( int i = 0; i < count; i++ )
      if( getRangeAxis( i ) == null )
        return i;

    return count;
  }

  /**
   * @return adequate position for a new dataset
   */
  private int getAdequateDatasetPos( )
  {
    final int count = getDatasetCount();
    if( count == 0 )
      return 0;

    for( int i = 0; i < count; i++ )
      if( getDataset( i ) == null )
        return i;

    return count;
  }

  /**
   * Removes all curves from plot.
   */
  public final synchronized void clearCurves( )
  {
    for( int i = 0; i < getDatasetCount(); i++ )
      setDataset( i, null );

    clearBackground();

    m_serie2dataset.clear();
    m_curve2serie.clear();

    m_diagAxis2ds.clear();

    m_chartAxes2Pos.clear();
    m_diag2chartAxis.clear();

    clearDomainMarkers();
    clearAnnotations();

    m_yConsts.clear();
    m_markers.clear();

    clearDomainAxes();
    clearRangeAxes();
  }

  /**
   * Adds a curve to the plot
   */
  public final synchronized void addCurve( final DiagViewCurve curve ) throws SensorException
  {
    if( curve == null || !curve.isShown() || m_curve2serie.containsKey( curve ) )
      return;

    final AxisMapping[] mings = curve.getMappings();

    IAxis xAxis = null;
    DiagramAxis xDiagAxis = null;
    IAxis yAxis = null;
    DiagramAxis yDiagAxis = null;

    if( mings != null )
    {
      for( final AxisMapping element : mings )
      {
        final DiagramAxis diagAxis = element.getDiagramAxis();

        if( diagAxis == null )
          continue;

        // check if this axis is already present in this plot
        if( !m_diag2chartAxis.containsKey( diagAxis ) )
          addDiagramAxis( diagAxis, element.getObservationAxis() );

        if( diagAxis.getDirection().equals( DiagramAxis.DIRECTION_HORIZONTAL ) )
        {
          xAxis = element.getObservationAxis();
          xDiagAxis = diagAxis;
        }
        else
        {
          yAxis = element.getObservationAxis();
          yDiagAxis = diagAxis;
        }
      }
    }

    if( xAxis == null || yAxis == null || xDiagAxis == null || yDiagAxis == null )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.diagview.jfreechart.ObservationPlot.2", curve ) ); //$NON-NLS-1$

    final XYCurveSerie serie = new XYCurveSerie( curve, xAxis, yAxis, xDiagAxis, yDiagAxis );

    m_curve2serie.put( curve, serie );

    final DiagramAxis key = yDiagAxis;

    CurveDataset cds = m_diagAxis2ds.get( key );

    if( cds == null )
    {
      cds = new CurveDataset();

      m_diagAxis2ds.put( key, cds );

      final int pos = getAdequateDatasetPos();
      setDataset( pos, cds );

      final XYItemRenderer renderer = getRenderer( yAxis.getType() );
      setRenderer( pos, renderer );

      mapDatasetToDomainAxis( pos, m_chartAxes2Pos.get( m_diag2chartAxis.get( xDiagAxis ) ).intValue() );
      mapDatasetToRangeAxis( pos, m_chartAxes2Pos.get( m_diag2chartAxis.get( yDiagAxis ) ).intValue() );
    }

    // if a curve gets removed meanwhile, the mapping seriespos -> curvecolor
    // gets invalid! always reset all colors of all curves
    final Color curveColor = curve.getColor();
    final Stroke curveStroke = curve.getStroke();
    cds.addCurveSerie( serie, curveColor, curveStroke, getRenderer( indexOf( cds ) ) );

    m_serie2dataset.put( serie, cds );

    analyseCurve( curve );
  }

  private void analyseCurve( final DiagViewCurve curve ) throws SensorException
  {
    final IObservation obs = curve.getObservation();

    if( curve.getView().isFeatureEnabled( ITimeseriesConstants.FEATURE_FORECAST ) )
    {
      // add a marker if the obs is a forecast
      final DateRange fr = TimeseriesUtils.isTargetForecast( obs );
      if( fr != null )
      {
        final Long begin = new Long( fr.getFrom().getTime() );
        if( !m_markers.containsKey( begin ) )
        {
          final long end = fr.getTo().getTime();
          final Marker marker = createMarker( begin.doubleValue(), end, Messages.getString("ObservationPlot.0"), TimeseriesUtils.getColorForMD( ITimeseriesConstants.MD_VORHERSAGE ) ); //$NON-NLS-1$

          addDomainMarker( marker, Layer.BACKGROUND );

          m_markers.put( begin, marker );
        }
      }
    }

    if( obs != null )
    {
      // add a constant Y line if obs has alarmstufen
      if( curve.isDisplayAlarmLevel() )
      {
        final AlarmLevel[] alarms = curve.getAlarmLevels();
        for( final AlarmLevel element : alarms )
        {
          final Double value = new Double( element.value );
          if( !m_yConsts.containsKey( value ) )
          {
            final XYCurveSerie xyc = m_curve2serie.get( curve );
            final double x;
            if( xyc.getItemCount() > 1 )
              x = xyc.getXValue( 1 ).doubleValue();
            else
              x = getDomainAxis().getLowerBound();

            final AlarmLevelPlotElement vac = new AlarmLevelPlotElement( element, x, xyc.getYDiagAxis() );
            m_yConsts.put( value, vac );
          }
        }
      }
    }
  }

  /**
   * Refreshes the plot in order to take the enabled features of the view into account
   */
  public final void refreshMetaInformation( )
  {
    // clear all markers and extra informations
    clearDomainMarkers();
    clearAnnotations();
    m_yConsts.clear();
    m_markers.clear();

    // step through curves and analyse them

    for( final Object element : m_curve2serie.keySet() )
    {
      final DiagViewCurve curve = (DiagViewCurve) element;

      try
      {
        analyseCurve( curve );
      }
      catch( final SensorException e )
      {
        Logger.getLogger( getClass().getName() ).warning( e.getLocalizedMessage() );
      }
    }
  }

  /**
   * Removes the curve from the plot
   */
  public synchronized void removeCurve( final DiagViewCurve curve )
  {
    final XYCurveSerie serie = m_curve2serie.get( curve );

    if( serie != null )
    {
      final CurveDataset ds = m_serie2dataset.get( serie );

      if( ds != null )
      {
        ds.removeCurveSerie( serie );

        // if dataset is empty, also remove it and the range axis to which it
        // belongs
        if( ds.getSeriesCount() == 0 )
        {
          // and remove the dataset
          for( int i = 0; i < getDatasetCount(); i++ )
          {
            if( getDataset( i ) == ds )
            {
              setDataset( i, null );

              break;
            }
          }

          // step though axes and remove the one that is associated to
          // the dataset we want to remove
          final Iterator<DiagramAxis> it = m_diagAxis2ds.keySet().iterator();
          while( it.hasNext() )
          {
            final DiagramAxis dAxis = it.next();
            if( m_diagAxis2ds.get( dAxis ) == ds )
            {
              final ValueAxis cAxis = m_diag2chartAxis.get( dAxis );
              final Integer pos = m_chartAxes2Pos.get( cAxis );

              // trick: if it is the only axis, then do not remove it
              // else NullPointerException in drawQuadrants (JFreeChart)
              if( getRangeAxis() != getRangeAxis( pos.intValue() ) || getRangeAxisCount() > 1 )
              {
                setRangeAxis( pos.intValue(), null );
                // m_chartAxes2Pos.remove( cAxis );
                m_diag2chartAxis.remove( dAxis );
              }

              it.remove();

              // break, that's it
              break;
            }
          }
        }
      }

      m_curve2serie.remove( curve );

      if( m_curve2serie.size() == 0 )
        clearCurves();
    }
  }

  private void clearBackground( )
  {
    setBackgroundImage( null );
  }

  /**
   * overwritten to return a default axis when no real axes defined yet
   *
   * @see org.jfree.chart.plot.XYPlot#getDomainAxis()
   */
  @Override
  public final synchronized ValueAxis getDomainAxis( )
  {
    if( m_diag2chartAxis.size() == 0 )
      return new NumberAxis();

    return super.getDomainAxis();
  }

  /**
   * Overriden to return a default axis when no real axes defined yet
   *
   * @see org.jfree.chart.plot.XYPlot#getRangeAxis()
   */
  @Override
  public final synchronized ValueAxis getRangeAxis( )
  {
    if( m_diag2chartAxis.size() == 0 )
      return new NumberAxis();

    for( int i = 0; i < getRangeAxisCount(); i++ )
    {
      final ValueAxis rangeAxis = getRangeAxis( i );
      if( rangeAxis != null )
        return rangeAxis;
    }

    return new NumberAxis();
  }

  /**
   * overriden to also draw our alarmlevels
   *
   * @see org.jfree.chart.plot.XYPlot#drawAnnotations(java.awt.Graphics2D, java.awt.geom.Rectangle2D,
   *      org.jfree.chart.plot.PlotRenderingInfo)
   */
  @Override
  public final synchronized void drawAnnotations( final Graphics2D g2d, final Rectangle2D rec, final PlotRenderingInfo arg2 )
  {
    super.drawAnnotations( g2d, rec, arg2 );

    drawAlarmLevels( g2d, rec );
  }

  /**
   * Draw alarmlevels (horizontal line and text annotation)
   */
  private void drawAlarmLevels( final Graphics2D g2, final Rectangle2D dataArea )
  {
    for( final Object element : m_yConsts.keySet() )
    {
      final AlarmLevelPlotElement vac = m_yConsts.get( element );

      final ValueAxis axis = m_diag2chartAxis.get( vac.getAxis() );
      if( axis == null )
        continue;

      if( axis.getRange().contains( vac.getAlarm().value ) )
      {
        final double yy = axis.valueToJava2D( vac.getAlarm().value, dataArea, RectangleEdge.LEFT );
        final Line2D line = new Line2D.Double( dataArea.getMinX(), yy, dataArea.getMaxX(), yy );
        // always set stroke, else we got the stroke from the last drawn line
        g2.setStroke( AlarmLevelPlotElement.STROKE_ALARM );
        g2.setPaint( vac.getAlarm().color );
        g2.draw( line );

        // and draw the text annotation: if annotation is outside (on top); label it below the line
        if( yy < dataArea.getMinY() + 20 )
          vac.getAnnotation().setAngle( Math.toRadians( 20 ) );
        else
          vac.getAnnotation().setAngle( Math.toRadians( 340 ) );

        vac.getAnnotation().draw( g2, this, dataArea, getDomainAxis(), axis );
      }
    }
  }

  /**
   * Helper that creates a marker
   */
  private static Marker createMarker( final double start, final double end, final String label, final Color color )
  {
    final IntervalMarker marker = new IntervalMarker( start, end );
    marker.setPaint( color );
    marker.setLabel( label );
    marker.setLabelAnchor( RectangleAnchor.CENTER );
    marker.setLabelTextAnchor( TextAnchor.CENTER );

    return marker;
  }

  /**
   * Returns the adequate renderer for the given axis type.
   */
  private XYItemRenderer getRenderer( final String axisType )
  {
    // TODO: also overwrite bar renderer in order to hide legend
    if( axisType.equals( ITimeseriesConstants.TYPE_RAINFALL ) )
      return new XYBarRenderer();

    if( axisType.equals( ITimeseriesConstants.TYPE_POLDER_CONTROL ) )
      return new XYBarRenderer();

    return new XYCurveRenderer( StandardXYItemRenderer.LINES );
  }

  /**
   * @param diagAxis
   * @return location according to axis
   */
  private static AxisLocation getLocation( final DiagramAxis diagAxis )
  {
    if( diagAxis.getPosition().equals( DiagramAxis.POSITION_BOTTOM ) )
    {
      return AxisLocation.BOTTOM_OR_LEFT;
    }
    else if( diagAxis.getPosition().equals( DiagramAxis.POSITION_TOP ) )
    {
      return AxisLocation.TOP_OR_LEFT;
    }
    else if( diagAxis.getPosition().equals( DiagramAxis.POSITION_LEFT ) )
    {
      return AxisLocation.TOP_OR_LEFT;
    }
    else if( diagAxis.getPosition().equals( DiagramAxis.POSITION_RIGHT ) )
    {
      return AxisLocation.TOP_OR_RIGHT;
    }

    // default
    return AxisLocation.BOTTOM_OR_LEFT;
  }

  /**
   * Special tick units for kalypso
   */
  public static TickUnitSource createStandardDateTickUnits( final TimeZone zone )
  {
    if( zone == null )
    {
      throw new IllegalArgumentException( "Null 'zone' argument." ); //$NON-NLS-1$
    }
    final TickUnits units = new TickUnits();

    // date formatters
    // DateFormat f1 = new SimpleDateFormat("HH:mm:ss.SSS");
    // DateFormat f2 = new SimpleDateFormat("HH:mm:ss");
    // DateFormat f3 = new SimpleDateFormat("HH:mm");
    // DateFormat f4 = new SimpleDateFormat("d-MMM, HH:mm");
    // DateFormat f5 = new SimpleDateFormat("d-MMM");
    // DateFormat f6 = new SimpleDateFormat("MMM-yyyy");
    // DateFormat f7 = new SimpleDateFormat("yyyy");

    final DateFormat f1 = new SimpleDateFormat( "dd.MM HH:mm:ss.SSS" ); //$NON-NLS-1$
    final DateFormat f2 = new SimpleDateFormat( "dd.MM HH:mm:ss" ); //$NON-NLS-1$
    final DateFormat f3 = new SimpleDateFormat( "dd.MM HH:mm" ); //$NON-NLS-1$
    final DateFormat f4 = new SimpleDateFormat( "dd.MM HH:mm" ); //$NON-NLS-1$
    final DateFormat f5 = new SimpleDateFormat( "dd.MM" ); //$NON-NLS-1$
    final DateFormat f6 = new SimpleDateFormat( "dd.MM.yy" ); //$NON-NLS-1$
    final DateFormat f7 = new SimpleDateFormat( "yyyy" ); //$NON-NLS-1$

    f1.setTimeZone( zone );
    f2.setTimeZone( zone );
    f3.setTimeZone( zone );
    f4.setTimeZone( zone );
    f5.setTimeZone( zone );
    f6.setTimeZone( zone );
    f7.setTimeZone( zone );

    // milliseconds
    units.add( new DateTickUnit( DateTickUnit.MILLISECOND, 1, f1 ) );
    units.add( new DateTickUnit( DateTickUnit.MILLISECOND, 5, DateTickUnit.MILLISECOND, 1, f1 ) );
    units.add( new DateTickUnit( DateTickUnit.MILLISECOND, 10, DateTickUnit.MILLISECOND, 1, f1 ) );
    units.add( new DateTickUnit( DateTickUnit.MILLISECOND, 25, DateTickUnit.MILLISECOND, 5, f1 ) );
    units.add( new DateTickUnit( DateTickUnit.MILLISECOND, 50, DateTickUnit.MILLISECOND, 10, f1 ) );
    units.add( new DateTickUnit( DateTickUnit.MILLISECOND, 100, DateTickUnit.MILLISECOND, 10, f1 ) );
    units.add( new DateTickUnit( DateTickUnit.MILLISECOND, 250, DateTickUnit.MILLISECOND, 10, f1 ) );
    units.add( new DateTickUnit( DateTickUnit.MILLISECOND, 500, DateTickUnit.MILLISECOND, 50, f1 ) );

    // seconds
    units.add( new DateTickUnit( DateTickUnit.SECOND, 1, DateTickUnit.MILLISECOND, 50, f2 ) );
    units.add( new DateTickUnit( DateTickUnit.SECOND, 5, DateTickUnit.SECOND, 1, f2 ) );
    units.add( new DateTickUnit( DateTickUnit.SECOND, 10, DateTickUnit.SECOND, 1, f2 ) );
    units.add( new DateTickUnit( DateTickUnit.SECOND, 30, DateTickUnit.SECOND, 5, f2 ) );

    // minutes
    units.add( new DateTickUnit( DateTickUnit.MINUTE, 1, DateTickUnit.SECOND, 5, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.MINUTE, 2, DateTickUnit.SECOND, 10, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.MINUTE, 5, DateTickUnit.MINUTE, 1, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.MINUTE, 10, DateTickUnit.MINUTE, 1, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.MINUTE, 15, DateTickUnit.MINUTE, 5, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.MINUTE, 20, DateTickUnit.MINUTE, 5, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.MINUTE, 30, DateTickUnit.MINUTE, 5, f3 ) );

    // hours
    units.add( new DateTickUnit( DateTickUnit.HOUR, 1, DateTickUnit.MINUTE, 5, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.HOUR, 2, DateTickUnit.MINUTE, 10, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.HOUR, 4, DateTickUnit.MINUTE, 30, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.HOUR, 6, DateTickUnit.HOUR, 1, f3 ) );
    units.add( new DateTickUnit( DateTickUnit.HOUR, 12, DateTickUnit.HOUR, 1, f4 ) );

    // days
    units.add( new DateTickUnit( DateTickUnit.DAY, 1, DateTickUnit.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnit.DAY, 2, DateTickUnit.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnit.DAY, 3, DateTickUnit.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnit.DAY, 4, DateTickUnit.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnit.DAY, 5, DateTickUnit.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnit.DAY, 6, DateTickUnit.HOUR, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnit.DAY, 7, DateTickUnit.DAY, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnit.DAY, 10, DateTickUnit.DAY, 1, f5 ) );
    units.add( new DateTickUnit( DateTickUnit.DAY, 15, DateTickUnit.DAY, 1, f5 ) );

    // months
    units.add( new DateTickUnit( DateTickUnit.MONTH, 1, DateTickUnit.DAY, 1, f6 ) );
    units.add( new DateTickUnit( DateTickUnit.MONTH, 2, DateTickUnit.DAY, 1, f6 ) );
    units.add( new DateTickUnit( DateTickUnit.MONTH, 3, DateTickUnit.MONTH, 1, f6 ) );
    units.add( new DateTickUnit( DateTickUnit.MONTH, 4, DateTickUnit.MONTH, 1, f6 ) );
    units.add( new DateTickUnit( DateTickUnit.MONTH, 6, DateTickUnit.MONTH, 1, f6 ) );

    // years
    units.add( new DateTickUnit( DateTickUnit.YEAR, 1, DateTickUnit.MONTH, 1, f7 ) );
    units.add( new DateTickUnit( DateTickUnit.YEAR, 2, DateTickUnit.MONTH, 3, f7 ) );
    units.add( new DateTickUnit( DateTickUnit.YEAR, 5, DateTickUnit.YEAR, 1, f7 ) );
    units.add( new DateTickUnit( DateTickUnit.YEAR, 10, DateTickUnit.YEAR, 1, f7 ) );
    units.add( new DateTickUnit( DateTickUnit.YEAR, 25, DateTickUnit.YEAR, 5, f7 ) );
    units.add( new DateTickUnit( DateTickUnit.YEAR, 50, DateTickUnit.YEAR, 10, f7 ) );
    units.add( new DateTickUnit( DateTickUnit.YEAR, 100, DateTickUnit.YEAR, 20, f7 ) );

    return units;

  }

  /**
   * mini helper class for storing a value and a color
   *
   * @author schlienger
   */
  private static final class AlarmLevelPlotElement
  {
    /** Stroke, with wich the alarm-levels get drawn */
    public static final Stroke STROKE_ALARM = new BasicStroke( 1.0f );

    private final AlarmLevel m_alarm;

    private final String m_label;

    private final DiagramAxis m_axis;

    private final XYPointerAnnotation m_annotation;

    public AlarmLevelPlotElement( final AlarmLevel al, final double xCoord, final DiagramAxis diagAxis )
    {
      m_alarm = al;
      m_label = al.label + " (" + al.value + ")"; //$NON-NLS-1$ //$NON-NLS-2$
      m_axis = diagAxis;
      m_annotation = new XYPointerAnnotation( al.label, xCoord, al.value, 0 );
      getAnnotation().setArrowLength( 10.0 );
      getAnnotation().setLabelOffset( 30 );
      getAnnotation().setArrowPaint( al.color );
      getAnnotation().setPaint( al.color );
    }

    @Override
    public String toString( )
    {
      return getClass().getName() + ": " + m_label + " " + getAlarm() + " " + getAxis().getLabel(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    protected XYPointerAnnotation getAnnotation( )
    {
      return m_annotation;
    }

    protected AlarmLevel getAlarm( )
    {
      return m_alarm;
    }

    protected DiagramAxis getAxis( )
    {
      return m_axis;
    }
  }

  public final void setTimezone( final TimeZone timezone )
  {
    m_timezone = timezone;

    for( int i = 0; i < getDomainAxisCount(); i++ )
    {
      final ValueAxis axis = getDomainAxis( i );
      setTimezone( axis );
    }

    for( int i = 0; i < getRangeAxisCount(); i++ )
    {
      final ValueAxis axis = getRangeAxis( i );
      setTimezone( axis );
    }
  }

  private void setTimezone( final ValueAxis axis )
  {
    if( axis instanceof DateAxis )
    {
      final DateAxis da = (DateAxis) axis;
      final DateFormat df = da.getDateFormatOverride() == null ? null : da.getDateFormatOverride();
      if( df != null )
      {
        df.setTimeZone( m_timezone );
        da.setDateFormatOverride( df );
      }

      final TickUnitSource source = createStandardDateTickUnits( m_timezone );
      da.setStandardTickUnits( source );
    }
  }
}