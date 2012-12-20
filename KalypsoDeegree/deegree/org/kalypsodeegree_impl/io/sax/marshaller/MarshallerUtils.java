/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypsodeegree_impl.io.sax.marshaller;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.transformation.CRSHelper;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Common helper code for sax marshelling.
 *
 * @author Felipe Maximino
 */
public final class MarshallerUtils
{
  private MarshallerUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  public static AttributesImpl createSrsAttributes( final String srsName, final int srsDimension )
  {
    final AttributesImpl atts = new AttributesImpl();
    addSrsNameAttributes( atts, srsName );
    addSrsDimensionAttributes( atts, srsDimension );
    return atts;
  }

  public static void addSrsNameAttributes( final AttributesImpl atts, final String crs )
  {
    atts.addAttribute( "", "srsName", "srsName", "anyURI", crs );
  }

  public static void addSrsDimensionAttributes( final AttributesImpl atts, final int srsDimension )
  {
    atts.addAttribute( "", "srsDimension", "srsDimension", "positiveInteger", Integer.toString( srsDimension ) );
  }

  public static void addCrsAttributesWSrsDimension( final AttributesImpl atts, final String srsName )
  {
    addSrsNameAttributes( atts, srsName );

    final int srsDimension = CRSHelper.getDimension( srsName );
    addSrsDimensionAttributes( atts, srsDimension );
  }

  public static void addSrsAttributes( final AttributesImpl atts, final String srsName, final int srsDimension )
  {
    if( !StringUtils.isBlank( srsName ) )
      addSrsNameAttributes( atts, srsName );

    if( srsDimension > 0 )
      addSrsDimensionAttributes( atts, srsDimension );
  }
}
