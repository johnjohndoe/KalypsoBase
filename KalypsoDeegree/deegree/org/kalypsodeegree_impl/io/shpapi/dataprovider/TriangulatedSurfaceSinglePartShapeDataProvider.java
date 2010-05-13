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
package org.kalypsodeegree_impl.io.shpapi.dataprovider;

import java.nio.charset.Charset;
import java.util.Map;

import org.deegree.framework.util.Pair;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.shape.IShapeData;
import org.kalypso.shape.ShapeDataException;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.deegree.GM_Object2Shape;
import org.kalypso.shape.deegree.GenericShapeDataFactory;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * data provider for exporting GM_TriangulatedSurface patches from 1d2d result. It converts the existing
 * TriangulatedSurface feature into several patch features. <br>
 * This is because of the very slow handling of MultiShapesZ in ArcView.<br>
 * If you want to export the TriangulatedSurface feature as it is in the gml, use
 * {@link TriangulatedSurfaceMultiPartShapeDataProvider} instead.
 * 
 * @author Thomas Jung
 */
public class TriangulatedSurfaceSinglePartShapeDataProvider implements IShapeData
{
  private final GM_Object2Shape m_gmObject2Shape;

  private final Feature[] m_features;

  private final GMLXPath m_geometry;

  private Map<DBFField, GMLXPath> m_dataMapping = null;

  private DBFField[] m_fields;

  private int m_total;

  private Integer[] m_tinSizes;

  private final Charset m_charset;

  public TriangulatedSurfaceSinglePartShapeDataProvider( final Feature[] features, final GMLXPath geometry, final Charset charset, final GM_Object2Shape gmObject2Shape )
  {
    m_features = features;
    m_geometry = geometry;
    m_charset = charset;
    m_gmObject2Shape = gmObject2Shape;
  }

  /**
   * @see org.kalypso.shape.IShapeData#getCharset()
   */
  @Override
  public Charset getCharset( )
  {
    return m_charset;
  }

  /**
   * @see org.kalypso.shape.IShapeData#getCoordinateSystem()
   */
  @Override
  public String getCoordinateSystem( )
  {
    return m_gmObject2Shape.getCoordinateSystem();
  }

  /**
   * @see org.kalypso.shape.IShapeData#getFields()
   */
  @Override
  public DBFField[] getFields( ) throws ShapeDataException
  {
    if( m_fields == null )
    {
      try
      {
        final Map<DBFField, GMLXPath> dataMapping = getDataMapping();
        m_fields = dataMapping.keySet().toArray( new DBFField[dataMapping.size()] );
      }
      catch( final DBaseException e )
      {
        throw new ShapeDataException( "Unable to create DBF signature.", e ); //$NON-NLS-1$
      }
    }

    return m_fields;
  }

  private Map<DBFField, GMLXPath> getDataMapping( ) throws DBaseException
  {
    if( m_dataMapping == null )
    {
      final IFeatureType featureType = GenericShapeDataFactory.findLeastCommonType( m_features );
      m_dataMapping = GenericShapeDataFactory.findDataMapping( featureType );
    }
    return m_dataMapping;
  }

  /**
   * @see org.kalypso.shape.IShapeData#getShapeType()
   */
  @Override
  public int getShapeType( )
  {
    return m_gmObject2Shape.getShapeType();
  }

  /**
   * @see org.kalypso.shape.IShapeData#size()
   */
  @Override
  public int size( ) throws ShapeDataException
  {
    if( m_tinSizes == null )
    {
      m_tinSizes = new Integer[m_features.length];

      m_total = 0;
      for( int i = 0; i < m_features.length; i++ )
      {
        final Feature feature = m_features[i];

        final GM_TriangulatedSurface tin = getTin( feature );
        if( tin == null )
          m_tinSizes[i] = 0;
        else
          m_tinSizes[i] = tin.size();

        m_total += m_tinSizes[i];
      }

    }

    return m_total;
  }

  private GM_TriangulatedSurface getTin( final Feature feature ) throws ShapeDataException
  {
    try
    {
      final Object property = GMLXPathUtilities.query( m_geometry, feature );
      if( property == null )
        return null;

      if( property instanceof GM_TriangulatedSurface )
        return (GM_TriangulatedSurface) property;

      throw new ShapeDataException( String.format( "Geometry is not a GM_TriangualtedSurface: %s", m_geometry.toString() ) ); //$NON-NLS-1$
    }
    catch( final GMLXPathException e )
    {
      throw new ShapeDataException( "Unable to access triangulated surface", e ); //$NON-NLS-1$
    }
  }

  /**
   * Calculates feature and triangle index from the outer index.<br>
   * Returns a Pair<Festure-Index, Triangle-Index>
   */
  private Pair<Integer, Integer> calcFeatureAndTriangleIndex( final int index ) throws ShapeDataException
  {
    int total = 0;

    for( int featureIndex = 0; featureIndex < m_tinSizes.length; featureIndex++ )
    {
      final Integer tinSize = m_tinSizes[featureIndex];
      if( total + tinSize > index )
      {
        final int triangleIndex = index - total;
        return new Pair<Integer, Integer>( featureIndex, triangleIndex );
      }
      else
        featureIndex++;

      total += tinSize;
    }

    throw new ShapeDataException( String.format( "Index out of bounds: %d", index ) );
  }

  public ISHPGeometry getGeometry( final int index ) throws ShapeDataException
  {
    final Pair<Integer, Integer> featureAndTriangleIndex = calcFeatureAndTriangleIndex( index );
    final Feature feature = m_features[featureAndTriangleIndex.first];
    final GM_TriangulatedSurface tin = getTin( feature );

    final GM_Triangle triangle = tin.get( featureAndTriangleIndex.second );

    return m_gmObject2Shape.convert( triangle );
  }

  /**
   * @see org.kalypso.shape.IShapeData#getData(int, int)
   */
  @Override
  public Object getData( final int row, final int field ) throws ShapeDataException
  {
    try
    {
      final Pair<Integer, Integer> featureAndTriangleIndex = calcFeatureAndTriangleIndex( row );
      final Feature feature = m_features[featureAndTriangleIndex.first];
      final Map<DBFField, GMLXPath> dataMapping = getDataMapping();
      final DBFField[] fields = getFields();
      final GMLXPath xPath = dataMapping.get( fields[field] );
      return GMLXPathUtilities.query( xPath, feature );
    }
    catch( final DBaseException e )
    {
      throw new ShapeDataException( e );
    }
    catch( final GMLXPathException e )
    {
      final String message = String.format( "Unable to resolve data for row %d, field %d", row, field ); //$NON-NLS-1$
      throw new ShapeDataException( message, e );
    }
  }
}
