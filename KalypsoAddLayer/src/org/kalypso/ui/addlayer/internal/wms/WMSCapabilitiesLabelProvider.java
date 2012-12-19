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
package org.kalypso.ui.addlayer.internal.wms;

import org.apache.commons.lang3.StringUtils;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * @author doemming
 */
public class WMSCapabilitiesLabelProvider extends LabelProvider
{
  @Override
  public String getText( final Object element )
  {
    if( element instanceof WMSCapabilities )
    {
      final WMSCapabilities caps = (WMSCapabilities) element;

      final String version = caps.getVersion();
      final String title = caps.getServiceIdentification().getTitle();

      final StringBuffer result = new StringBuffer();
      result.append( "WMS " ); //$NON-NLS-1$
      result.append( "(" + version + ") " ); //$NON-NLS-1$ //$NON-NLS-2$
      result.append( title );

      return result.toString();
    }

    if( element instanceof Layer )
    {
      final Layer layer = (Layer) element;

      final String title = layer.getTitle();

      final String description = layer.getAbstract();

      if( StringUtils.isBlank( description ) )
        return title;

      return String.format( "%s (%s)", title, description ); //$NON-NLS-1$
    }

    return super.getText( element );
  }
}
