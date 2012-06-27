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
package org.kalypso.ui.addlayer.internal.wms;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.kalypso.ui.addlayer.internal.AddLayerImages;
import org.kalypso.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class LoadCapabilitiesAction extends Action
{
  private final ICapabilitiesData m_data;

  public LoadCapabilitiesAction( final ICapabilitiesData data )
  {
    m_data = data;

    setText( Messages.getString("LoadCapabilitiesAction_0") ); //$NON-NLS-1$
    setToolTipText( Messages.getString("LoadCapabilitiesAction_1") ); //$NON-NLS-1$
    setImageDescriptor( AddLayerImages.getImageDescriptor( AddLayerImages.ICON_REFRESH_CAPABILITIES ) );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    m_data.loadCapabilities();
  }
}