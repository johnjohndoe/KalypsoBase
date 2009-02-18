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
package org.kalypso.gmlschema.property.relation;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.AbstractPropertyTypeFromElement;

/**
 * @author doemming
 */
public class AdvRelationType extends AbstractPropertyTypeFromElement implements IRelationType
{
  private IRelationContentType m_relationContentType;

  private final QName m_advReferenziertesElement;

  public AdvRelationType( final GMLSchema gmlSchema, final Element element, final QName advReferenziertesElement )
  {
    super( gmlSchema, element );
    m_advReferenziertesElement = advReferenziertesElement;
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:
        m_relationContentType = new ForcedRelationContentType( m_gmlSchema, m_advReferenziertesElement );
        break;
    }
    if( m_relationContentType != null )
      m_relationContentType.init( initializeRun );
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isInlineAble()
   */
  public boolean isInlineAble( )
  {
    return false;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationType#isLinkAble()
   */
  public boolean isLinkAble( )
  {
    return true;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationContentType#getTargetFeatureTypes(org.kalypso.gmlschema.GMLSchema,
   *      boolean)
   */
  public IFeatureType[] getTargetFeatureTypes( GMLSchema context, boolean includeAbstract )
  {
    return m_relationContentType.getTargetFeatureTypes( context, includeAbstract );
  }
}
