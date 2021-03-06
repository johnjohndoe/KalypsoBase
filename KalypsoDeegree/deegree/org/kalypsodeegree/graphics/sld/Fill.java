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
package org.kalypsodeegree.graphics.sld;

import java.awt.Color;

import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.model.feature.Feature;

/**
 * A Fill allows area geometries to be filled. There are two types of fills: solid-color and repeated GraphicFill. In
 * general, if a Fill element is omitted in its containing element, no fill will be rendered. The default is a solid
 * 50%-gray (color "#808080") opaque fill.
 * <p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public interface Fill extends Drawing
{
  String CSS_FILL = "fill"; //$NON-NLS-1$

  String CSS_OPACITY = "fill-opacity"; //$NON-NLS-1$

  // default values
  Color FILL_DEFAULT = Color.decode( "#808080" );

  double OPACITY_DEFAULT = 1.0;

  /**
   * Returns the (evaluated) value of the fill's CssParameter 'fill'.
   * <p>
   * 
   * @param feature
   *          specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
   * @return the (evaluated) value of the parameter
   * @throws FilterEvaluationException
   *           if the evaluation fails or the value is invalid
   */
  Color getFill( Feature feature ) throws FilterEvaluationException;

  /**
   * sets the value of the fill's CssParameter 'fill' as a simple color
   * 
   * @param color
   *          color to be set
   */
  void setFill( Color color );

  /**
   * Returns the (evaluated) value of the fill's CssParameter 'fill-opacity'.
   * <p>
   * 
   * @param feature
   *          specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
   * @return the (evaluated) value of the parameter
   * @throws FilterEvaluationException
   *           if the evaluation fails or the value is invalid
   */
  double getOpacity( Feature feature ) throws FilterEvaluationException;

  /**
   * sets the value of the opacity's CssParameter 'opacity' as a value. Valid values ranges from 0 .. 1. If a value < 0
   * is passed it will be set too 0. If a value > 1 is passed it will be set too 1.
   * 
   * @param opacity
   *          opacity to be set
   */
  void setOpacity( double opacity );
}