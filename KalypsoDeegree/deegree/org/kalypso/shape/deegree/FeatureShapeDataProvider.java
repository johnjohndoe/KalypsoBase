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
package org.kalypso.shape.deegree;

import java.util.List;
import java.util.Map;

import org.kalypso.shape.IShapeData;
import org.kalypso.shape.ShapeDataException;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.geometry.SHPNullShape;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * @author Gernot Belger
 */
public class FeatureShapeDataProvider implements IShapeData
{
  private final List<Feature> m_features;

  private final GM_Object2Shape m_gmObject2Shape;

  private final Map<DBFField, GMLXPath> m_mapping;

  private final DBFField[] m_fields;

  private final GMLXPath m_geometry;

  public FeatureShapeDataProvider( final List<Feature> features, final byte shapeType, final Map<DBFField, GMLXPath> mapping, final GMLXPath geometry )
  {
    m_features = features;
    m_geometry = geometry;
    m_fields = mapping.keySet().toArray( new DBFField[mapping.size()] );
    m_mapping = mapping;
    m_gmObject2Shape = new GM_Object2Shape( shapeType );
  }

  /**
   * @see org.kalypso.shape.IShapeDataProvider#getData(int, int)
   */
  @Override
  public Object getData( final int row, final int field ) throws ShapeDataException
  {
    try
    {
      final GMLXPath xPath = m_mapping.get( m_fields[field] );
      final Feature feature = m_features.get( row );
      final Object value = GMLXPathUtilities.query( xPath, feature );

      // TODO: we need an additional mapping here?!

      return value;
    }
    catch( final GMLXPathException e )
    {
      final String message = String.format( "Failed to evaluate geometry xpath on row %d", row );
      throw new ShapeDataException( message, e );
    }

  }

  /**
   * @see org.kalypso.shape.IShapeDataProvider#getFields()
   */
  @Override
  public DBFField[] getFields( )
  {
    return m_fields;
  }

  /**
   * @see org.kalypso.shape.IShapeDataProvider#getGeometry(int)
   */
  @Override
  public ISHPGeometry getGeometry( final int index ) throws ShapeDataException
  {
    try
    {
      final Feature feature = m_features.get( index );
      final Object geom = GMLXPathUtilities.query( m_geometry, feature );
      if( geom == null )
        return new SHPNullShape();

      if( !(geom instanceof GM_Object) )
      {
        final String message = String.format( "XPath failed to evaluate to a geometry on row %d", index );
        throw new ShapeDataException( message );
      }

      return m_gmObject2Shape.convert( (GM_Object) geom );
    }
    catch( final GMLXPathException e )
    {
      final String message = String.format( "Failed to evaluate geometry xpath on row %d", index );
      throw new ShapeDataException( message, e );
    }
  }

  /**
   * @see org.kalypso.shape.IShapeDataProvider#getShapeType()
   */
  @Override
  public byte getShapeType( )
  {
    return m_gmObject2Shape.getShapeType();
  }

  /**
   * @see org.kalypso.shape.IShapeDataProvider#size()
   */
  @Override
  public int size( )
  {
    return m_features.size();
  }

}
