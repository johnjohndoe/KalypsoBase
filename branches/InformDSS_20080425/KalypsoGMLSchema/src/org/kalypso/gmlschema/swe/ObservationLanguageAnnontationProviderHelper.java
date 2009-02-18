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
package org.kalypso.gmlschema.swe;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.gmlschema.annotation.AnnotationAdapterFactory;
import org.kalypso.gmlschema.annotation.DefaultAnnotation;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;

/**
 * @author kuch
 */
public class ObservationLanguageAnnontationProviderHelper
{
  public static IAnnotation[] getAnnontations( final String key, final NodeList listAnnontation )
  {
    if( (listAnnontation == null) || (listAnnontation.getLength() == 0) )
    {
      return new IAnnotation[0];
    }

    final List<IAnnotation> list = new ArrayList<IAnnotation>();

    for( int i = 0; i < listAnnontation.getLength(); i++ )
    {
      final Node item = listAnnontation.item( i );
      if( item instanceof ElementNSImpl )
      {
        final NodeList languageNodes = item.getChildNodes();

        for( int ln = 0; ln < languageNodes.getLength(); ln++ )
        {
          final Node itmLang = languageNodes.item( ln );

          if( itmLang instanceof ElementNSImpl )
          {
            final DefaultAnnotation annontation = ObservationLanguageAnnontationProviderHelper.computeDefaultAnnontation( key, itmLang );

            list.add( annontation );
          }
        }
      }

      return list.toArray( new IAnnotation[] {} );
    }

    return list.toArray( new IAnnotation[] {} );
  }

  private static DefaultAnnotation computeDefaultAnnontation( final String key, final Node itmLang )
  {
    final NamedNodeMap attributes = itmLang.getAttributes();
    final Node langNode = attributes.getNamedItem( "xml:lang" );
    final String language = langNode.getTextContent();

    final DefaultAnnotation annotation = new DefaultAnnotation( language, key );

    final NodeList childNodes = itmLang.getChildNodes();
    for( int i = 0; i < childNodes.getLength(); i++ )
    {
      final Node item = childNodes.item( i );

      final String name = item.getLocalName();
      final String value = item.getTextContent();

      annotation.putValue( name, value );
    }

    return annotation;
  }

  public static IAnnotation getDefaultAnnontation( final String value )
  {
    final String lang = AnnotationAdapterFactory.getPlatformLang();

    return new DefaultAnnotation( lang, value );
  }

}
