/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.simulation.core.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;
import org.kalypso.contribs.java.xml.XMLUtilities;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.FilterFactory;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.zml.filters.AbstractFilterType;
import org.kalypso.zml.filters.InterpolationFilterType;
import org.kalypso.zml.filters.NOperationFilterType;
import org.kalypso.zml.filters.OperationFilterType;
import org.kalypso.zml.filters.ZmlFilterType;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;
import org.w3._1999.xlinkext.SimpleLinkType;
import org.xml.sax.InputSource;

/**
 * @author Gernot Belger
 */
public class GMLWeightingOperation
{
  private final org.w3._1999.xlinkext.ObjectFactory m_linkFac = new org.w3._1999.xlinkext.ObjectFactory();

  private final IGMLWeightingData m_data;

  private final ILogger m_logger;

  private NamespaceContext m_namespaceContext;

  public GMLWeightingOperation( final IGMLWeightingData data, final ILogger logger )
  {
    m_data = data;
    m_logger = logger;
  }

  public void execute( ) throws Exception
  {
    final IUrlResolver urlResolver = UrlResolverSingleton.getDefault();
    // create needed factories
    final Marshaller marshaller = JaxbUtilities.createMarshaller( ZmlFactory.JC, true );

    // workspace for results
    final URL modelURL = m_data.getModelLocation();
    final GMLWorkspace resultWorkspace = CopyObservationMappingHelper.createMappingWorkspace( modelURL );

    // 1. load srcgml
    m_logger.log( Level.INFO, -1, "Lade Modell " + modelURL );
    final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( modelURL, null );
    m_namespaceContext = workspace.getNamespaceContext();

    // 2. locate features to process
    final GMLXPath targetFeaturePath = asPath( m_data.getTargetFeaturePath() );
    final Feature[] targetFeatures = resolveFeatures( workspace.getRootFeature(), targetFeaturePath );
    if( targetFeatures == null )
      throw new BuildException( "Kein(e) Ziel-Feature(s) gefunden für FeaturePath: " + targetFeaturePath );

    final GMLXPath targetZMLPath = asPath( m_data.getTargetZMLProperty() );
    final URL targetContext = m_data.getTargetContext();

    final GMLXPath sourceMember = asPath( m_data.getSourceMember() );

    final GMLXPath weightPath = asPath( m_data.getWeightProperty() );
    final GMLXPath offsetPath = asPath( m_data.getOffsetProperty() );

    // loop all features
    for( final Feature targetFE : targetFeatures )
    {
      // 3. find target
      final TimeseriesLinkType targetLink = GMLXPathUtilities.query( targetZMLPath, targetFE );
      final String targetHref = targetLink.getHref();
      final URL targetURL = urlResolver.resolveURL( targetContext, targetHref );

      // 4. build n-operation filter
      final NOperationFilterType nOperationFilter = FilterFactory.OF_FILTER.createNOperationFilterType();
      nOperationFilter.setOperator( "+" ); //$NON-NLS-1$
      final List<JAXBElement< ? extends AbstractFilterType>> filterList = nOperationFilter.getFilter();

      // 5. resolve weights

      final GMLXPath weightMember = asPath( m_data.getWeightMember() );
      final Feature[] weightFEs = resolveFeatures( targetFE, weightMember );
      if( weightFEs == null )
        throw new BuildException( "Kein(e) Gewichts-Feature(s) gefunden für FeaturePath: " + weightMember );

      // 6. loop weights
      for( final Feature weightFE : weightFEs )
      {
        final double factor = getFactor( weightFE, weightPath );
        final double offset = getOffset( weightFE, offsetPath );

        // 7. resolve sources
        final Feature[] sourceFeatures = resolveFeatures( weightFE, sourceMember );
        if( sourceFeatures == null )
          throw new BuildException( "Kein(e) Quell-Feature(s) gefunden für FeaturePath: " + sourceMember );

        final OperationFilterType offsetFilter = FilterFactory.OF_FILTER.createOperationFilterType();
        offsetFilter.setOperator( "+" ); //$NON-NLS-1$
        offsetFilter.setOperand( Double.toString( offset ) );

        final NOperationFilterType weightSumFilter = FilterFactory.OF_FILTER.createNOperationFilterType();
        weightSumFilter.setOperator( "+" ); //$NON-NLS-1$

        offsetFilter.setFilter( FilterFactory.OF_FILTER.createNOperationFilter( weightSumFilter ) );

        final List<JAXBElement< ? extends AbstractFilterType>> offsetSummands = weightSumFilter.getFilter();

        addSourceSummands( weightFE, factor, sourceFeatures, offsetSummands );
        /* Empty NOperation filter is forbidden */
        // Bad warning message, can happen if all sub-elements are disabled
        if( offsetSummands.isEmpty() )
        {
          // m_logger.log( Level.WARNING, LoggerUtilities.CODE_SHOW_DETAILS, "Leere Summe für Feature: " +
          // weightFE.getId() );
        }
        else
          filterList.add( FilterFactory.OF_FILTER.createOperationFilter( offsetFilter ) );
      }

      /* Empty NOperation filter is forbidden */
      if( filterList.isEmpty() )
      {
        m_logger.log( Level.SEVERE, LoggerUtilities.CODE_SHOW_MSGBOX, "Leere Summe für Feature: " + targetFE.getId() );
        return;
      }

      // 11. serialize filter to string
      final Writer writer = new StringWriter();
      marshaller.marshal( FilterFactory.OF_FILTER.createNOperationFilter( nOperationFilter ), writer );
      writer.close();
      final String string = XMLUtilities.removeXMLHeader( writer.toString() );
      final String filterInline = XMLUtilities.prepareInLine( string );

      // 12. add mapping to result workspace
      CopyObservationMappingHelper.addMapping( resultWorkspace, filterInline, targetURL.toExternalForm() );
      m_logger.log( Level.INFO, -1, "Ziel-ZML " + targetURL );
    }

    // 14. do the mapping
    final Date sourceFrom = m_data.getSourceFrom();
    final Date sourceTo = m_data.getSourceTo();
    final Date targetFrom = m_data.getTargetFrom();
    final Date targetTo = m_data.getTargetTo();
    final Date forecastFrom = m_data.getForecastFrom();
    final Date forecastTo = m_data.getForecastTo();

    final DateRange measuredRange = DateRange.createDateRangeOrNull( sourceFrom, sourceTo );
    final DateRange keForecastRange = DateRange.createDateRangeOrNull( targetFrom, targetTo );
    final DateRange forecastMetadataRange = DateRange.createDateRangeOrNull( forecastFrom, forecastTo );

    CopyObservationMappingHelper.runMapping( resultWorkspace, modelURL, m_logger, true, measuredRange, keForecastRange, forecastMetadataRange );

    // 15. serialize result workspace to file
    final File targetMapping = m_data.getTargetMapping();
    if( targetMapping != null )
    {
      FileWriter writer = null;
      try
      {
        writer = new FileWriter( targetMapping );
        GmlSerializer.serializeWorkspace( writer, resultWorkspace );
        writer.close();
      }
      finally
      {
        IOUtils.closeQuietly( writer );
      }
    }
  }

  private void addSourceSummands( final Feature weightFE, final double factor, final Feature[] sourceFeatures, final List<JAXBElement< ? extends AbstractFilterType>> offsetSummands ) throws GMLXPathException, SensorException, JAXBException
  {
    final GMLXPath sourceZMLPath = asPath( m_data.getSourceZMLProperty() );
    final GMLXPath sourceIsUsedPath = asPath( m_data.getSourceIsUsedProperty() );
    final String sourceFilter = m_data.getSourceFilter();

    // 8. loop source features
    for( final Feature sourceFE : sourceFeatures )
    {
      if( sourceFE == null )
      {
        m_logger.log( Level.WARNING, -1, "Linked source feature missing in Feature: " + weightFE.getId() );

        // IMPORTANT: just skips this weight; leads probably to wrong results
        continue;
      }

      // 9. resolve property that is source zml reference
      final TimeseriesLinkType zmlLink = GMLXPathUtilities.query( sourceZMLPath, sourceFE );
      final Boolean useThisSource;
      if( sourceIsUsedPath != null )
        useThisSource = GMLXPathUtilities.query( sourceIsUsedPath, sourceFE );
      else
        useThisSource = Boolean.TRUE;

      if( !useThisSource.booleanValue() )
      {
        m_logger.log( Level.INFO, LoggerUtilities.CODE_NONE, "Ignoriere: " + sourceFE.getId() );
        continue;
      }

      if( zmlLink == null )
      {
        m_logger.log( Level.WARNING, LoggerUtilities.CODE_SHOW_DETAILS, "Linked timeserie link missing in Feature: " + weightFE.getId() ); //$NON-NLS-1$

        // IMPORTANT: just skips this weight; leads probably to wrong results
        continue;
      }

      // 10. build operation filter with parameters from gml
      final OperationFilterType filter = FilterFactory.OF_FILTER.createOperationFilterType();
      offsetSummands.add( FilterFactory.OF_FILTER.createOperationFilter( filter ) );
      filter.setOperator( "*" ); //$NON-NLS-1$
      filter.setOperand( Double.toString( factor ) );

      /* Innermost filter part */
      final ZmlFilterType zmlFilter = FilterFactory.OF_FILTER.createZmlFilterType();
      final SimpleLinkType simpleLink = m_linkFac.createSimpleLinkType();
      final String sourceHref = zmlLink.getHref();
      simpleLink.setHref( sourceHref );
      zmlFilter.setZml( simpleLink );

      if( sourceFilter != null )
      {
        final String strFilterXml = FilterFactory.getFilterPart( sourceFilter );

        final StringReader sr = new StringReader( strFilterXml );
        final Unmarshaller unmarshaller = ZmlFactory.JC.createUnmarshaller();
        final JAXBElement< ? > filterElement = (JAXBElement< ? >)unmarshaller.unmarshal( new InputSource( sr ) );
        if( filterElement == null || !AbstractFilterType.class.isAssignableFrom( filterElement.getDeclaredType() ) )
          throw new UnsupportedOperationException( "Filter must start with an AbstractFilterType element." ); //$NON-NLS-1$

        @SuppressWarnings( "unchecked" ) final JAXBElement<AbstractFilterType> af = (JAXBElement<AbstractFilterType>)filterElement;
        filter.setFilter( af );

        // HACK
        final AbstractFilterType abstractFilter = af.getValue();
        if( abstractFilter instanceof InterpolationFilterType )
          ((InterpolationFilterType)abstractFilter).setFilter( FilterFactory.OF_FILTER.createZmlFilter( zmlFilter ) );
        else
          throw new UnsupportedOperationException( "Only InterpolationFilter as source-filter supported at the moment." ); //$NON-NLS-1$

        sr.close();
      }
      else
        filter.setFilter( FilterFactory.OF_FILTER.createZmlFilter( zmlFilter ) );
    }
  }

  private GMLXPath asPath( final String property )
  {
    if( StringUtils.isBlank( property ) )
      return null;

    return new GMLXPath( property, m_namespaceContext );
  }

  private Feature[] resolveFeatures( final Feature feature, final GMLXPath featurePath ) throws GMLXPathException
  {
    if( featurePath == null )
      return new Feature[] { feature };

    final Object property = GMLXPathUtilities.query( featurePath, feature );
    if( property instanceof FeatureList )
      return ((FeatureList)property).toFeatures();

    if( property instanceof Feature )
      return new Feature[] { (Feature)property };

    if( property instanceof String )
    {
      final Feature resolvedFeature = feature.getWorkspace().getFeature( (String)property );
      return new Feature[] { resolvedFeature };
    }

    return null;
  }

  private double getOffset( final Feature weightFE, final GMLXPath offsetPath ) throws GMLXPathException
  {
    if( offsetPath == null )
      return 0.0;

    return resolveValueReference( weightFE, offsetPath, 0.0 );
  }

  private double getFactor( final Feature weightFE, final GMLXPath weightPath ) throws GMLXPathException
  {
    if( weightPath == null )
      return 1.0;

    return resolveValueReference( weightFE, weightPath, 1.0 );
  }

  private double resolveValueReference( final Feature feature, final GMLXPath path, final double defaultValue ) throws GMLXPathException
  {
    final Object valueOrReference = GMLXPathUtilities.query( path, feature );
    if( valueOrReference instanceof Number )
      return ((Number)valueOrReference).doubleValue();

    if( valueOrReference == null )
      return defaultValue;

    final String valuePath = valueOrReference.toString();
    if( StringUtils.isBlank( valuePath ) )
      return defaultValue;

    final GMLXPath valueXpath = new GMLXPath( valuePath, m_namespaceContext );

    return resolveValueReference( feature, valueXpath, defaultValue );
  }
}