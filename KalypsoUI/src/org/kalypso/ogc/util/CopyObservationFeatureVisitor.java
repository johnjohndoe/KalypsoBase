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
package org.kalypso.ogc.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITuppleModel;
import org.kalypso.ogc.sensor.MetadataList;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.ogc.sensor.timeseries.forecast.ForecastTuppleModel;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ogc.util.CopyObservationSourceDelegate.ObservationSource;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree_impl.model.feature.visitors.MonitorFeatureVisitor.IMonitoredFeatureVisitor;

/**
 * @author Gernot Belger
 */
public class CopyObservationFeatureVisitor extends AbstractMonitoredFeatureVisitor implements FeatureVisitor, IMonitoredFeatureVisitor
{
  /** Used to search/replace metadata content with properties of the visited feature */
  private static Pattern PATTERN_FEATURE_PROPERTY = Pattern.compile( "\\Q${property;\\E([^;]*)\\Q;\\E([^}]*)\\Q}\\E" ); //$NON-NLS-1$

  private static final QName FID_QNAME = new QName( "FID" ); //$NON-NLS-1$

  private final URL m_context;

  private final IUrlResolver m_urlResolver;

  private final ILogger m_logger;

  private final DateRange m_forecastRange;

  private final DateRange m_targetRange;

  private final Properties m_metadata;

  private final CopyObservationTimeSeriesDelegate m_timeSeriesDelegate;

  private final CopyObservationSourceDelegate m_sourceDelegate;

  /**
   * @param context
   *          context to resolve relative url
   * @param urlResolver
   *          resolver for urls
   * @param metadata
   *          All entries will be added to the target observation
   */
  public CopyObservationFeatureVisitor( final URL context, final IUrlResolver urlResolver, final CopyObservationTimeSeriesDelegate timeSeriesDelegate, final CopyObservationSourceDelegate sourceDelegate, final Properties metadata, final DateRange targetRange, final DateRange forecastRange, final ILogger logger )
  {
    m_context = context;
    m_urlResolver = urlResolver;
    m_timeSeriesDelegate = timeSeriesDelegate;
    m_sourceDelegate = sourceDelegate;
    m_metadata = metadata;
    m_forecastRange = forecastRange;
    m_targetRange = targetRange;
    m_logger = logger;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureVisitor#visit(org.kalypsodeegree.model.feature.Feature)
   */
  public boolean visit( final Feature feature )
  {
    try
    {
      final String targetHref = m_timeSeriesDelegate.getTargetHref( feature );
      setCurrentSubTask( targetHref );

      ObservationSource[] sources = m_sourceDelegate.getObservationSources( feature );

      final IFile targetfile = createTargetFile( targetHref );
      if( targetfile == null )
        return true;

      final IObservation resultObs = combineResultObservation( sources );
      updateMetaData( resultObs, feature );

      final IRequest request = new ObservationRequest( m_targetRange );
// // FIXME: this causes two calls to the repository and eventually two calls to the underlying database
// KalypsoProtocolWriter.analyseValues( resultObs, resultObs.getValues( request ), m_logger );

      wrtieTargetObservation( targetfile, resultObs, request );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      m_logger.log( Level.WARNING, LoggerUtilities.CODE_SHOW_DETAILS, Messages.getString( "org.kalypso.ogc.util.CopyObservationFeatureVisitor.3" ) + feature.getId() + "\t" + e.getLocalizedMessage() );//$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    return true;
  }

  private IFile createTargetFile( final String targetHref ) throws MalformedURLException
  {
    return ResourceUtilities.findFileFromURL( m_urlResolver.resolveURL( m_context, targetHref ) );
  }

  private void wrtieTargetObservation( final IFile targetfile, final IObservation resultObs, final IRequest request ) throws SensorException
  {
    final IPath location = targetfile.getLocation();
    final File file = location.toFile();
    file.getParentFile().mkdirs();
    ZmlFactory.writeToFile( resultObs, file, request );
  }

  private void updateMetaData( final IObservation resultObs, final Feature feature )
  {
    TimeserieUtils.setDateRange( resultObs, m_targetRange );

    /* set forecast metadata, might be used in diagram for instance to mark the forecast range */
    TimeserieUtils.setForecast( resultObs, m_forecastRange );

    // put additional metadata that we got from outside
    final MetadataList resultMetadata = resultObs.getMetadataList();
    for( final Entry<Object, Object> element : m_metadata.entrySet() )
    {
      final Entry<Object, Object> entry = element;
      final String metaValue = replaceMetadata( feature, (String) entry.getValue() );
      final String metaKey = (String) entry.getKey();
      resultMetadata.put( metaKey, metaValue );
    }
  }

  private IObservation combineResultObservation( final ObservationSource[] sources ) throws SensorException
  {
    List<ITuppleModel> models = new ArrayList<ITuppleModel>();
    for( ObservationSource source : sources )
    {
      IObservation observation = source.OBSERVATION;
      ObservationRequest request = new ObservationRequest( source.DATE_RANGE );

      models.add( observation.getValues( request ) );
    }

    if( models.size() == 0 )
      return null;

    final ForecastTuppleModel tuppleModel = new ForecastTuppleModel( models.toArray( new ITuppleModel[] {} ) );

    IObservation baseObservation = sources[0].OBSERVATION;
    final MetadataList metadataList = (MetadataList) baseObservation.getMetadataList().clone();
    final IAxis[] axes = baseObservation.getAxisList();

    return new SimpleObservation( null, null, null, false, metadataList, axes, tuppleModel );

// final ForecastFilter fc = new ForecastFilter();
// fc.initFilter( sourceObses, sourceObses[0], null );
// return fc;
  }

  /**
   * Search/replace metadata values with properties from the current feature. <br>
   * REMARK: The search/replace mechanism is similar to the one used in Kalypso 2.0 and this code should be replaced as
   * soon as we change to the new version of Kalypso.
   */
  private String replaceMetadata( final Feature feature, final String metaValue )
  {
    if( metaValue == null )
      return null;

    final Matcher matcher = PATTERN_FEATURE_PROPERTY.matcher( metaValue );
    if( matcher.matches() )
    {
      final String propertyName = matcher.group( 1 );
      final String defaultValue = matcher.group( 2 );
      final QName propertyQName = createQName( propertyName );

      if( FID_QNAME.equals( propertyQName ) )
        return feature.getId();

      final Object property = feature.getProperty( propertyQName.getLocalPart() );
      if( property == null )
        return defaultValue;

      return property.toString();
    }

    return metaValue;
  }

  /**
   * REMARK: this is another backport from Kalypso 2.0 (QNameUtilities) remove as soon as we go to thsat version. syntax
   * of fragmentedFullQName :
   * 
   * <pre>
   *         &lt;namespace&gt;#&lt;localpart&gt;
   * </pre>
   * 
   * example: fragmentedFullQName = www.w3c.org#index.html <br/>
   * If no '#' is given, a qname with only a localPart is created.
   * 
   * @return qname from fragmentedFullQName
   */
  private static QName createQName( final String fragmentedFullQName )
  {
    if( "FID".equals( fragmentedFullQName ) ) //$NON-NLS-1$
      return FID_QNAME;

    final String[] parts = fragmentedFullQName.split( "#" ); //$NON-NLS-1$
    if( parts.length == 2 )
      return new QName( parts[0], parts[1] );

    return QName.valueOf( fragmentedFullQName );
  }

  public final static class Source
  {
    private final String property;

    private final DateRange range;

    private final String filter;

    public Source( final String prop, final DateRange dateRange, final String filt )
    {
      this.property = prop;
      this.range = dateRange;
      this.filter = filt;
    }

    public final DateRange getRange( )
    {
      return range;
    }

    public final String getProperty( )
    {
      return property;
    }

    public String getFilter( )
    {
      return filter;
    }
  }

  public static DateRange createDateRangeOrNull( final Date from, final Date to )
  {
    if( from == null || to == null )
      return null;

    return new DateRange( from, to );
  }

  /**
   * @see org.kalypsodeegree_impl.model.feature.visitors.MonitorFeatureVisitor.IMonitoredFeatureVisitor#getTaskName()
   */
  @Override
  public String getTaskName( )
  {
    return "";
  }
}
