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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.xb.xsdschema.AttributeGroupRef;
import org.apache.xmlbeans.impl.xb.xsdschema.ComplexType;
import org.apache.xmlbeans.impl.xb.xsdschema.Element;
import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.javax.xml.namespace.QNameUtilities;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.builder.IInitialize;
import org.kalypso.gmlschema.feature.FeatureType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.xml.ElementReference;
import org.kalypso.gmlschema.xml.ElementWithOccurs;

/**
 * TODO: insert type comment here
 *
 * @author doemming
 */
public abstract class RelationContentType implements IRelationContentType, IInitialize
{
  /** TODO move this one to a constants class. */
  protected final static String XPATH_APPINFO_NAMESPACE_DECL = "declare namespace xs='" + NS.XSD_SCHEMA + "' " + "declare namespace kapp" + "='" + NS.KALYPSO_APPINFO + "' ";

  protected final static String DOCREF_XPATH = XPATH_APPINFO_NAMESPACE_DECL + "xs:annotation/xs:appinfo/kapp:documentReference";

  private final GMLSchema m_gmlSchema;

  private final ComplexType m_complexType;

  private FeatureType m_ftRelationTarget = null;

  private boolean m_linkable = false;

  private boolean m_inlinable = true;

  private IDocumentReference[] m_documentReferences;

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
  public void init( final int initializeRun ) throws GMLSchemaException
  {
    switch( initializeRun )
    {
      case IInitialize.INITIALIZE_RUN_FIRST:
      {
        final List<FeatureType> result = new ArrayList<FeatureType>();
        final List<ElementWithOccurs> localElements = getSequence();
        final ElementWithOccurs[] elementArray = localElements.toArray( new ElementWithOccurs[localElements.size()] );
        GMLSchema schema = null;
        for( final ElementWithOccurs elementWithOccurs : elementArray )
        {
          final QName ref = elementWithOccurs.getElement().getRef();
          final Element element;
          if( ref == null )
          {
            element = elementWithOccurs.getElement();
            schema = getGMLSchema();
          }
          else
          {
            final ElementReference reference = m_gmlSchema.resolveElementReference( ref );
            schema = reference.getGMLSchema();
            element = reference.getElement();
          }

          final Object buildedObject = schema.getBuildedObjectFor( element );
          if( buildedObject instanceof FeatureType )
          {
            result.add( (FeatureType) buildedObject );

            /*
             * We determine also if we may inline. Normaly this is true but when the maxOccurs of the referenced element
             * is set to 0.
             */
            if( elementWithOccurs.getOccurs().getMax() == 0 )
              m_inlinable = false;

            // we only need the first occurence and ignore the rest
            break;
          }
          else
          {
            // TODO
// if( Debug.traceSchemaParsing() )
// System.out.println( "schema error: links not to a feature:" + element );
          }
        }

        if( result.size() != 1 )
        {
          // This happens for gml:ReferenceType which is sometimes used (for example by xplan)
          return;
          // throw new UnsupportedOperationException( "An object relation must reference exactly one element" );
        }

        m_ftRelationTarget = result.get( 0 );

        /* Determine if linked objects are allowed */
        final AttributeGroupRef[] attributeGroups = getAttributeGroups();
        for( final AttributeGroupRef ag : attributeGroups )
        {
          final QName ref = ag.getRef();
          // TODO: what about gml2, probably it is different there? Maybe just look for xlink:href
          if( ref != null && QNameUtilities.equals( ref, NS.GML3, "AssociationAttributeGroup" ) )
            m_linkable = true;
          else if( ref != null && QNameUtilities.equals( ref, NS.XLINK, "simpleLink" ) && schema != null && "2.1.2".equals( schema.getGMLVersion() ) )
            m_linkable = true;
        }

        /* Collect document references */
        final String[] docRefs = collectReferences();
        if( docRefs.length == 0 )
          m_documentReferences = new IDocumentReference[] { IDocumentReference.SELF_REFERENCE };
        else
        {
          final Set<IDocumentReference> docRefSet = new HashSet<IDocumentReference>( docRefs.length );
          for( final String ref : docRefs )
          {
            if( IDocumentReference.SELF_REFERENCE_NAME.equals( ref ) )
              docRefSet.add( IDocumentReference.SELF_REFERENCE );
            else
              docRefSet.add( new DocumentReference( ref ) );
          }

          m_documentReferences = docRefSet.toArray( new IDocumentReference[docRefSet.size()] );
        }

      }
        break;
    }
  }

  public abstract List<ElementWithOccurs> getSequence( );

  public abstract AttributeGroupRef[] getAttributeGroups( );

  public abstract String[] collectReferences( );

  public ComplexType getComplexType( )
  {
    return m_complexType;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationContentType#getTargetFeatureTypes(org.kalypso.gmlschema.GMLSchema,
   *      boolean)
   */
  public IFeatureType getTargetFeatureType( )
  {
    return m_ftRelationTarget;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationContentType#isLinkable()
   */
  public boolean isLinkable( )
  {
    return m_linkable;
  }

  public boolean isInlineable( )
  {
    return m_inlinable;
  }

  /**
   * @see org.kalypso.gmlschema.property.relation.IRelationContentType#getDocumentReferences()
   */
  public IDocumentReference[] getDocumentReferences( )
  {
    return m_documentReferences;
  }
}
