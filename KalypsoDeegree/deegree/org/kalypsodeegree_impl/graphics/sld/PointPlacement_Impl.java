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
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.graphics.sld.PointPlacement;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree_impl.tools.Debug;

/**
 * Incarnation of a sld:PointPlacement-element. For a PointPlacement, the anchor point of the label and a linear
 * displacement from the point can be specified, to allow a graphic symbol to be plotted directly at the point. This
 * might be useful to label a city, for example. For a LinePlacement, a perpendicular offset can be specified, to allow
 * the line itself to be plotted also. This might be useful for labelling a road or a river, for example.
 * <p>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public class PointPlacement_Impl implements PointPlacement, Marshallable
{
  private ParameterValueType m_rotation = null;

  private ParameterValueType[] m_anchorPoint = null;

  private ParameterValueType[] m_displacement = null;

  // should the placement be optimized?
  private boolean m_auto = false;

  PointPlacement_Impl( )
  {
    // default
  }

  /**
   * Creates a new PointPlacement_Impl object.
   * 
   * @param anchorPoint
   * @param displacement
   * @param rotation
   */
  public PointPlacement_Impl( final ParameterValueType[] anchorPoint, final ParameterValueType[] displacement, final ParameterValueType rotation, final boolean auto )
  {
    m_anchorPoint = anchorPoint;
    m_displacement = displacement;
    m_rotation = rotation;
    m_auto = auto;
  }

  /**
   * The AnchorPoint element of a PointPlacement gives the location inside of a label to use for anchoring the label to
   * the main-geometry point.
   * <p>
   * </p>
   * The coordinates are given as two floating-point numbers in the AnchorPointX and AnchorPointY elements each with
   * values between 0.0 and 1.0 inclusive. The bounding box of the label to be rendered is considered to be in a
   * coorindate space from 0.0 (lower-left corner) to 1.0 (upper-right corner), and the anchor position is specified as
   * a point in this space. The default point is X=0, Y=0.5, which is at the middle height of the left-hand side of the
   * label. A system may choose different anchor points to de-conflict labels.
   * <p>
   * 
   * @param feature
   *          specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
   * @return 2 double values: x ([0]) and y ([0])
   * @throws FilterEvaluationException
   *           if the evaluation fails
   */
  @Override
  public double[] getAnchorPoint( final Feature feature ) throws FilterEvaluationException
  {
    final double[] anchorPointVal = { 0.0, 0.5 };

    if( m_anchorPoint != null )
    {
      anchorPointVal[0] = Double.parseDouble( m_anchorPoint[0].evaluate( feature ) );
      anchorPointVal[1] = Double.parseDouble( m_anchorPoint[1].evaluate( feature ) );
    }

    return anchorPointVal;
  }

  /**
   * @see PointPlacement#getAnchorPoint(Feature) <p>
   * @param anchorPoint
   *          anchorPoint for the PointPlacement
   */
  @Override
  public void setAnchorPoint( final double[] anchorPoint )
  {
    ParameterValueType pvt = null;
    final ParameterValueType[] pvtArray = new ParameterValueType[anchorPoint.length];
    for( int i = 0; i < anchorPoint.length; i++ )
    {
      pvt = StyleFactory.createParameterValueType( "" + anchorPoint[i] );
      pvtArray[i] = pvt;
    }
    m_anchorPoint = pvtArray;
  }

  /**
   * The Displacement element of a PointPlacement gives the X and Y displacements from the main-geometry point to render
   * a text label.
   * <p>
   * </p>
   * This will often be used to avoid over-plotting a graphic symbol marking a city or some such feature. The
   * displacements are in units of pixels above and to the right of the point. A system may reflect this displacement
   * about the X and/or Y axes to de-conflict labels. The default displacement is X=0, Y=0.
   * <p>
   * 
   * @param feature
   *          specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
   * @return 2 double values: x ([0]) and y ([0])
   * @throws FilterEvaluationException
   *           if the evaluation fails*
   */
  @Override
  public double[] getDisplacement( final Feature feature ) throws FilterEvaluationException
  {
    final double[] displacementVal = { 0.0, 0.0 };

    if( m_displacement != null )
    {
      displacementVal[0] = Double.parseDouble( m_displacement[0].evaluate( feature ) );
      displacementVal[1] = Double.parseDouble( m_displacement[1].evaluate( feature ) );
    }

    return displacementVal;
  }

  /**
   * @see PointPlacement#getDisplacement(Feature) <p>
   * @param displacement
   */
  @Override
  public void setDisplacement( final double[] displacement )
  {
    ParameterValueType pvt = null;
    final ParameterValueType[] pvtArray = new ParameterValueType[displacement.length];
    for( int i = 0; i < displacement.length; i++ )
    {
      pvt = StyleFactory.createParameterValueType( "" + displacement[i] );
      pvtArray[i] = pvt;
    }
    m_displacement = pvtArray;
  }

  /**
   * The Rotation of a PointPlacement gives the clockwise rotation of the label in degrees from the normal direction for
   * a font (left-to-right for Latin- derived human languages at least).
   * <p>
   * 
   * @param feature
   *          specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
   * @return double value describing the rotation parameter
   * @throws FilterEvaluationException
   *           if the evaluation fails*
   */
  @Override
  public double getRotation( final Feature feature ) throws FilterEvaluationException
  {
    if( m_rotation != null )
      return Double.parseDouble( m_rotation.evaluate( feature ) );

    return 0.0;
  }

  /**
   * @see PointPlacement#getRotation(Feature)
   * @param rotation
   *          the rotation to be set for the PointPlacement
   */
  @Override
  public void setRotation( final double rotation )
  {
    ParameterValueType pvt = null;
    pvt = StyleFactory.createParameterValueType( "" + rotation );
    m_rotation = pvt;
  }

  /**
   * Returns whether the placement should be optimized or not.
   * <p>
   * 
   * @return true, if it should be optimized
   */
  @Override
  public boolean isAuto( )
  {
    return m_auto;
  }

  /**
   * <p>
   * 
   * @param auto
   */
  @Override
  public void setAuto( final boolean auto )
  {
    m_auto = auto;
  }

  /**
   * exports the content of the PointPlacement as XML formated String
   * 
   * @return xml representation of the PointPlacement
   */
  @Override
  public String exportAsXML( )
  {
    Debug.debugMethodBegin();

    final StringBuffer sb = new StringBuffer( 1000 );
    sb.append( "<PointPlacement" );
    if( m_auto )
    {
      sb.append( " auto='true'" );
    }
    sb.append( ">" );
    if( m_anchorPoint != null && m_anchorPoint.length > 1 )
    {
      sb.append( "<AnchorPoint>" ).append( "<AnchorPointX>" );
      sb.append( ((Marshallable) m_anchorPoint[0]).exportAsXML() );
      sb.append( "</AnchorPointX>" ).append( "<AnchorPointY>" );
      sb.append( ((Marshallable) m_anchorPoint[1]).exportAsXML() );
      sb.append( "</AnchorPointY>" ).append( "</AnchorPoint>" );
    }
    if( m_displacement != null && m_displacement.length > 1 )
    {
      sb.append( "<Displacement>" ).append( "<DisplacementX>" );
      if( m_anchorPoint == null || m_anchorPoint[0] == null )
        sb.append( 0.0 );
      else
        sb.append( ((Marshallable) m_anchorPoint[0]).exportAsXML() );
      sb.append( "</DisplacementX>" ).append( "<DisplacementY>" );
      if( m_anchorPoint == null || m_anchorPoint[1] == null )
        sb.append( 0.5 );
      else
        sb.append( ((Marshallable) m_anchorPoint[1]).exportAsXML() );
      sb.append( "</DisplacementY>" ).append( "</Displacement>" );
    }
    if( m_rotation != null )
    {
      sb.append( "<Rotation>" );
      sb.append( ((Marshallable) m_rotation).exportAsXML() );
      sb.append( "</Rotation>" );
    }

    sb.append( "</PointPlacement>" );

    Debug.debugMethodEnd();
    return sb.toString();
  }
}