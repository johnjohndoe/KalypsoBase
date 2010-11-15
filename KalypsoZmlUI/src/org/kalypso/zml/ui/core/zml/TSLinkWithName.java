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
package org.kalypso.zml.ui.core.zml;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.net.URL;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.kalypso.contribs.java.awt.ColorUtilities;
import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * @author belger
 */
public class TSLinkWithName
{
  private final Color m_color;

  private final String m_href;

  private final boolean m_isEditable;

  private final String m_linktype;

  private URL m_context;

  private final String m_name;

  private final boolean m_showLegend;

  private final Stroke m_stroke;

  public TSLinkWithName( final URL scontext, final String sname, final String slinktype, final String shref, final String filter, final String scolor, final String swidth, final String sdashing, final boolean editable, final boolean showInLegend )
  {
    m_context = scontext;
    m_name = sname;
    m_linktype = slinktype;
    m_isEditable = editable;
    m_showLegend = showInLegend;

    if( filter != null && filter.length() > 0 )
      m_href = shref + "?" + filter;
    else
      m_href = shref;

    m_color = scolor == null ? null : ColorUtilities.decodeWithAlpha( scolor );

    final float width = swidth == null ? 1f : (float) NumberUtils.parseQuietDouble( swidth );
    final float[] dash;
    if( sdashing == null )
      dash = null;
    else
    {
      final float dashWidth = (float) NumberUtils.parseQuietDouble( sdashing );
      dash = new float[] { dashWidth, dashWidth };
    }

    m_stroke = new BasicStroke( width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, dash, 1f );
  }

  public Color getColor( )
  {
    return m_color;
  }

  public URL getContext( )
  {
    return m_context;
  }

  public String getHref( )
  {
    return m_href;
  }

  public String getLinktype( )
  {
    return m_linktype;
  }

  public String getName( )
  {
    return m_name;
  }

  public Stroke getStroke( )
  {
    return m_stroke;
  }

  public boolean isEditable( )
  {
    return m_isEditable;
  }

  public boolean isShowLegend( )
  {
    return m_showLegend;
  }

  public void setContext( final URL context )
  {
    m_context = context;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return ToStringBuilder.reflectionToString( this );
  }
}
