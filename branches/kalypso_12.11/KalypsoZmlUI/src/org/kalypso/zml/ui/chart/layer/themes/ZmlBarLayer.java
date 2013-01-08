/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.zml.ui.chart.layer.themes;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataHelper;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.base.IZmlLayerProvider;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.chart.layer.filters.IZmlChartLayerFilter;

import de.openali.odysseus.chart.ext.base.layer.AbstractBarLayer;
import de.openali.odysseus.chart.ext.base.layer.BarPaintManager;
import de.openali.odysseus.chart.ext.base.layer.IBarLayerPainter;
import de.openali.odysseus.chart.framework.OdysseusChartExtensions;
import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlBarLayer extends AbstractBarLayer implements IZmlLayer
{
  private static final String PARAMETER_FIXED_HEIGHT = "fixedHeight"; //$NON-NLS-1$

  private final Pattern m_styleIndexPattern = Pattern.compile( "(.*?)(\\d*)" ); //$NON-NLS-1$

  private final Pattern m_styleWithIndexPattern = Pattern.compile( "(.*)([0-9]+)" ); //$NON-NLS-1$

  private IZmlLayerDataHandler m_handler;

  private String m_labelDescriptor;

  private final ZmlBarLayerRangeHandler m_range = new ZmlBarLayerRangeHandler( this );

  public ZmlBarLayer( final IZmlLayerProvider layerProvider, final IStyleSet styleSet, final URL context )
  {
    super( layerProvider, styleSet );

    setup( context );
  }

  @Override
  public void dispose( )
  {
    if( Objects.isNotNull( m_handler ) )
    {
      m_handler.dispose();
    }

    super.dispose();
  }

  @Override
  protected IAreaStyle getAreaStyle( )
  {
    // TODO: hmmm.....

    final IStyleSet styleSet = getStyleSet();
    final int index = ZmlLayerHelper.getLayerIndex( getIdentifier() );

    final StyleSetVisitor visitor = new StyleSetVisitor( true );
    return visitor.visit( styleSet, IAreaStyle.class, index );
  }

  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_handler;
  }

  @Override
  public IDataRange<Double> getDomainRange( )
  {
    return m_range.getDomainRange();
  }

  @Override
  public IZmlLayerProvider getProvider( )
  {
    return (IZmlLayerProvider)super.getProvider();
  }

  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    // FIXME: @Kim das ist genau das was ich vermeien wollte, jetzt funktioniert die Screen-Achse nicht mehr so wie gedacht ;-(
    final Number fixedHeight = getFixedHeight();
    if( fixedHeight != null )
    {
      final de.openali.odysseus.chart.framework.model.mapper.IAxis targetAxis = getTargetAxis();
      final int screenHeight = targetAxis.getScreenHeight();
      return new DataRange<>( 0.0, (double)screenHeight );
    }

    return m_range.getTargetRange();
  }

  @Override
  public String getTitle( )
  {
    if( m_labelDescriptor == null )
      return super.getTitle();

    final IObservation observation = (IObservation)getDataHandler().getAdapter( IObservation.class );
    if( observation == null )
      return m_labelDescriptor;

    final IAxis valueAxis = getDataHandler().getValueAxis();
    if( valueAxis == null )
      return m_labelDescriptor;

    return ObservationTokenHelper.replaceTokens( m_labelDescriptor, observation, valueAxis );
  }

  @Override
  public void onObservationChanged( final ContentChangeType type )
  {
    m_range.invalidateRange();

    invalidateHoverIndex();

    getEventHandler().fireLayerContentChanged( this, type );
  }

  @Override
  protected IBarLayerPainter createPainter( final BarPaintManager paintManager )
  {
    final IObservation observation = (IObservation)m_handler.getAdapter( IObservation.class );
    if( Objects.isNull( observation ) )
      return null;

    try
    {
      final String[] styleNames = findStyleNames();

      final IRequest request = m_handler.getRequest();
      final Period timestep = findtimestep( observation, request );

      final IAxis valueAxis = m_handler.getValueAxis();

      // TODO: implement forwards
      return new ZmlBarLayerBackwardsVisitor( this, paintManager, observation, valueAxis, request, timestep, styleNames );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      return null;
    }
  }

  Number getFixedHeight( )
  {
    try
    {
      final String textValue = getProvider().getParameterContainer().getParameterValue( PARAMETER_FIXED_HEIGHT, null );
      final Object logical = getTargetAxis().xmlStringToLogical( textValue );
      if( logical instanceof Number )
        return (Number)logical;

      // TODO: better error handling
      return null;
    }
    catch( final MalformedValueException e )
    {
      // TODO: better error handling
      e.printStackTrace();
      return null;
    }
  }

  // FIXME: too special for this layer, make more general...
  private String[] findStyleNames( )
  {
    final int index = ZmlLayerHelper.getLayerIndex( getIdentifier() );

    final String[] defaultStyles = findDefaultStyles();

    /* find all styles for this index */
    final String[] styleNames = findStyles( index );

    if( styleNames.length != 0 )
      return ArrayUtils.addAll( styleNames, defaultStyles );

    // reuse styles of index 0 if no styles are defined for this index
    return ArrayUtils.addAll( findStyles( 0 ), defaultStyles );
  }

  /**
   * Finds all styles without index
   */
  private String[] findDefaultStyles( )
  {
    final Collection<String> styleNames = new ArrayList<>();

    final IStyleSet styleSet = getStyleSet();

    final Map<String, IStyle> styles = styleSet.getStyles();

    for( final String styleName : styles.keySet() )
    {
      final IStyle style = styles.get( styleName );
      if( style instanceof IAreaStyle )
      {
        if( !m_styleWithIndexPattern.matcher( styleName ).matches() )
          styleNames.add( styleName );
      }
    }

    return styleNames.toArray( new String[styleNames.size()] );
  }

  /**
   * Find all style for a given layer index
   */
  private String[] findStyles( final int layerIndex )
  {
    final Collection<String> styleNames = new ArrayList<>();

    final IStyleSet styleSet = getStyleSet();

    final Map<String, IStyle> styles = styleSet.getStyles();

    final String styleSuffig = "_" + layerIndex; //$NON-NLS-1$

    for( final String styleName : styles.keySet() )
    {
      final IStyle style = styles.get( styleName );
      if( style instanceof IAreaStyle )
      {
        if( styleName.endsWith( styleSuffig ) )
          styleNames.add( styleName );
      }
    }

    return styleNames.toArray( new String[styleNames.size()] );
  }

  private Period findtimestep( final IObservation observation, final IRequest request ) throws SensorException
  {
    final Period timestep = MetadataHelper.getTimestep( observation.getMetadataList() );
    if( timestep != null )
      return timestep;

    // REMARK: we assume here that only old and short timeseries have no timstep, else we would get a performance
    // problem here
    final ITupleModel values = observation.getValues( request );

    return TimeseriesUtils.guessTimestep( values );
  }

  @Override
  public void setDataHandler( final IZmlLayerDataHandler handler )
  {
    if( m_handler != null )
      m_handler = handler;

    m_handler = handler;
  }

  @Override
  public void setLabelDescriptor( final String labelDescriptor )
  {
    m_labelDescriptor = labelDescriptor;
  }

  private void setup( final URL context )
  {
    final IZmlLayerProvider provider = getProvider();
    final ZmlObsProviderDataHandler handler = new ZmlObsProviderDataHandler( this, provider.getTargetAxisId() );
    try
    {
      handler.load( provider, context );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    setDataHandler( handler );
  }

  IChartLayerFilter getStyleFilter( final String styleName, final IObservation observation )
  {
    final Matcher matcher = m_styleIndexPattern.matcher( styleName );
    if( !matcher.matches() || matcher.groupCount() < 1 )
      return null;

    final String baseName = matcher.group( 1 );

    final String filterParameter = "styleFilter." + baseName; //$NON-NLS-1$

    final String filterId = getProvider().getParameterContainer().getParameterValue( filterParameter, null );
    if( StringUtils.isBlank( filterId ) )
      return null;

    final IChartLayerFilter filter = OdysseusChartExtensions.createFilter( filterId );
    if( filter instanceof IZmlChartLayerFilter )
    {
      final MetadataList metadata = observation.getMetadataList();
      final IAxis[] axes = observation.getAxes();
      ((IZmlChartLayerFilter)filter).init( metadata, axes );
    }

    return filter;
  }
}