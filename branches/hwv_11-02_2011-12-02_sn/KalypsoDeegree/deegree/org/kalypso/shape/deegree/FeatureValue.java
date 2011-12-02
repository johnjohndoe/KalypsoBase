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

import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.shape.ShapeDataException;
import org.kalypso.shape.dbf.AbstractDBFValue;
import org.kalypso.shape.dbf.DBFField;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * @author Gernot Belger
 */
public class FeatureValue extends AbstractDBFValue
{
  private final GMLXPath m_path;

  public FeatureValue( final IFeatureType type, final DBFField field, final GMLXPath path )
  {
    super( labelFromType( type, path ), field );

    m_path = path;
  }

  private static String labelFromType( final IFeatureType type, final GMLXPath path )
  {
    try
    {
      final Object result = GMLXPathUtilities.query( path, type );
      if( result instanceof IPropertyType )
      {
        final IPropertyType pt = (IPropertyType) result;
        final String localPart = pt.getQName().getLocalPart();
        return AnnotationUtilities.getAnnotation( pt.getAnnotation(), localPart, IAnnotation.ANNO_NAME );
      }
      else
        return String.format( "XPath does not evaluate to property: %s", path );
    }
    catch( final GMLXPathException e )
    {
      e.printStackTrace();
      return e.getLocalizedMessage();
    }
  }

  /**
   * @see org.kalypso.model.wspm.tuhh.ui.export.shape.IDBFValue#getValue(java.lang.Object)
   */
  @Override
  public Object getValue( final Object element ) throws ShapeDataException
  {
    final Feature feature = (Feature) element;
    try
    {
      return GMLXPathUtilities.query( m_path, feature );
    }
    catch( final GMLXPathException e )
    {
      final String message = String.format( "Failed to evaluate xpath '%s' on feature '%s'.", m_path, feature );
      throw new ShapeDataException( message, e );
    }
  }
}
