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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.apache.xmlbeans.impl.xb.xsdschema.ExplicitGroup;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.basics.IInitialize;
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.xml.ElementReference;

/**
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public abstract class RelationContentType implements IRelationContentType, IInitialize
{

  private final GMLSchema m_gmlSchema;

  private final ComplexType m_complexType;

  private FeatureType m_ftRelationTarget = null;

  public RelationContentType( final GMLSchema gmlSchema, final ComplexType complexType )
  {
    m_gmlSchema = gmlSchema;
    m_complexType = complexType;
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

        final ExplicitGroup sequence = getSequence();
        final List<FeatureType> result = new ArrayList<FeatureType>();
        if( sequence != null )
        {
          final Element[] elementArray = sequence.getElementArray();
          for( int i = 0; i < elementArray.length; i++ )
          {
            final QName ref = elementArray[i].getRef();
            final Element element;
            final GMLSchema schema;
            if( ref == null )
            {
              element = elementArray[i];
              schema = getGMLSchema();
            }
            else
            {
              final ElementReference reference = (ElementReference) m_gmlSchema.resolveReference( ref );
              schema = reference.getGMLSchema();
              element = reference.getElement();
            }
            final Object buildedObject = schema.getBuildedObjectFor( element );
            if( buildedObject instanceof FeatureType )
              result.add( (FeatureType) buildedObject );
          }
        }
        if( result.size() != 1 )
          throw new UnsupportedOperationException();
        m_ftRelationTarget = result.get( 0 );
        break;
    }
  }

  /**
   */
  public abstract ExplicitGroup getSequence( );

  /**
   */
  public ComplexType getComplexType( )
  {
    return m_complexType;
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
