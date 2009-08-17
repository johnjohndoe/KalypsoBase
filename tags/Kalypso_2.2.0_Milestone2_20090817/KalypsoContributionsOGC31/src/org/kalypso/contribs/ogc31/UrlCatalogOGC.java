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
package org.kalypso.contribs.ogc31;

import java.net.URL;
import java.util.Map;

import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.AbstractUrlCatalog;

/**
 * this catalog resolves all schemas that are original provided by ogc or very close to these
 * 
 * @author doemming
 */
public class UrlCatalogOGC extends AbstractUrlCatalog
{
  @Override
  protected void fillCatalog( final Class<?> myClass, final Map<String, URL> catalog, Map<String, String> prefixes )
  {
    // SWE & OM things
    catalog.put( NS.SWE, myClass.getResource( "schemata/sweCommon/1.0.30/swe.xsd" ) );
    prefixes.put( NS.SWE, "swe" );

    catalog.put( NS.ST, myClass.getResource( "schemata/sweCommon/1.0.30/simpleTypeDerivation.xsd" ) );
    prefixes.put( NS.ST, "st" );

    catalog.put( NS.OM, myClass.getResource("schemata/om/1.0.30/observation.xsd") );
    prefixes.put( NS.OM, "om" );
  }
}
