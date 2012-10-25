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
package org.kalypso.model.wspm.core.gml.classifications;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.ColorUtilities;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.gml.classifications.IStyleParameterConstants.TYPE;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.FeatureBindingCollection;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * @author Gernot Belger
 */
public class Style extends Feature_Impl implements IStyle
{
  private IFeatureBindingCollection<IStyleParameter> m_parameters = null;

  public Style( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  @Override
  public synchronized IFeatureBindingCollection<IStyleParameter> getParameterCollection( )
  {
    if( m_parameters == null )
      m_parameters = new FeatureBindingCollection<>( this, IStyleParameter.class, MEMBER_PARAMETER );

    return m_parameters;
  }

  private String getParameterValue( final String parameterKey )
  {
    final IFeatureBindingCollection<IStyleParameter> parameters = getParameterCollection();
    for( final IStyleParameter parameter : parameters )
    {
      final String key = parameter.getKey();
      if( key.equals( parameterKey ) )
        return parameter.getValue();
    }

    return null;
  }

  @Override
  public TYPE getType( )
  {
    final String typeName = getParameterValue( IStyleParameterConstants.PARAMETER_TYPE );
    if( StringUtils.isBlank( typeName ) )
      return null;

    try
    {
      return IStyleParameterConstants.TYPE.valueOf( typeName );
    }
    catch( final IllegalArgumentException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private int getIntParameter( final String key, final int defaultValue )
  {
    final String textValue = getParameterValue( key );
    if( StringUtils.isBlank( textValue ) )
      return defaultValue;

    return NumberUtils.parseQuietInt( textValue, defaultValue );
  }

  private RGB getColorParameter( final String key )
  {
    final RGB black = new RGB( 0, 0, 0 );

    final String textValue = getParameterValue( key );
    if( StringUtils.isBlank( textValue ) )
      return black;

    final RGB htmlColor = ColorUtilities.toRGBFromHTML( textValue );
    if( htmlColor == null )
      return black;

    return htmlColor;
  }

  @Override
  public int getMarkerWidth( )
  {
    return getIntParameter( IStyleParameterConstants.MARKER_WIDTH, 1 );
  }

  @Override
  public int getMarkerHeight( )
  {
    return getIntParameter( IStyleParameterConstants.MARKER_HEIGHT, 1 );
  }

  @Override
  public int getStrokeWidth( )
  {
    return getIntParameter( IStyleParameterConstants.STROKE_WIDTH, 1 );
  }

  @Override
  public RGB getStrokeColor( )
  {
    return getColorParameter( IStyleParameterConstants.STROKE_COLOR );
  }

  @Override
  public RGB getFillColor( )
  {
    return getColorParameter( IStyleParameterConstants.FILL_COLOR );
  }
}