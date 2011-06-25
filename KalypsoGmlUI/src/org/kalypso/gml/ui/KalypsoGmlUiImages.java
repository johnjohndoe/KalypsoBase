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
package org.kalypso.gml.ui;

import org.kalypso.commons.eclipse.core.runtime.PluginImageProvider.ImageKey;

/**
 * Utility class for handling images in this plugin.
 * 
 * @author belger
 */
public class KalypsoGmlUiImages
{
  public static enum DESCRIPTORS implements ImageKey
  {
    COVERAGE_ADD("icons/cview16/addCoverage.gif"), //$NON-NLS-1$
    COVERAGE_REMOVE("icons/cview16/removeCoverage.gif"), //$NON-NLS-1$
    COVERAGE_EXPORT("icons/cview16/exportCoverage.gif"), //$NON-NLS-1$
    COVERAGE_UP("icons/cview16/upCoverage.gif"), //$NON-NLS-1$
    COVERAGE_DOWN("icons/cview16/downCoverage.gif"), //$NON-NLS-1$
    COVERAGE_JUMP("icons/cview16/jumptoCoverage.gif"), //$NON-NLS-1$
    STYLE_EDIT("icons/cview16/style_edit.gif"), //$NON-NLS-1$

    SHAPE_FILE_NEW_ADD_FIELD("icons/obj16/shapeFileNew_addField.gif"), //$NON-NLS-1$
    SHAPE_FILE_NEW_REMOVE_FIELD("icons/obj16/shapeFileNew_removeField.gif"), //$NON-NLS-1$
    TOOLS_WIZ("icons/obj16/tools_64.png"); //$NON-NLS-1$

    private final String m_imagePath;

    private DESCRIPTORS( final String imagePath )
    {
      m_imagePath = imagePath;
    }

    /**
     * @see org.kalypso.commons.eclipse.core.runtime.PluginImageProvider.ImageKey#getImagePath()
     */
    @Override
    public String getImagePath( )
    {
      return m_imagePath;
    }
  }
}