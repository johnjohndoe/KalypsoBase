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
package org.kalypsodeegree_impl.model.geometry;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import ogc31.www.opengis.net.gml.Coord;
import ogc31.www.opengis.net.gml.CoordType;
import ogc31.www.opengis.net.gml.Coordinates;
import ogc31.www.opengis.net.gml.CoordinatesType;
import ogc31.www.opengis.net.gml.DirectPositionType;
import ogc31.www.opengis.net.gml.EnvelopeType;
import ogc31.www.opengis.net.gml.Pos;

import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.contribs.ogc31.KalypsoOGC31JAXBcontext;
import org.kalypso.contribs.org.xml.sax.DelegateXmlReader;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler2;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.gmlschema.types.UnmarshallResultEater;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * Factory class to wrap from binding geometries to GM_Object geometries and visa versa
 *
 * @author doemming
 */
public class AdapterBindingToValue_GML31 implements AdapterBindingToValue
{
  public AdapterBindingToValue_GML31( )
  {
    // do not instantiate
  }

  private GM_Envelope createGM_Envelope( final EnvelopeType bindingEnvelope )
  {
    final List<Coord> coord = bindingEnvelope.getCoord();
    final String bindingSrsName = bindingEnvelope.getSrsName();
    // REMARK: backwards compablity: use kalypso-srs if no srs was found
    final String srsName = bindingSrsName == null ? KalypsoDeegreePlugin.getDefault().getCoordinateSystem() : bindingSrsName;

    if( coord != null && !coord.isEmpty() )
    {
      final CoordType min = coord.get( 0 ).getValue();
      final CoordType max = coord.get( 1 ).getValue();
      final GM_Position minPos = createGM_Position( min );
      final GM_Position maxPos = createGM_Position( max );
      return GeometryFactory.createGM_Envelope( minPos, maxPos, srsName );
    }
    final Coordinates coordinates = bindingEnvelope.getCoordinates();
    if( coordinates != null )
    {

      final GM_Position[] positions = createGM_Positions( coordinates.getValue() );
      return GeometryFactory.createGM_Envelope( positions[0], positions[1], srsName );
    }

    // TODO coordinates

    final DirectPositionType lowerCorner = bindingEnvelope.getLowerCorner();
    final DirectPositionType upperCorner = bindingEnvelope.getUpperCorner();
    if( lowerCorner != null && upperCorner != null )
    {
      final List<Double> min = lowerCorner.getValue();
      final List<Double> max = upperCorner.getValue();
      final GM_Position minPos = GeometryFactory.createGM_Position( min.get( 0 ), min.get( 1 ) );
      final GM_Position maxPos = GeometryFactory.createGM_Position( max.get( 0 ), max.get( 1 ) );
      return GeometryFactory.createGM_Envelope( minPos, maxPos, srsName );
    }

    final List<Pos> pos = bindingEnvelope.getPos();
    if( pos != null && !pos.isEmpty() )
    {
      final List<Double> min = pos.get( 0 ).getValue().getValue();
      final List<Double> max = pos.get( 1 ).getValue().getValue();
      final GM_Position minPos = GeometryFactory.createGM_Position( min.get( 0 ), min.get( 1 ) );
      final GM_Position maxPos = GeometryFactory.createGM_Position( max.get( 0 ), max.get( 1 ) );
      return GeometryFactory.createGM_Envelope( minPos, maxPos, srsName );
    }

    throw new UnsupportedOperationException();
  }

  private GM_Position createGM_Position( final CoordType coord )
  {
    final double x = coord.getX().doubleValue();
    final double y = coord.getX().doubleValue();
    final BigDecimal z = coord.getZ();
    if( z == null )
      return GeometryFactory.createGM_Position( x, y );
    return GeometryFactory.createGM_Position( x, y, z.doubleValue() );
  }

  private static GM_Position[] createGM_Positions( final CoordinatesType coordinates )
  {
    final String coordinateSepearator = coordinates.getCs();
    final String tuppleSeparator = coordinates.getTs();
    final String decimal = coordinates.getDecimal();
    final String value = coordinates.getValue();

    final String[] tupples = StringUtilities.splitString( value, tuppleSeparator );
    final GM_Position[] result = new GM_Position[tupples.length];

    for( int i = 0; i < result.length; i++ )
    {
      final String[] coordinateSplit = StringUtilities.splitString( tupples[i], coordinateSepearator );
      final double[] pos = new double[coordinateSplit.length];
      for( int j = 0; j < pos.length; j++ )
      {
        final String coordinate = StringUtilities.replaceString( coordinateSplit[j], decimal, "." );
        pos[j] = Double.parseDouble( coordinate );
      }
      result[i] = GeometryFactory.createGM_Position( pos );
    }

    return result;
  }

  @Override
  public Object wrapFromBinding( final Object bindingGeometry, final Class< ? > geometryClass ) throws GM_Exception
  {
    if( bindingGeometry == null )
      return null;

    if( bindingGeometry instanceof JAXBElement )
      return wrapFromBinding( ((JAXBElement< ? >) bindingGeometry).getValue(), geometryClass );

    if( bindingGeometry instanceof EnvelopeType )
      return createGM_Envelope( (EnvelopeType) bindingGeometry );

    throw new UnsupportedOperationException( bindingGeometry.getClass().getName() + " is not supported" );
  }

  @Override
  public Object wrapFromNode( final Node node ) throws Exception
  {
    // Still used for RectifiedGridDomain and spatial-ops

    final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final QName nodeName = new QName( node.getNamespaceURI(), node.getLocalName() );
    final IMarshallingTypeHandler typeHandler = registry.getTypeHandlerForTypeName( nodeName );

    if( typeHandler instanceof IMarshallingTypeHandler2 )
    {
      final Object[] result = new Object[1];
      final UnmarshallResultEater eater = new UnmarshallResultEater()
      {
        @Override
        public void unmarshallSuccesful( final Object value )
        {
          result[0] = value;
        }
      };

      final DelegateXmlReader xmlReader = new DelegateXmlReader();
      final ContentHandler contentHandler = ((IMarshallingTypeHandler2) typeHandler).createContentHandler( xmlReader, null, eater );
      xmlReader.setContentHandler( contentHandler );

      final TransformerFactory tf = TransformerFactory.newInstance();
      final Transformer transformer = tf.newTransformer();

      transformer.transform( new DOMSource( node ), new SAXResult( xmlReader ) );
      return result[0];
    }

    /* Only used for envelope now */
    final Unmarshaller unmarshaller = KalypsoOGC31JAXBcontext.getContext().createUnmarshaller();
    final Object bindingGeometry = unmarshaller.unmarshal( node );

    return wrapFromBinding( bindingGeometry, null );
  }
}
