/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * TODO: was not merges from 3.0 Version; check if everything is fine
 * 
 * Provides fake tooltips for control's which doesnt have one. For example TableItem or Tablecolumns in Tables.
 * 
 * @author belger
 */
public abstract class AbstractControlTooltipListener implements Listener
{
  protected static final void hookListener( final Widget widget, final AbstractControlTooltipListener tableListener )
  {
    widget.addListener( SWT.Dispose, tableListener );
    widget.addListener( SWT.KeyDown, tableListener );
    widget.addListener( SWT.MouseMove, tableListener );
    widget.addListener( SWT.MouseHover, tableListener );
  }

  private final Listener m_controlListener;

  protected final Shell m_shell;

  private final boolean m_isAlwaysVisible;

  protected String m_orgTooltip = null;

  protected AbstractControlTooltipListener( final Shell shell, final boolean alwaysVisible )
  {
    m_shell = shell;
    m_isAlwaysVisible = alwaysVisible;

    m_controlListener = new Listener()
    {
      @Override
      public void handleEvent( final Event event )
      {
        switch( event.type )
        {
          case SWT.MouseExit:
          // fall through
          case SWT.MouseMove:
            if( event.widget instanceof Control )
            {
              final Control control = (Control) event.widget;
              control.removeListener( SWT.MouseMove, this );
              control.removeListener( SWT.MouseExit, this );
              control.setToolTipText( m_orgTooltip );
            }
            break;
        }
      }
    };
  }

  /**
   * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void handleEvent( final Event event )
  {
    if( event.widget instanceof Control )
    {
      final Control control = (Control) event.widget;
      switch( event.type )
      {
        case SWT.Dispose:
        case SWT.KeyDown:
          break;

        case SWT.MouseMove:
          if( !m_isAlwaysVisible )
            break;

        case SWT.MouseHover:
        {
          final String tooltip = getTooltipForEvent( event );
          final String toolTipText = control.getToolTipText();

          if( tooltip != null )
          {
            if( !tooltip.equals( toolTipText ) )
            {
              control.setToolTipText( tooltip );
              m_orgTooltip = control.getToolTipText();
            }

            control.addListener( SWT.MouseMove, m_controlListener );
            control.addListener( SWT.MouseExit, m_controlListener );
          }
          else
          {
            control.setToolTipText( m_orgTooltip );
            m_orgTooltip = null;
          }
        }
      }
    }
  }

  protected abstract String getTooltipForEvent( final Event e );
}
