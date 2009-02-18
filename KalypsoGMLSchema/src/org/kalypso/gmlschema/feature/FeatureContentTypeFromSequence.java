/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.gmlschema.feature;

import java.util.List;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.xml.ElementWithOccurs;

/**
 * representation of a feature content definition from a xml schema sequence fragment.
 * 
 * @author doemming
 */
public class FeatureContentTypeFromSequence extends FeatureContentType
{
  private final List<ElementWithOccurs> m_elementList;

  public FeatureContentTypeFromSequence( final GMLSchema gmlSchema, final ComplexType complexType, final List<ElementWithOccurs> elementList )
  {
    super( gmlSchema, complexType );

    m_elementList = elementList;
  }

  @Override
  public List<ElementWithOccurs> getSequence( )
  {
    return m_elementList;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getBase()
   */
  public IFeatureContentType getBase( )
  {
    return null;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getDerivationType()
   */
  public int getDerivationType( )
  {
    return DERIVATION_NONE;
  }

  /**
   * @see org.kalypso.gmlschema.feature.IFeatureContentType#getDirectProperties()
   */
  public IPropertyType[] getDirectProperties( )
  {
    return getProperties();
  }
}
