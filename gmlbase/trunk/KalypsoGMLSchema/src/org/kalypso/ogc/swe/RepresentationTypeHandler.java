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
package org.kalypso.ogc.swe;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.types.SimpleDOMTypeHandler;
import org.kalypso.gmlschema.types.TypeRegistryException;
import org.w3c.dom.Node;

/**
 * @author schlienger
 */
public class RepresentationTypeHandler extends SimpleDOMTypeHandler
{
  public RepresentationTypeHandler(  )
  {
    super( "representation", new QName( NS.SWE, "RepresentationType"), false );
  }

  /**
   * @see org.kalypso.gmlschema.types.SimpleDOMTypeHandler#internalUnmarshall(org.w3c.dom.Node, java.lang.Object, org.kalypso.contribs.java.net.IUrlResolver)
   */
  @Override
  protected Object internalUnmarshall( final Node node, final IUrlResolver urlResolver ) throws TypeRegistryException
  {
    try
    {
      return RepresentationTypeUtilities.unmarshalRepresentationType( node );
    }
    catch( final GMLSchemaException e )
    {
      e.printStackTrace();
      
      throw new TypeRegistryException( e );
    }
  }
}
