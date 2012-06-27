/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package de.openali.odysseus.chart.framework.util.resource;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;

/**
 * Registry to store SWT Patterns created from images. The caller does not need to dispose the patterns.
 * 
 * @author burtscher1
 */
public class PatternRegistry extends AbstractResourceRegistryFactory<ImageDescriptor, Pair<Image, Pattern>>
{
// private final Map<ImageDescriptor, IPair<Image, Pattern>> m_patternMap = new HashMap<ImageDescriptor, IPair<Image,
  // Pattern>>();
//
// public Pattern getPattern( Device dev, ImageDescriptor id )
// {
// IPair<Image, Pattern> pair = m_patternMap.get( id );
// if( pair == null )
// {
// Image img = id.createImage( dev );
// Pattern p = new Pattern( dev, img );
// pair = new Pair<Image, Pattern>( img, p );
// m_patternMap.put( id, pair );
// }
// return pair.getTarget();
// }
//
// @Override
// public void dispose( )
// {
// for( IPair<Image, Pattern> pair : m_patternMap.values() )
// {
// pair.getDomain().dispose();
// pair.getTarget().dispose();
// }
// m_patternMap.clear();
//
// }

  /**
   * @see de.openali.odysseus.chart.framework.util.resource.AbstractResourceRegistryFactory#createResource(org.eclipse.swt.graphics.Device,
   *      java.lang.Object)
   */
  @Override
  protected Pair<Image, Pattern> createResource( final Device dev, final ImageDescriptor descriptor )
  {
    final Image img = descriptor.createImage( dev );
    final Pattern p = new Pattern( dev, img );
    final Pair<Image, Pattern> pair = new Pair<Image, Pattern>( img, p );
    return pair;
  }

  /**
   * @see de.openali.odysseus.chart.framework.util.resource.AbstractResourceRegistryFactory#disposeResource(java.lang.Object)
   */
  @Override
  protected void disposeResource( final Pair<Image, Pattern> resource )
  {
    resource.getDomain().dispose();
    resource.getTarget().dispose();
  }

}