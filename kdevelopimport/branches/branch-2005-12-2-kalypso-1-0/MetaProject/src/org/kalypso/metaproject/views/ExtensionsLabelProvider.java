/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.metaproject.views;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.kalypso.metaproject.ImageProvider;

/**
 * @author schlienger
 */
public class ExtensionsLabelProvider extends LabelProvider
{
  private final Image m_imgExt = ImageProvider.IMAGE_EXTENSION.createImage();
  private final Image m_imgExtPt = ImageProvider.IMAGE_EXTENSIONPOINT.createImage();
  private final Image m_imgPlugin = ImageProvider.IMAGE_PLUGIN.createImage();

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#dispose()
   */
  public void dispose()
  {
    m_imgPlugin.dispose();
    m_imgExt.dispose();
    m_imgExtPt.dispose();

    super.dispose();
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
   */
  public Image getImage( Object element )
  {
    if( element instanceof ExtensionsTreeContentProvider.NamespaceInfo )
      return m_imgPlugin;
    
    if( element instanceof IExtension || element instanceof ExtensionsTreeContentProvider.ExtensionInfo )
      return m_imgExt;

    if( element instanceof IExtensionPoint || element instanceof ExtensionsTreeContentProvider.ExtensionPointInfo )
      return m_imgExtPt;

    return super.getImage( element );
  }
}
