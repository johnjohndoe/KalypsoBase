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
package org.kalypso.commons.databinding.conversion;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Gernot Belger
 */
public class AwtToSwtColorConverter extends TypedConverter<java.awt.Color, org.eclipse.swt.graphics.Color>
{
  private final ColorRegistry m_registry;

  private final String m_colorKey;

  public AwtToSwtColorConverter( final ColorRegistry registry, final String colorKey )
  {
    super( java.awt.Color.class, org.eclipse.swt.graphics.Color.class );

    m_registry = registry;
    m_colorKey = colorKey;
  }

  @Override
  public org.eclipse.swt.graphics.Color convertTyped( final java.awt.Color from )
  {
    m_registry.put( m_colorKey, new RGB( from.getRed(), from.getGreen(), from.getBlue() ) );
    return m_registry.get( m_colorKey );
  }
}