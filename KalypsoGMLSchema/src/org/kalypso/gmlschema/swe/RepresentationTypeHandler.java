/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.gmlschema.swe;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.DefaultAnnotation;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.restriction.EnumerationRestriction;
import org.kalypso.gmlschema.property.restriction.FractionDigitRestriction;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.property.restriction.MaxInclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.MinInclusiveRestriction;
import org.kalypso.gmlschema.property.restriction.RegExpRestriction;
import org.kalypso.gmlschema.types.SimpleDOMTypeHandler;
import org.kalypso.gmlschema.types.TypeRegistryException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * TODO: using the class RepresantationType is not nice. We should rather create 4 different type handlers for the 4
 * choices and return the read object respectively.
 * 
 * @author Marc Schlienger
 */
public class RepresentationTypeHandler extends SimpleDOMTypeHandler
{
  public RepresentationTypeHandler( )
  {
    super( "representation", new QName( NS.SWE, "RepresentationType" ), false ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#internalUnmarshall(org.w3c.dom.Node, java.lang.Object,
   *      org.kalypso.contribs.java.net.IUrlResolver)
   */
  @Override
  protected Object internalUnmarshall( final Node node ) throws TypeRegistryException
  {
    if( !(node instanceof Element) )
      throw new TypeRegistryException( "Empty or non-existent representation: cannot create RepresentationType instance" ); //$NON-NLS-1$

    final Element typeElt = (Element) node;

    final String localName = typeElt.getLocalName();
    final String base;
    if( "SimpleType".equals( localName ) ) //$NON-NLS-1$
    {
      // read base-attribute of restriction
      final NodeList elementsByTagName = typeElt.getElementsByTagNameNS( NS.XST, "restriction" ); //$NON-NLS-1$
      if( elementsByTagName.getLength() == 1 )
      {
        final Element restrictionElt = (Element) elementsByTagName.item( 0 );
        base = restrictionElt.getAttribute( "base" ); //$NON-NLS-1$

        // TODO: recurse into simple-type and collect all restrictions

        if( base == null || base.length() == 0 )
          throw new TypeRegistryException( "No base defined for st:restriction." ); //$NON-NLS-1$
      }
      else
      {
        // error handling?
        throw new TypeRegistryException( "SimpleType without st:restriction definition." ); //$NON-NLS-1$
      }
    }
    else if( "Number".equals( localName ) ) //$NON-NLS-1$
    {
      base = "double"; //$NON-NLS-1$
    }
    else if( "Word".equals( localName ) ) //$NON-NLS-1$
    {
      base = "string"; //$NON-NLS-1$
    }
    else if( "Boolean".equals( localName ) ) //$NON-NLS-1$
    {
      base = "boolean"; //$NON-NLS-1$
    }
    else
    {
      base = "string"; //$NON-NLS-1$
    }

    // read base-attribute of restriction
    // final NodeList elementsByTagName = typeElt.getElementsByTagNameNS( NS.SWE, "restriction" );
    // Element restrictionElt = null;
    // if( elementsByTagName.getLength() == 1 )
    // restrictionElt = (Element) elementsByTagName.item( 0 );

    // TODO: read IRestriction - Elements from restrictionElt and append them to the RepresentationType

    // parse unitOfMeasure
    final NodeList nlUom = typeElt.getElementsByTagNameNS( NS.GML3, "unitOfMeasure" ); //$NON-NLS-1$
    Element uomElt = null;
    if( nlUom.getLength() == 1 )
    {
      uomElt = (Element) nlUom.item( 0 );
    }

    String unit = ""; //$NON-NLS-1$
    if( uomElt != null )
    {
      unit = uomElt.getAttribute( "uom" ); //$NON-NLS-1$
    }

    // parse frame (i.e. timezone information for dates)
    final NodeList nlFrame = typeElt.getElementsByTagNameNS( NS.GML3, "frame" ); //$NON-NLS-1$
    Element frameElt = null;
    if( nlFrame.getLength() == 1 )
    {
      frameElt = (Element) nlFrame.item( 0 );
    }

    String frame = ""; //$NON-NLS-1$
    if( frameElt != null )
    {
      frame = frameElt.getAttributeNS( NS.XLINK, "href" ); //$NON-NLS-1$
    }

    // TODO: parse classification
    final String classification = ""; //$NON-NLS-1$

    /* parsing of restrictions */
    final Set<IRestriction> myRestrictions = new HashSet<IRestriction>();

    NodeList restriction = typeElt.getElementsByTagNameNS( NS.SWE, "restriction" ); //$NON-NLS-1$
    if( restriction.getLength() <= 0 )
    {
      restriction = typeElt.getElementsByTagNameNS( NS.XST, "restriction" ); //$NON-NLS-1$
    }

    if( restriction.getLength() > 0 )
    {
      final Map<Object, IAnnotation> hEnumerations = new LinkedHashMap<Object, IAnnotation>();
      final Set<String> sPatterns = new HashSet<String>(); // *< for pattern regex matching

      for( int i = 0; i < restriction.getLength(); i++ )
      {
        final Node rNode = restriction.item( i );
        final NodeList restrictions = rNode.getChildNodes();

        for( int j = 0; j < restrictions.getLength(); j++ )
        {
          final Node myNode = restrictions.item( j );

          // TODO: implement other restrictions (see, who implements IRestriction!)
          if( "pattern".equals( myNode.getLocalName() ) ) //$NON-NLS-1$
          {
            final NamedNodeMap attributes = myNode.getAttributes();
            for( int a = 0; a < attributes.getLength(); a++ )
            {
              final Node n = attributes.item( a );
              if( n instanceof Attr )
              {
                final Attr attribute = (Attr) n;
                final String value = attribute.getValue();
                sPatterns.add( value );
              }
            }
          }
          else if( "enumeration".equals( myNode.getLocalName() ) ) //$NON-NLS-1$
          {
            // key
            final NamedNodeMap attributes = myNode.getAttributes();
            final Node itmKey = attributes.getNamedItem( "value" ); //$NON-NLS-1$

            // values
            final NodeList childNodes = myNode.getChildNodes();
            final IAnnotation foundAnnotation = AnnotationUtilities.parseAnnontation( itmKey.getTextContent(), childNodes );
            final IAnnotation annontation = foundAnnotation == null ? new DefaultAnnotation( Platform.getNL(), itmKey.getTextContent() ) : foundAnnotation;

            hEnumerations.put( itmKey.getTextContent(), annontation );
          }
          else if( "minInclusive".equals( myNode.getLocalName() ) ) //$NON-NLS-1$
          {
            final NamedNodeMap attributes = myNode.getAttributes();
            for( int a = 0; a < attributes.getLength(); a++ )
            {
              final Node n = attributes.item( a );
              if( n instanceof Attr )
              {
                final Attr attribute = (Attr) n;
                final String value = attribute.getValue();
                myRestrictions.add( new MinInclusiveRestriction( new Double( value ) ) );
              }
            }
          }
          else if( "maxInclusive".equals( myNode.getLocalName() ) ) //$NON-NLS-1$
          {
            final NamedNodeMap attributes = myNode.getAttributes();
            for( int a = 0; a < attributes.getLength(); a++ )
            {
              final Node n = attributes.item( a );
              if( n instanceof Attr )
              {
                final Attr attribute = (Attr) n;
                final String value = attribute.getValue();
                myRestrictions.add( new MaxInclusiveRestriction( new Double( value ) ) );
              }
            }
          }// end if-else-if
          else if( "fractionDigits".equals( myNode.getLocalName() ) ) //$NON-NLS-1$
          {
            final NamedNodeMap attributes = myNode.getAttributes();
            for( int a = 0; a < attributes.getLength(); a++ )
            {
              final Node n = attributes.item( a );
              if( n instanceof Attr )
              {
                final Attr attribute = (Attr) n;
                final String value = attribute.getValue();
                myRestrictions.add( new FractionDigitRestriction( new Integer( value ) ) );
              }
            }
          }
        } // endFor j
      } // endFor i

      if( localName.equals( "Boolean" ) ) //$NON-NLS-1$
      {
        // Special case for boolean representations: use boolean values in enum
        if( hEnumerations.size() != 2 )
          throw new TypeRegistryException( "Boolean representation type must have exactly two enumeration restrictions." ); //$NON-NLS-1$

        final Set<Entry<Object, IAnnotation>> entrySet = hEnumerations.entrySet();
        final Iterator<Entry<Object, IAnnotation>> entryIt = entrySet.iterator();

        final Entry<Object, IAnnotation> falseEntry = entryIt.next();
        final Entry<Object, IAnnotation> trueEntry = entryIt.next();

        final Map<Object, IAnnotation> booleanEnum = new LinkedHashMap<Object, IAnnotation>();
        booleanEnum.put( Boolean.FALSE, falseEntry.getValue() );
        booleanEnum.put( Boolean.TRUE, trueEntry.getValue() );

        // TODO: if we ever support serialization of other string represantations as 'false' and 'true', we need
        // to introduce a special type handler for that (see ObservationFeatureFactory#typeHanderForComponent) and
        // we also need to give the string representations to the enumeration
        myRestrictions.add( new EnumerationRestriction( booleanEnum ) );
      }
      else if( hEnumerations.size() > 0 )
      {
        myRestrictions.add( new EnumerationRestriction( hEnumerations ) );
      }

      if( sPatterns.size() > 0 )
      {
        myRestrictions.add( new RegExpRestriction( sPatterns.toArray( new String[] {} ) ) );
      }
    }
    // from qname we can get the type handler via the registry
    return new RepresentationType( RepresentationType.KIND.valueOf( localName ), new QName( NS.XSD_SCHEMA, base ), unit, frame, myRestrictions.toArray( new IRestriction[] {} ), classification );
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#internalMarshall(java.lang.Object, org.w3c.dom.Document,
   *      java.net.URL)
   */
  @Override
  protected Node internalMarshall( final Object value, final Document document, final URL context )
  {
    // TODO: write restrictions

    final RepresentationType type = (RepresentationType) value;

    switch( type.getKind() )
    {
      case Boolean:
        return document.createElementNS( NS.SWE, "Boolean" ); //$NON-NLS-1$

      case Word:
      {
        final Element eltWord = document.createElementNS( NS.SWE, "Word" ); //$NON-NLS-1$

        if( type.getClassification().length() > 0 )
        {
          final Element eltClassification = document.createElementNS( NS.SWE, "classification" ); //$NON-NLS-1$
          eltWord.appendChild( eltClassification );
        }

        return eltWord;
      }

      case Number:
      {
        final Element eltNumber = document.createElementNS( NS.SWE, "Number" ); //$NON-NLS-1$

        RepresentationTypeHandler.handleUnit( type, document, eltNumber );
        RepresentationTypeHandler.handleFrame( type, document, eltNumber );

        return eltNumber;
      }

      case SimpleType:
      {
        final Element eltSimpleType = document.createElementNS( NS.SWE, "SimpleType" ); //$NON-NLS-1$

        final Element eltRestriction = document.createElementNS( NS.XST, "restriction" ); //$NON-NLS-1$
        eltRestriction.setAttribute( "base", type.getValueTypeName().getLocalPart() ); //$NON-NLS-1$
        eltSimpleType.appendChild( eltRestriction );

        RepresentationTypeHandler.handleUnit( type, document, eltSimpleType );
        RepresentationTypeHandler.handleFrame( type, document, eltSimpleType );

        return eltSimpleType;
      }

      default:
        throw new UnsupportedOperationException();
    }

  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#getValueClass()
   */
  @Override
  public Class< ? > getValueClass( )
  {
    return RepresentationType.class;
  }

  private static void handleUnit( final RepresentationType type, final Document doc, final Element elt )
  {
    final String unit = type.getUnit();
    if( unit != null && unit.length() > 0 )
    {
      final Element eltUom = doc.createElementNS( NS.GML3, "unitOfMeasure" ); //$NON-NLS-1$
      eltUom.setAttribute( "uom", unit ); //$NON-NLS-1$

      elt.appendChild( eltUom );
    }
  }

  private static void handleFrame( final RepresentationType type, final Document doc, final Element elt )
  {
    final String frame = type.getFrame();
    if( frame != null && frame.length() > 0 )
    {
      final Element eltFrame = doc.createElementNS( NS.SWE, "frame" ); //$NON-NLS-1$
      eltFrame.setAttribute( "frame", frame ); //$NON-NLS-1$

      elt.appendChild( eltFrame );
    }
  }
}
