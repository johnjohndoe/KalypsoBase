/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 *
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 *
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 *
 * and
 *
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact:
 *
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 *
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.simulation.core.ant;

import java.net.URL;

import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.merged.Source;
import org.kalypso.simulation.core.ant.copyobservation.CopyObservationFeatureVisitor;
import org.kalypso.simulation.core.ant.copyobservation.ICopyObservationSource;
import org.kalypso.simulation.core.ant.copyobservation.source.FeatureCopyObservationSource;
import org.kalypso.simulation.core.ant.copyobservation.target.CopyObservationTargetFactory;
import org.kalypso.simulation.core.ant.copyobservation.target.ICopyObservationTarget;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.gml.schema.schemata.DeegreeUrlCatalog;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.GMLWorkspace_Impl;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * Helper class to generate a gml that can be used for converting time series. The generated gml includes a list map
 * features. Each map feature has a input-TimeseriesLink and output-TimeseriesLink. The resulting gml can be used as
 * input for the CopyObservationTask. TODO also add a geometry property to the map feature, so that this gml can be also
 * used with a mapview (e.g. in a wizard), where the user can select the time series from the map.
 *
 * @author doemming
 */
public class CopyObservationMappingHelper
{
  /**
   * fragment part of the url denoting a Zml-Url with a context, not a Zml-Id
   *
   * @deprecated Does not work any more, code was removed. Only used by GmlWeightingTask and KrigingTask, which are both
   *             obsolete.
   */
  @Deprecated
  private static final String FRAGMENT_USEASCONTEXT = "useascontext"; //$NON-NLS-1$


  /**
   * @param context
   *          that should be used by the workspace
   * @return GMLWorkspace that represents the mapping
   * @throws Exception
   */
  public static GMLWorkspace createMappingWorkspace( final URL context ) throws GMLSchemaException
  {
    final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
    final GMLSchema schema = schemaCatalog.getSchema( DeegreeUrlCatalog.NS_UPDATE_OBSERVATION_MAPPING, (String) null );
    if( schema == null )
    {
      System.err.println( "Failed to load schema with namespace: " + DeegreeUrlCatalog.NS_UPDATE_OBSERVATION_MAPPING ); //$NON-NLS-1$
      return null;
    }

    final IFeatureType mapColFT = schema.getFeatureType( DeegreeUrlCatalog.QNAME_MAPPING_COLLECTION );
    final Feature rootFE = FeatureFactory.createFeature( null, null, "1", mapColFT, true ); //$NON-NLS-1$
    return new GMLWorkspace_Impl( schema, rootFE, context, null, null, null );
  }

  /**
   * @param workspace
   *          the mapping will be added to the given workspace, usually a workspace created by this class
   * @param filterInline
   *          an inline Filter reference to the ZML-input (also a filter),usually used as source by a
   *          CopyObservationTask
   * @param outHref
   *          reference to the ZML-output, usually used as target by a CopyObservationTask
   * @throws Exception
   */
  public static void addMapping( final GMLWorkspace workspace, final String filterInline, final String outHref ) throws Exception
  {
    final org.kalypso.zml.obslink.ObjectFactory obsLinkFac = new org.kalypso.zml.obslink.ObjectFactory();

    final IFeatureType mapFT = GMLSchemaUtilities.getFeatureTypeQuiet( DeegreeUrlCatalog.QNAME_MAPPING_OBSERVATION );
    final Feature rootFeature = workspace.getRootFeature();

    // in
    final IRelationType pt3 = (IRelationType) rootFeature.getFeatureType().getProperty( DeegreeUrlCatalog.RESULT_LIST_PROP );
    final Feature mapFE = workspace.createFeature( rootFeature, pt3, mapFT );
    final TimeseriesLinkType inLink = obsLinkFac.createTimeseriesLinkType();
    final String finalHref = "#" + FRAGMENT_USEASCONTEXT + "?" + filterInline; //$NON-NLS-1$ //$NON-NLS-2$
    inLink.setHref( finalHref );
    final IPropertyType inLinkPT = mapFT.getProperty( DeegreeUrlCatalog.RESULT_TS_IN_PROP );
    mapFE.setProperty( inLinkPT, inLink );

    // out
    final TimeseriesLinkType outLink = obsLinkFac.createTimeseriesLinkType();
    outLink.setHref( outHref );
    final IPropertyType pt2 = mapFT.getProperty( DeegreeUrlCatalog.RESULT_TS_OUT_PROP );
    mapFE.setProperty( pt2, outLink );
    workspace.addFeatureAsComposition( rootFeature, pt3, 0, mapFE );
  }

  /**
   * this mapping updates only the measured time period, the forecast period will be taken from the target before
   * overwriting it. So only measured period will update.
   */
  public static void runMapping( final GMLWorkspace workspace, final URL srcContext, final ILogger logger, final boolean keepForecast, final DateRange measuredRange, final DateRange doNotOverwriteRange, final DateRange forecastRange )
  {
    final Source[] sources;
    if( keepForecast )
    {
      /*
       * Note: the order is important for the ForecastFilter! so we put the target-observation in the first place since
       * it is the first element that will be backed by the forecast-filter forecast and measured
       */
      sources = new Source[] { new Source( null, DeegreeUrlCatalog.RESULT_TS_OUT_PROP.getLocalPart(), doNotOverwriteRange, null ),
          new Source( null, DeegreeUrlCatalog.RESULT_TS_IN_PROP.getLocalPart(), measuredRange, null ) };
    }
    else
    {
      // measured
      sources = new Source[] { new Source( null, DeegreeUrlCatalog.RESULT_TS_IN_PROP.getLocalPart(), measuredRange, null ), };
    }

    /*
     * REMARK: forecastFrom and forecastTo where formerly not set which resulted in strange behavior: run from the
     * runtime workspace, the forecast range was set, from the deployed application it was not, however both used
     * exactly the same plug-ins. Setting it here succeeded however.
     */
    final DateRange completeRange = new DateRange( measuredRange.getFrom(), doNotOverwriteRange.getTo() );

    final GMLXPath targetPath = new GMLXPath( DeegreeUrlCatalog.RESULT_TS_OUT_PROP );
    final ICopyObservationTarget timeSeriesLink = CopyObservationTargetFactory.getLink( srcContext, targetPath, null, completeRange, forecastRange );
    final ICopyObservationSource source = new FeatureCopyObservationSource( srcContext, sources, null );

    final CopyObservationFeatureVisitor visitor = new CopyObservationFeatureVisitor( source, timeSeriesLink, new MetadataList(), logger );
    workspace.accept( visitor, DeegreeUrlCatalog.RESULT_LIST_PROP.getLocalPart(), 1 );
  }
}
