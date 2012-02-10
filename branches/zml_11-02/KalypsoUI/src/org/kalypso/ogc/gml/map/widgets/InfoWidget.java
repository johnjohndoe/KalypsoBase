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
package org.kalypso.ogc.gml.map.widgets;

import java.util.ArrayList;
import java.util.Collection;

import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.IMapModell;

/**
 * @author Gernot Belger
 */
public class InfoWidget extends AbstractThemeInfoWidget
{
  private static final IKalypsoTheme[] EMPTY_THEMES = new IKalypsoTheme[0];

  private static final String THEME_PROPERTY_INFO = "infoWidget.showInfo";

  public InfoWidget( )
  {
    super( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.0" ), Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    setNoThemesTooltip( "No Info-Themes configured" );
  }

  public InfoWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.MapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    final IKalypsoTheme[] infoThemes = findInfoThemes( mapPanel );
    setThemes( infoThemes );
  }

  private IKalypsoTheme[] findInfoThemes( final IMapPanel mapPanel )
  {
    if( mapPanel == null )
      return EMPTY_THEMES;

    final IMapModell mapModell = mapPanel.getMapModell();
    if( mapModell == null )
      return EMPTY_THEMES;

    final Collection<IKalypsoTheme> themes = new ArrayList<IKalypsoTheme>();

    final IKalypsoTheme[] allThemes = mapModell.getAllThemes();
    for( final IKalypsoTheme theme : allThemes )
    {
      final String property = theme.getProperty( THEME_PROPERTY_INFO, "false" );
      if( Boolean.parseBoolean( property ) )
        themes.add( theme );
    }

    return themes.toArray( new IKalypsoTheme[themes.size()] );
  }
}