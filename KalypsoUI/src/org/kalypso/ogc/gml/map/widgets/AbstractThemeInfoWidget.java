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
package org.kalypso.ogc.gml.map.widgets;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Formatter;

import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeInfo;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;

/**
 * Abstract implementation of the ThemeInfoWidget; can be used to include the info-widget functionality in your own
 * widget.<br>
 * The only difference to the default implementation is, that this widget does not know which themes to display info
 * for.
 * 
 * @author Gernot Belger
 */
public abstract class AbstractThemeInfoWidget extends AbstractWidget
{
  private final ToolTipRenderer m_tooltipRenderer = new ToolTipRenderer();

  private String m_noThemesTooltip = Messages.getString( "org.kalypso.ogc.gml.map.widgets.AbstractThemeInfoWidget.0" ); //$NON-NLS-1$

  private IKalypsoTheme[] m_themes = null;

  private Point m_point = null;

  public AbstractThemeInfoWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  public void setThemes( final IKalypsoTheme[] themes )
  {
    m_themes = themes;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#moved(java.awt.Point)
   */
  @Override
  public void moved( final Point p )
  {
    if( getMapPanel() == null )
      return;

    m_point = p;

    // May happen, if called from selection change
    if( m_point == null )
      return;

    final GM_Point location = MapUtilities.transform( getMapPanel(), p );
    final GM_Position position = location.getPosition();

    final String tooltip = formatInfo( position );

    m_tooltipRenderer.setTooltip( tooltip );

    getMapPanel().repaintMap();
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    if( m_point == null )
      return;

    m_tooltipRenderer.paintToolTip( m_point, g, getMapPanel().getScreenBounds() );
  }

  protected Point getCurrentPoint( )
  {
    return m_point;
  }

  /** Creates the tooltip text for the given themes. */
  private String formatInfo( final GM_Position position )
  {
    if( m_themes == null || m_themes.length == 0 )
      return m_noThemesTooltip;

    final StringBuffer sb = new StringBuffer();
    final Formatter formatter = new Formatter( sb );

    final String headInfo = m_themes.length == 1 ? "" : "'%s': "; //$NON-NLS-1$ //$NON-NLS-2$

    for( final IKalypsoTheme theme : m_themes )
    {
      formatter.format( headInfo, theme.getName().getValue() );

      final IKalypsoThemeInfo themeInfo = (IKalypsoThemeInfo) theme.getAdapter( IKalypsoThemeInfo.class );
      if( themeInfo == null )
        formatter.format( Messages.getString( "org.kalypso.ogc.gml.map.widgets.AbstractThemeInfoWidget.3" ) ); //$NON-NLS-1$
      else
        themeInfo.appendQuickInfo( formatter, position );

      formatter.format( "%n" ); //$NON-NLS-1$
    }

    formatter.close();

    final String info = sb.toString().trim();

    return info.length() > 0 ? info : null;
  }

  /**
   * Sets the tooltip which will be shown, if no themes are available.
   */
  public void setNoThemesTooltip( final String noThemesTooltip )
  {
    m_noThemesTooltip = noThemesTooltip;
  }

}
