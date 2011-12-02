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
package org.kalypso.contribs.eclipse.swt.widgets;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

/**
 * Puts an tooltip on a control which is always visible.
 * <p>
 * The tooltip string is read from the control's data
 * </p>
 * 
 * @author belger
 */
public class PersistentControlTooltipListener extends AbstractControlTooltipListener
{
  /**
   * Creates the listener and hooks it to the control.
   * 
   * @param alwaysVisible
   *          if true, the tooltip is always visible if the mouse touches the control, if false, we have the normal
   *          tooltip behaviour
   * @param dataKey
   *          key, from wich the tooltip string is retrieved, see
   *          {@link org.eclipse.swt.widgets.Widget#getData(java.lang.String)}
   */
  public final static void hookControl( final Control control, final boolean alwaysVisible, final String dataKey )
  {
    final AbstractControlTooltipListener tableListener = new PersistentControlTooltipListener( control.getShell(), alwaysVisible, dataKey );
    hookListener( control, tableListener );
  }

  /**
   * Like {@link #hookControl(Control, boolean, String)}, but the tooltip value is also set.
   */
  public final static void hookControl( final Control control, final boolean alwaysVisible, final String dataKey, final String tooltip )
  {
    control.setData( dataKey, tooltip );
    final AbstractControlTooltipListener tableListener = new PersistentControlTooltipListener( control.getShell(), alwaysVisible, dataKey );
    hookListener( control, tableListener );
  }

  private final String m_dataKey;

  public PersistentControlTooltipListener( final Shell shell, final boolean alwaysVisible, final String dataKey )
  {
    super( shell, alwaysVisible );
    m_dataKey = dataKey;
  }

  /**
   * @see org.kalypso.contribs.eclipse.swt.widgets.AbstractControlTooltipListener#getTooltipForEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  protected String getTooltipForEvent( final Event e )
  {
    final Object data = e.widget.getData( m_dataKey );

    return data == null ? null : data.toString();
  }

}
