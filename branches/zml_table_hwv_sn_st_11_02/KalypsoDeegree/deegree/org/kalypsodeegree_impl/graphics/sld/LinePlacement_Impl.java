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

import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.LinePlacement;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree_impl.tools.Debug;

/**
 * Incarnation of an sld:LinePlacement-element.
 * <p>
 * Contains some deegree-specific extensions:
 * <ul>
 * <li>PerpendicularOffset: may be used as defined by the OGC, but it can also be set to one of the special values
 * 'center', 'above', 'below', 'auto'
 * <li>Gap: defines the distance between two captions on the line string
 * <li>LineWidth: provides the thickness of the styled line (needed as information for the correct positioning of
 * labels above and below the line string)
 * </ul>
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public class LinePlacement_Impl implements LinePlacement, Marshallable
{
  private ParameterValueType m_perpendicularOffset = null;

  private ParameterValueType m_lineWidth = null;

  private ParameterValueType m_gap = null;

  public LinePlacement_Impl( final ParameterValueType perpendicularOffset, final ParameterValueType lineWidth,
      final ParameterValueType gap )
  {
    this.m_perpendicularOffset = perpendicularOffset;
    this.m_lineWidth = lineWidth;
    this.m_gap = gap;
  }

  /**
   * The PerpendicularOffset element of a LinePlacement gives the perpendicular distance away from a line to draw a
   * label. The distance is in pixels and is positive to the left-hand side of the line string. Negative numbers mean
   * right. The default offset is 0.
   * <p>
   * deegree-specific extension: if the element has one of the values: 'center', 'above', 'below', 'auto', the return
   * value is invalid
   * <p>
   * 
   * @param feature
   *          specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
   * @return the offset (only valid if type is TYPE_ABSOLUTE)
   * @throws FilterEvaluationException
   *           if the evaluation fails
   */
  @Override
  public double getPerpendicularOffset( final Feature feature ) throws FilterEvaluationException
  {
    if( m_perpendicularOffset != null )
    {
      final PlacementType placementType = getPlacementType( feature );
      if( placementType == PlacementType.absolute )
      {
        try
        {
          final String stringValue = m_perpendicularOffset.evaluate( feature );
          return Double.parseDouble( stringValue );
        }
        catch( final NumberFormatException e )
        {
          throw new FilterEvaluationException( "Element 'PerpendicularOffset' "
              + "must be equal to 'center', 'above', 'below' or 'auto' or it " + "must denote a valid double value!" );
        }

      }
    }

    return 0.0;
  }

  /**
   * @see org.kalypsodeegree_impl.graphics.sld.LinePlacement_Impl#getPerpendicularOffset(Feature)
   * @param perpendicularOffset
   */
  @Override
  public void setPerpendicularOffset( final double perpendicularOffset )
  {
    m_perpendicularOffset = StyleFactory.createParameterValueType( "" + perpendicularOffset );
  }

  /**
   * Returns the placement type (one of the constants defined in <tt>LinePlacement</tt>).
   * <p>
   * 
   * @throws FilterEvaluationException
   */
  @Override
  public PlacementType getPlacementType( final Feature feature ) throws FilterEvaluationException
  {
    if( m_perpendicularOffset == null )
      return PlacementType.absolute;

    final String stringValue = m_perpendicularOffset.evaluate( feature );
    try
    {
      return PlacementType.valueOf( stringValue );
    }
    catch( final Exception e )
    {
// e.printStackTrace();
      return PlacementType.absolute;
    }
  }

  /**
   * Sets the placement type (one of the constants defined in <tt>LinePlacement</tt>).
   * <p>
   * 
   * @param placementType
   */
  @Override
  public void setPlacementType( final PlacementType placementType )
  {
    if( placementType == PlacementType.absolute )
      m_perpendicularOffset = StyleFactory.createParameterValueType( "0.0" );
    else
      m_perpendicularOffset = StyleFactory.createParameterValueType( placementType.name() );
  }

  /**
   * Provides the thickness of the styled line (needed as information for the correct positioning of labels above and
   * below the line string).
   * <p>
   * 
   * @throws FilterEvaluationException
   */
  @Override
  public double getLineWidth( final Feature feature ) throws FilterEvaluationException
  {
    if( m_lineWidth != null )
      return Double.parseDouble( m_lineWidth.evaluate( feature ) );

    return 3;
  }

  /**
   * Provides the thickness of the styled line (needed as information for the correct positioning of labels above and
   * below the line string).
   * <p>
   * 
   * @param lineWidth
   *          the lineWidth to be set
   */
  @Override
  public void setLineWidth( final double lineWidth )
  {
    m_lineWidth = StyleFactory.createParameterValueType( "" + lineWidth );
  }

  /**
   * Defines the distance between two captions on the line string. One unit is the width of the label caption.
   * <p>
   * 
   * @throws FilterEvaluationException
   */
  @Override
  public int getGap( final Feature feature ) throws FilterEvaluationException
  {
    if( m_gap != null )
      return Integer.parseInt( m_gap.evaluate( feature ) );

    return 6;
  }

  /**
   * Defines the distance between two captions on the line string. One unit is the width of the label caption.
   * <p>
   * 
   * @param gap
   *          the gap to be set
   */
  @Override
  public void setGap( final int gap )
  {
    m_gap = StyleFactory.createParameterValueType( "" + gap );
  }

  /**
   * exports the content of the Font as XML formated String
   * 
   * @return xml representation of the Font
   */
  @Override
  public String exportAsXML()
  {
    Debug.debugMethodBegin();

    final StringBuffer sb = new StringBuffer( 1000 );
    sb.append( "<LinePlacement>" );
    if( m_perpendicularOffset != null )
    {
      sb.append( "<PerpendicularOffset>" );
      sb.append( ((Marshallable) m_perpendicularOffset).exportAsXML() );
      sb.append( "</PerpendicularOffset>" );
    }
    if( m_lineWidth != null )
    {
      sb.append( "<LineWidth>" );
      sb.append( ((Marshallable) m_lineWidth).exportAsXML() );
      sb.append( "</LineWidth>" );
    }
    if( m_gap != null )
    {
      sb.append( "<Gap>" );
      sb.append( ((Marshallable) m_gap).exportAsXML() );
      sb.append( "</Gap>" );
    }
    sb.append( "</LinePlacement>" );

    Debug.debugMethodEnd();
    return sb.toString();
  }
}