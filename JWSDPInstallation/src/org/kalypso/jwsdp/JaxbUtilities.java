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
package org.kalypso.jwsdp;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.kalypso.commons.xml.NSPrefixProvider;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * @author Belger
 */
public final class JaxbUtilities
{
  private JaxbUtilities( )
  {
    // never instantiate
  }

  /**
   * 
   */
  public static JAXBContext createQuiet( final Class... objectFactoryClasses )
  {
    try
    {
      // final Map<String, Object> map = new HashMap<String, Object>();
      // map.put( JAXBRIContext.DEFAULT_NAMESPACE_REMAP, "gistreeview.template.kalypso.org");
      return JAXBContext.newInstance( objectFactoryClasses );
    }
    catch( final JAXBException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private static NamespacePrefixMapper getNSPrefixMapper( )
  {
    return new NamespacePrefixMapper()
    {
      @Override
      public String getPreferredPrefix( String namespace, String suggestion, boolean required )
      {
        // never return null to avoid using of defaultnamespace witch leads to broken xml sometimes. see bug-description
        // at methode createMarshaller()
        final NSPrefixProvider nsProvider = NSPrefixProvider.getInstance();
        return nsProvider.getPreferredPrefix( namespace, suggestion );
      }
    };
  }

  /**
   * create marshaller with namespace prefixmapping<br>
   * use this methode to avoid bug in jaxb-implementation when prefixing attributes with defaultnamespace like this:
   * <code><element xmlns="www.xlink..." :href="test"/></code> instead if
   * <code><element xmlns:xlink="blabla" xlink:href="test"/></code>.
   */
  public static Marshaller createMarshaller( final JAXBContext context ) throws JAXBException
  {
    final Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper", getNSPrefixMapper() );
    return marshaller;
  }
  
  public static Validator createValidator( final JAXBContext context ) throws JAXBException
  {
    final Unmarshaller unmarshaller = context.createUnmarshaller();
    final Schema schema = unmarshaller.getSchema();
    if( schema == null )
      return null;
    
    return schema.newValidator();
  }
}
