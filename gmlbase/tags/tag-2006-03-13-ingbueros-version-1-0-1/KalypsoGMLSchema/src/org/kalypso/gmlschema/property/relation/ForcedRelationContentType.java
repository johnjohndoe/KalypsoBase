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
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * relation forced by informations from a appinfo-tag (e.g. adv:referenziertesElement)
 * 
 * @author doemming
 */
public class ForcedRelationContentType implements IRelationContentType, IInitialize
{

  private final GMLSchema m_gmlSchema;

  private FeatureType m_ftRelationTarget = null;

  private final QName m_referencedQNameFeatureType;

  public ForcedRelationContentType( final GMLSchema gmlSchema, final QName referencedQNameFeatureType )
  {
    m_gmlSchema = gmlSchema;
    m_referencedQNameFeatureType = referencedQNameFeatureType;
  }

  public GMLSchema getGMLSchema( )
  {
    return m_gmlSchema;
  }

  /**
   * @see org.kalypso.gmlschema.basics.IInitialize#init(int)
   */
  public void init( int initializeRun )
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:
        final ElementReference reference = (ElementReference) m_gmlSchema.resolveReference( m_referencedQNameFeatureType );
        final GMLSchema schema = reference.getGMLSchema();
        final Element element = reference.getElement();
        final Object buildedObject = schema.getBuildedObjectFor( element );
        if( buildedObject instanceof FeatureType )
          m_ftRelationTarget = (FeatureType) buildedObject;
        else
          throw new UnsupportedOperationException();
        break;
    }
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationContentType#getTargetFeatureTypes(org.kalypso.gmlschema.GMLSchema,
   *      boolean)
   */
  public IFeatureType[] getTargetFeatureTypes( GMLSchema context, boolean includeAbstract )
  {
    return m_ftRelationTarget.getSubstituts( context, includeAbstract, true );
  }

}
