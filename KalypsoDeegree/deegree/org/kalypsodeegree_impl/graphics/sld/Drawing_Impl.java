/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 * 
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always. 
 * 
 * If you intend to use this software in other ways than in kalypso 
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree, 
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.graphics.sld;

import java.util.Map;

import org.kalypsodeegree.graphics.sld.CssParameter;
import org.kalypsodeegree.graphics.sld.Drawing;
import org.kalypsodeegree.graphics.sld.GraphicFill;

/**
 * This is the top level interface of <tt>Fill</tt> and <tt>Stroke</tt> defining the methods <tt>getGraphicFill()</tt>
 * and <tt>getCssParameters()</tt> that are common to both.
 * <p>
 * 
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public class Drawing_Impl implements Drawing
{
  private GraphicFill m_graphicFill;

  private final Map<String, CssParameter> m_cssParams;

  /**
   * Constructs a new instance of <tt>Drawing_Impl</tt>.
   */
  Drawing_Impl( final Map<String, CssParameter> cssParams, final GraphicFill graphicFill )
  {
    m_cssParams = cssParams;
    m_graphicFill = graphicFill;
  }

  /**
   * The GraphicFill element both indicates that a stipple-fill repeated graphic will be used and specifies the fill
   * graphic.
   * 
   * @return the GraphicFill-Element
   */
  @Override
  public GraphicFill getGraphicFill( )
  {
    return m_graphicFill;
  }

  @Override
  public void setGraphicFill( final GraphicFill graphicFill )
  {
    m_graphicFill = graphicFill;
  }

  /**
   * A simple SVG/CSS2 styling parameters are given with the CssParameter element.
   * <p>
   * </p>
   * This method is for technical use. The user should access the specialized methods of the derived classes.
   * 
   * @return the CssParameters
   */
  @Override
  public Map<String, CssParameter> getCssParameters( )
  {
    return m_cssParams;
  }

  @Override
  public CssParameter getParameter( final String key )
  {
    return m_cssParams.get( key );
  }

  /**
   * Simple SVG/CSS2 styling parameters are given with the CssParameter element. This method adds a CssParameter to a
   * given set of CssParameters.
   * <p>
   * 
   * @param key
   *          the key of the object to insert
   * @param value
   *          the value of the object to insert
   */
  @Override
  public void addCssParameter( final String key, final CssParameter value )
  {
    m_cssParams.put( key, value );
  }

  /**
   * Simple SVG/CSS2 styling parameters are given with the CssParameter element.
   * <p>
   * This method adds a CssParameter to a given set of CssParameters.
   * 
   * @param key
   *          the key of the object to remove
   */
  @Override
  public void removeCssParameter( final String key )
  {
    m_cssParams.remove( key );
  }

}