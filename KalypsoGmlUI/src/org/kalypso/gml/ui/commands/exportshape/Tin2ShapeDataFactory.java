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
package org.kalypso.gml.ui.commands.exportshape;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.shape.IShapeData;
import org.kalypso.shape.ShapeConst;
import org.kalypso.shape.ShapeDataException;
import org.kalypso.shape.deegree.GM_Object2Shape;
import org.kalypso.shape.deegree.GenericShapeDataFactory;
import org.kalypso.shape.deegree.IShapeDataFactory;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.io.shpapi.dataprovider.TriangulatedSurfaceSinglePartShapeDataProvider;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author Gernot Belger
 */
public class Tin2ShapeDataFactory implements IShapeDataFactory
{
  private final Feature[] m_features;

  private final Charset m_charset;

  private final String m_coordinateSystem;

  public Tin2ShapeDataFactory( final Feature[] features, final Charset charset, final String coordinateSystem )
  {
    m_features = features;
    m_charset = charset;
    m_coordinateSystem = coordinateSystem;
  }

  /**
   * @see org.kalypso.gml.ui.commands.exportshape.IShapeDataFactory#createData()
   */
  @Override
  public IShapeData createData( ) throws ShapeDataException
  {
    // TODO: get shape type from dialog
    // TODO: let user choose
    final int shapeType = ShapeConst.SHAPE_TYPE_POLYGONZ;

    final IFeatureType featureType = GenericShapeDataFactory.findLeastCommonType( m_features );
    final IValuePropertyType[] tinTypes = findTinTypes( featureType );

    if( tinTypes.length == 0 )
    {
      final String message = String.format( "Choosen features do not contains a Triangulated-Surface: %s", featureType );
      throw new ShapeDataException( message );
    }

    final GMLXPath geometry = new GMLXPath( tinTypes[0].getQName() );
    final GM_Object2Shape shapeConverter = new GM_Object2Shape( shapeType, m_coordinateSystem );
    return new TriangulatedSurfaceSinglePartShapeDataProvider( m_features, geometry, m_charset, shapeConverter );
  }

  private IValuePropertyType[] findTinTypes( final IFeatureType featureType )
  {
    final Collection<IValuePropertyType> geometries = new ArrayList<IValuePropertyType>();
    final IPropertyType[] geometryProperties = featureType.getProperties();
    for( final IPropertyType propertyType : geometryProperties )
    {
      if( propertyType instanceof IValuePropertyType )
      {
        final IValuePropertyType vpt = (IValuePropertyType) propertyType;
        if( vpt.isGeometry() && !vpt.isList() )
        {
          // TODO: list of tins not yet supported (the data provider is not able to handle them)

          final Class< ? > valueClass = vpt.getValueClass();
          if( GM_TriangulatedSurface.class.isAssignableFrom( valueClass ) )
            geometries.add( vpt );
        }
      }
    }

    return geometries.toArray( new IValuePropertyType[geometries.size()] );
  }

}
