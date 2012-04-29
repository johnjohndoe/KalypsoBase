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

import org.apache.commons.lang3.ArrayUtils;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * content provider for WMS capabilities
 *
 * @author doemming
 */
class WMSCapabilitiesContentProvider implements ITreeContentProvider
{
  @Override
  public Object[] getElements( final Object inputElement )
  {
    return getChildren( inputElement );
  }

  @Override
  public void dispose( )
  {
    // nothing to dispose
  }

  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
  }

  @Override
  public Object[] getChildren( final Object parentElement )
  {
    if( parentElement instanceof WMSCapabilities )
      return new Layer[] { ((WMSCapabilities) parentElement).getLayer() };

    if( parentElement instanceof Layer )
      return ((Layer) parentElement).getLayer();

    return ArrayUtils.EMPTY_OBJECT_ARRAY;
  }

  @Override
  public Object getParent( final Object element )
  {
    return null; // don't known
  }

  @Override
  public boolean hasChildren( final Object element )
  {
    return getChildren( element ).length > 0;
  }
}
