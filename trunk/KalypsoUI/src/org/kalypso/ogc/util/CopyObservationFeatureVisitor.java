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
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITuppleModel;
import org.kalypso.ogc.sensor.MetadataList;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.FilterFactory;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.request.RequestFactory;
import org.kalypso.ogc.sensor.template.ObsViewUtils;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.ogc.sensor.timeseries.forecast.ForecastTuppleModel;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ogc.sensor.zml.ZmlURL;
import org.kalypso.zml.obslink.ObjectFactory;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypso.zml.request.Request;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author belger
 */
public class CopyObservationFeatureVisitor implements FeatureVisitor
{
  /** Used to search/replace metadata content with properties of the visited feature */
  private static Pattern PATTERN_FEATURE_PROPERTY = Pattern.compile( "\\Q${property;\\E([^;]*)\\Q;\\E([^}]*)\\Q}\\E" ); //$NON-NLS-1$

  private static final ObjectFactory OF = new ObjectFactory();

  private static final QName FID_QNAME = new QName( "FID" ); //$NON-NLS-1$

  private final Source[] m_sources;

  private final URL m_context;

  private final String m_targetobservation;

  private final IUrlResolver m_urlResolver;

  private final ILogger m_logger;

  private final DateRange m_forecastRange;

  private final DateRange m_targetRange;

  private final Properties m_metadata;

  /**
   * Die Liste der Tokens und deren Ersetzung in der Form:
   * <p>
   * tokenName-featurePropertyName;tokenName-featurePropertyName;...
   * <p>
   * Die werden benutzt um token-replace im Zml-Href durchzuführen (z.B. um automatisch der Name der Feature als
   * Request-Name zu setzen)
   */
  private final String m_tokens;

  /** TODO: Only used by KalypsoNA */
  private final File m_targetobservationDir;

  /**
   * @param context
   *          context to resolve relative url
   * @param urlResolver
   *          resolver for urls
   * @param metadata
   *          All entries will be added to the target observation
   */
  public CopyObservationFeatureVisitor( final URL context, final IUrlResolver urlResolver, final String targetobservation, final File targetobservationDir, final Source[] sources, final Properties metadata, final DateRange targetRange, final DateRange forecastRange, final ILogger logger, final String tokens )
  {
    m_context = context;
    m_urlResolver = urlResolver;
    m_targetobservationDir = targetobservationDir;
    m_targetobservation = targetobservation;
    m_sources = sources;
    m_metadata = metadata;
    m_forecastRange = forecastRange;
    m_targetRange = targetRange;
    m_logger = logger;
    m_tokens = tokens;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureVisitor#visit(org.kalypsodeegree.model.feature.Feature)
   */
  public boolean visit( final Feature f )
  {
    try
    {
      final IObservation[] sourceObses = getObservations( f );
      final DateRange[] sourceRanges = getSourceRanges();

      final IFile targetfile = createTargetFile( f );
      if( targetfile == null )
        return true;

      final IObservation resultObs = combineResultObservation( sourceObses, sourceRanges );

      setForecastAndAddMetadata( resultObs, f );

      final IRequest request = new ObservationRequest( m_targetRange );
// // FIXME: this causes two calls to the repository and eventually two calls to the underlying database
// KalypsoProtocolWriter.analyseValues( resultObs, resultObs.getValues( request ), m_logger );

      wrtieTargetObservation( targetfile, resultObs, request );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      m_logger.log( Level.WARNING, LoggerUtilities.CODE_SHOW_DETAILS, Messages.getString( "org.kalypso.ogc.util.CopyObservationFeatureVisitor.3" ) + f.getId() + "\t" + e.getLocalizedMessage() );//$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
    }

    return true;
  }

  private DateRange[] getSourceRanges( )
  {
    final DateRange[] ranges = new DateRange[m_sources.length];

    for( int i = 0; i < m_sources.length; i++ )
      ranges[i] = m_sources[i].getRange();

    return ranges;
  }

  private IFile createTargetFile( final Feature f ) throws MalformedURLException
  {
    final TimeseriesLinkType targetlink = getTargetLink( f );
    if( targetlink == null )
    {
      m_logger.log( Level.WARNING, LoggerUtilities.CODE_SHOW_MSGBOX, Messages.getString( "org.kalypso.ogc.util.CopyObservationFeatureVisitor.1" ) + f.getId() );//$NON-NLS-1$
      return null;
    }
    // remove query part if present, href is also used as file name here!
    final String href = ZmlURL.getIdentifierPart( targetlink.getHref() );
    return ResourceUtilities.findFileFromURL( m_urlResolver.resolveURL( m_context, href ) );
  }

  private void wrtieTargetObservation( final IFile targetfile, final IObservation resultObs, final IRequest request ) throws SensorException
  {
    final IPath location = targetfile.getLocation();
    final File file = location.toFile();
    file.getParentFile().mkdirs();
    ZmlFactory.writeToFile( resultObs, file, request );
  }

  private void setForecastAndAddMetadata( final IObservation resultObs, final Feature feature )
  {
    // set forecast metadata, might be used in diagram for instance
    // to mark the forecast range
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

  private IObservation combineResultObservation( final IObservation[] sourceObses, final DateRange[] sourceRanges ) throws SensorException
  {
    // only do ForeCastFilter if we have more than one obs
    if( sourceObses.length < 2 || sourceObses[1] == null )
      return sourceObses[0];

    final ITuppleModel[] models = new ITuppleModel[sourceObses.length];
    for( int i = 0; i < sourceObses.length; i++ )
      models[i] = sourceObses[i].getValues( new ObservationRequest( sourceRanges[i] ) );

    final ForecastTuppleModel tuppleModel = new ForecastTuppleModel( models );

    final MetadataList metadataList = (MetadataList) sourceObses[0].getMetadataList().clone();
    final IAxis[] axes = sourceObses[0].getAxisList();
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

  private TimeseriesLinkType getTargetLink( final Feature f )
  {
    if( m_targetobservationDir != null )
    {
      // FIXME: this dirty shit was made only for KalypsoNA: must be removed!!!
      String name = (String) f.getProperty( "name" ); //$NON-NLS-1$
      if( name == null || name.length() < 1 )
        name = f.getId();
      if( name == null || name.length() < 1 )
        name = "generated"; //$NON-NLS-1$
      final File file = getValidFile( name, 0 );
      final TimeseriesLinkType link = OF.createTimeseriesLinkType();
      final IFile contextIFile = ResourceUtilities.findFileFromURL( m_context );
      final File contextFile = contextIFile.getLocation().toFile();
      final String relativePathTo = FileUtilities.getRelativePathTo( contextFile, file );
      link.setHref( relativePathTo );
      return link;
    }

    return (TimeseriesLinkType) f.getProperty( m_targetobservation );
  }

  private File getValidFile( final String name, int index )
  {
    String newName = name;
    if( index > 0 )
      newName = newName + "_" + Integer.toString( index ); //$NON-NLS-1$
    final String newName2 = FileUtilities.validateName( newName, "_" ); //$NON-NLS-1$
    final File file = new File( m_targetobservationDir, newName2 + ".zml" ); //$NON-NLS-1$
    if( file.exists() )
    {
      index++;
      return getValidFile( name, index );
    }
    return file;
  }

  private IObservation[] getObservations( final Feature f ) throws SensorException
  {
    final List<IObservation> result = new ArrayList<IObservation>();
    for( final Source source : m_sources )
    {
      try
      {
        result.add( getObservation( f, source.getProperty(), source.getRange(), source.getFilter() ) );
      }
      catch( final Exception e )
      {
        // it is possible to use the target also as input, e.g. if you want to update just a part of the zml.
        // if this source==target is unreachable it should be ignored, if it is not the target throw an exception
        if( m_targetobservation.equals( source.getProperty() ) )
          m_logger.log( Level.WARNING, LoggerUtilities.CODE_NONE, Messages.getString( "org.kalypso.ogc.util.CopyObservationFeatureVisitor.9" ) );//$NON-NLS-1$
        else
          throw new SensorException( e );
      }
    }

    return result.toArray( new IObservation[result.size()] );
  }

  private IObservation getObservation( final Feature feature, final String sourceProperty, final DateRange range, final String filter ) throws MalformedURLException, SensorException
  {
    if( sourceProperty == null )
      return null;

    final TimeseriesLinkType sourcelink = (TimeseriesLinkType) feature.getProperty( sourceProperty );
    if( sourcelink == null )
      return null;

    final String sourceHref = sourcelink.getHref();
    final String hrefWithFilter = ZmlURL.insertQueryPart( sourceHref, filter );

    // filter variable might also contain request spec
    String hrefWithFilterAndRange = ZmlURL.insertRequest( hrefWithFilter, new ObservationRequest( range ) );

    // token replacement
    if( m_tokens != null && m_tokens.length() > 0 )
    {
      final Properties properties = FeatureHelper.createReplaceTokens( feature, m_tokens );

      hrefWithFilterAndRange = ObsViewUtils.replaceTokens( hrefWithFilterAndRange, properties );
    }

    final URL sourceURL = new UrlResolver().resolveURL( m_context, hrefWithFilterAndRange );

    try
    {
      return ZmlFactory.parseXML( sourceURL, feature.getId() );
    }
    catch( final SensorException e )
    {
      final Request requestType = RequestFactory.parseRequest( hrefWithFilterAndRange );
      if( requestType == null )
        throw new SensorException( Messages.getString( "org.kalypso.ogc.util.CopyObservationFeatureVisitor.10" ) + hrefWithFilterAndRange, e );//$NON-NLS-1$

      // obs could not be created, use the request now
      final String message = String.format( "Abruf von '%s' fehlgeschlagen. Erzeuge syntetische Zeitreihe.", sourceHref );
      m_logger.log( Level.WARNING, -1, message ); //$NON-NLS-1$
      final IObservation synteticObservation = RequestFactory.createDefaultObservation( requestType );
      return FilterFactory.createFilterFrom( filter, synteticObservation, null );
    }
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

}
