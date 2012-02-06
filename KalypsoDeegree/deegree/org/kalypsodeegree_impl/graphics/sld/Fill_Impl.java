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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.kalypso.commons.KalypsoCommonsPlugin;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.CssParameter;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.GraphicFill;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.Marshallable;

/**
 * A Fill allows area geometries to be filled. There are two types of fills: solid-color and repeated GraphicFill. In
 * general, if a Fill element is omitted in its containing element, no fill will be rendered. The default is a solid
 * 50%-gray (color "#808080") opaque fill.
 * <p>
 * 
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public class Fill_Impl extends Drawing_Impl implements Fill, Marshallable
{
  /**
   * Constructs a new <tt>Fill_Impl</tt>.
   */
  protected Fill_Impl()
  {
    super( new HashMap<String, CssParameter>(), null );
  }

  /**
   * Constructs a new <tt>Fill_Impl</tt>.
   */
  public Fill_Impl( final Map<String, CssParameter> params, final GraphicFill gaphicFill )
  {
    super( params, gaphicFill );
  }

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
  @Override
  public Color getFill( final Feature feature ) throws FilterEvaluationException
  {
    final CssParameter cssParam = getParameter( "fill" );

    if( cssParam == null )
      return FILL_DEFAULT;

    String s = cssParam.getValue( feature );
    if( s.length() < 2 )
      return FILL_DEFAULT;

    try
    {
      if( s.charAt( 0 ) == '#' && s.charAt( 1 ) == '#' )
        s = s.substring( 1 );

      return Color.decode( s );
    }
    catch( final Exception e )
    {
      KalypsoCommonsPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e, "Given value ('" + s + "') for CSS-Parameter 'fill' " + "does not denote a valid color!" ) );
      return FILL_DEFAULT;
    }
  }


  /**
   * sets the value of the fill's CssParameter 'fill' as a simple color
   * 
   * @param color
   *          color to be set
   */
  @Override
  public void setFill( final Color color )
  {

    final String s = StyleFactory.getColorAsHex( color );
    final CssParameter fill = StyleFactory.createCssParameter( "fill", s );

    addCssParameter( "fill", fill );
  }

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
  @Override
  public double getOpacity( final Feature feature ) throws FilterEvaluationException
  {
    double opacity = OPACITY_DEFAULT;

    final CssParameter cssParam = getParameter( "fill-opacity" );

    if( cssParam != null )
    {
      final String value = cssParam.getValue( feature );

      try
      {
        opacity = Double.parseDouble( value );
      }
      catch( final NumberFormatException e )
      {
        throw new FilterEvaluationException( "Given value for parameter 'fill-opacity' ('" + value
            + "') has invalid format!" );
      }

      if( ( opacity < 0.0 ) || ( opacity > 1.0 ) )
      {
        throw new FilterEvaluationException( "Value for parameter 'fill-opacity' (given: '" + value
            + "') must be between 0.0 and 1.0!" );
      }
    }

    return opacity;
  }

  /**
   * sets the value of the opacity's CssParameter 'opacity' as a value. Valid values ranges from 0 .. 1. If a value < 0
   * is passed it will be set to 0. If a value > 1 is passed it will be set to 1.
   * 
   * @param opacity
   *          opacity to be set
   */
  @Override
  public void setOpacity( double opacity )
  {
    if( opacity > 1 )
    {
      opacity = 1;
    }
    else if( opacity < 0 )
    {
      opacity = 0;
    }

    final CssParameter fillOp = StyleFactory.createCssParameter( "fill-opacity", "" + opacity );
    addCssParameter( "fill-opacity", fillOp );
  }

  /**
   * exports the content of the CssParameter as XML formated String
   * 
   * @return xml representation of the CssParameter
   */
  @Override
  public String exportAsXML()
  {
    final StringBuffer sb = new StringBuffer( "<Fill>" );

    final GraphicFill graphicFill = getGraphicFill();
    if( graphicFill != null )
      sb.append( graphicFill.exportAsXML() );

    final Map<String, CssParameter> cssParameters = getCssParameters();
    for( final CssParameter param : cssParameters.values() )
      sb.append( param.exportAsXML() );

    sb.append( "</Fill>" );

    return sb.toString();
  }

}