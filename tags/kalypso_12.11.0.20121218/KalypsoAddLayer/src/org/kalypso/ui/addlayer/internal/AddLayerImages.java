/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.addlayer.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.ui.KalypsoAddLayerPlugin;

/**
 * Accessor to images for this plugin
 *
 * @author Gernot Belger
 */
public final class AddLayerImages
{
  public final static String ICON_REFRESH_CAPABILITIES = "icon/refreshCapabilities.gif"; //$NON-NLS-1$

  public final static String ICON_OPEN_CAPABILITIES = "icon/openCapabilities.gif"; //$NON-NLS-1$

  public final static ImageDescriptor getImageDescriptor( final String path )
  {
    return AbstractUIPlugin.imageDescriptorFromPlugin( KalypsoAddLayerPlugin.getId(), path );
  }
}
