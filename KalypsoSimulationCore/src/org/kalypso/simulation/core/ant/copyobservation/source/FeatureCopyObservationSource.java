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
package org.kalypso.simulation.core.ant.copyobservation.source;

import java.net.URL;
import java.util.Properties;

import org.kalypso.ogc.sensor.timeseries.merged.Source;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Dirk Kuch
 */
public class FeatureCopyObservationSource extends AbstractCopyObservationSource
{
  private final String m_tokens;

  public FeatureCopyObservationSource( final URL context, final Source[] sources, final String tokens )
  {
    super( context, sources );
    m_tokens = tokens;
  }

  /**
   * @see org.kalypso.ogc.util.copyobservation.source.AbstractCopyObservationSource#getReplaceTokens(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  protected final Properties getReplaceTokens( final Feature feature )
  {
    if( m_tokens != null && !m_tokens.isEmpty() )
      return FeatureHelper.createReplaceTokens( feature, m_tokens );

    return null;
  }

  /**
   * @see org.kalypso.ogc.util.copyobservation.source.AbstractCopyObservationSource#getSourceLinkHref()
   */
  @Override
  protected final String getSourceLinkHref( final Feature feature, final Source source )
  {
    if( source.getProperty() == null )
      return null;

    final TimeseriesLinkType sourcelink = (TimeseriesLinkType) feature.getProperty( source.getProperty() );
    if( sourcelink == null )
      return null;

    return sourcelink.getHref();

  }

}
