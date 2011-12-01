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
package org.kalypso.observation.phenomenon;

import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.deegree.binding.gml.Dictionary;
import org.kalypsodeegree.model.feature.Feature;

/**
 * An {@link IPhenomenon} implementation based on a dictionary entry.
 * 
 * @author Dirk Kuch
 */
public class DictionaryPhenomenon implements IPhenomenon
{
  private final String m_id;

  public DictionaryPhenomenon( final String id/* , final String name, final String description */)
  {
    m_id = id;
  }

  @Override
  public String getDescription( )
  {
    final Feature feature = getFeature();
    if( feature != null )
      return feature.getDescription();

    return "";
  }

  private Feature getFeature( )
  {
    final String[] split = m_id.split( "#" ); //$NON-NLS-1$
    final String dictionaryUrn = split[0];
    final String itemId = split[1];

    final Dictionary dict = KalypsoCorePlugin.getDefault().getDictionary( dictionaryUrn );
    if( dict == null )
      return null;

    return dict.getDefinition( itemId );
  }

  /**
   * @see org.kalypso.observation.IPhenomenon#getID()
   */
  @Override
  public String getID( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.observation.IPhenomenon#getName()
   */
  @Override
  public String getName( )
  {
    final Feature feature = getFeature();
    if( feature != null )
      return feature.getDescription();

    return "";
  }
}
