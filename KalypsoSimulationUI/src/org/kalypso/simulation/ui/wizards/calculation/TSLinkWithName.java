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
package org.kalypso.simulation.ui.wizards.calculation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * @author belger
 */
public class TSLinkWithName
{
  public final String name;

  public final String linktype;

  public final String href;

  public final Color color;

  public final Stroke stroke;

  public TSLinkWithName( final String sname, final String slinktype, final String shref, final String filter,
      final String scolor, final String swidth, final String sdashing )
  {
    this.name = sname;
    this.linktype = slinktype;

    if( filter != null && filter.length() > 0 )
      this.href = shref + "?" + filter;
    else
      this.href = shref;

    color = scolor == null ? null : Color.decode( scolor );

    final float width = swidth == null ? 1f : (float)NumberUtils.parseQuietDouble( swidth );
    final float[] dash;
    if( sdashing == null )
      dash = null;
    else
    {
      final float dashWidth = (float)NumberUtils.parseQuietDouble( sdashing );
      dash = new float[]
      { dashWidth, dashWidth };
    }

    stroke = new BasicStroke( width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, dash, 1f );
  }
}
