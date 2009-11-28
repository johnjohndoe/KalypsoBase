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
package org.kalypso.service.wps.utils.ogc;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * A prefix mapper for the namespaces.
 * 
 * @author Holger Albert
 */
public class OGCNamespacePrefixMapper extends NamespacePrefixMapper
{
  /**
   * The constructor.
   */
  public OGCNamespacePrefixMapper( )
  {
  }

  /**
   * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreferredPrefix(java.lang.String, java.lang.String,
   *      boolean)
   */
  @Override
  public String getPreferredPrefix( String namespaceUri, String suggestion, boolean requirePrefix )
  {
    /* Use xmlns-prefix for xmlns-Namespace. */
    if( "http://www.w3.org/2001/XMLSchema".equals( namespaceUri ) ) //$NON-NLS-1$
      return "xmlns"; //$NON-NLS-1$

    /* Use xlink-prefix for xlink-Namespace. */
    if( "http://www.w3.org/1999/xlink".equals( namespaceUri ) ) //$NON-NLS-1$
      return "xlink"; //$NON-NLS-1$

    /* Use wps-prefix for wps-Namespace. */
    if( "http://www.opengeospatial.net/wps".equals( namespaceUri ) ) //$NON-NLS-1$
      return "wps"; //$NON-NLS-1$

    /* Use ows-prefix for ows-Namespace. */
    if( "http://www.opengeospatial.net/ows".equals( namespaceUri ) ) //$NON-NLS-1$
      return "ows"; //$NON-NLS-1$

    return suggestion;
  }
}