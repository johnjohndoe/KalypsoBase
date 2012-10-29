/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.chart.ext.observation;

import java.net.URL;

import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class GmlWorkspaceObservationProvider implements IObservationProvider
{
  final private String m_observationId;

  final private String m_href;

  final private URL m_context;

  private GMLWorkspace m_workspace;

  /**
   * @param href
   *          Location of a gml resource (.gml file)
   * @param observationId
   *          Feature id in gml workspace to an observation feature.
   */
  public GmlWorkspaceObservationProvider( final URL context, final String href, final String observationId )
  {
    m_context = context;
    m_href = href;
    m_observationId = observationId;
  }

  @Override
  public void dispose( )
  {
    if( m_workspace != null )
    {
      m_workspace.dispose();
      m_workspace = null;
    }
  }

  @Override
  public IObservation<TupleResult> getObservation( )
  {
    try
    {
      m_workspace = GmlSerializer.createGMLWorkspace( new URL( m_context, m_href ), null );
      final Feature feature = m_workspace.getFeature( m_observationId );
      if( feature != null )
      {
        // FIXME: makes no sense.... however we should give some information if the
        // feature was not found... -> exception?
        // Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Found feature: " + feature.getId() );
        return ObservationFeatureFactory.toObservation( feature );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return null;
  }
}