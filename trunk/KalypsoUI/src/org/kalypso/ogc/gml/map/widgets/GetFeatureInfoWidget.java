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

import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.swt.awt.SWT_AWT_Utilities;
import org.kalypso.contribs.java.net.UrlUtilities;
import org.kalypso.ogc.gml.AbstractCascadingLayerTheme;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;
import org.kalypso.ogc.gml.map.widgets.dialogs.GetFeatureInfoDialog;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ogc.gml.widgets.AbstractWidget;

/**
 * This widget executes a feature info request.
 * 
 * @author Holger Albert
 */
public class GetFeatureInfoWidget extends AbstractWidget
{
  /**
   * The property of the theme, with with we want to do the get feature info request.
   */
  private String m_themeProperty;

  /**
   * The wms theme.
   */
  private KalypsoWMSTheme m_wmsTheme;

  /**
   * The constructor.
   */
  public GetFeatureInfoWidget( )
  {
    super( "GetFeatureInfo", "Executes a GetFeatureInfoRequest" );

    m_themeProperty = null;
    m_wmsTheme = null;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.IMapPanel)
   */
  @Override
  public void activate( ICommandTarget commandPoster, IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    initialize( mapPanel );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#leftClicked(java.awt.Point)
   */
  @Override
  public void leftClicked( Point p )
  {
    try
    {
      /* We need a wms theme. */
      if( m_wmsTheme == null )
      {
        initialize( getMapPanel() );

        if( m_wmsTheme == null )
          return;
      }

      /* Get the last request. */
      String lastRequest = m_wmsTheme.getLastRequest();
      if( lastRequest == null || lastRequest.length() == 0 )
        throw new IllegalStateException( "Das WMS Thema ist inaktiv..." );

      /* Get the parameter of the last request. */
      URL lastRequestUrl = new URL( lastRequest );
      Map<String, String> lastRequestParams = UrlUtilities.parseQuery( lastRequestUrl );

      /* Greate the additional parameters. */
      /* May be existing ones, which will be replaced. */
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put( "REQUEST", "GetFeatureInfo" );
      parameters.put( "QUERY_LAYERS", lastRequestParams.get( "LAYERS" ) );
      parameters.put( "X", String.format( Locale.PRC, "%.0f", p.getX() ) );
      parameters.put( "Y", String.format( Locale.PRC, "%.0f", p.getY() ) );
      parameters.put( "INFO_FORMAT", "text/html" );

      /* Adjust the request. */
      URL newRequestUrl = UrlUtilities.addQuery( lastRequestUrl, parameters );

      /* Create the dialog. */
      GetFeatureInfoDialog dialog = new GetFeatureInfoDialog( SWT_AWT_Utilities.findActiveShell(), newRequestUrl );

      /* Open the dialog. */
      SWT_AWT_Utilities.openSwtWindow( dialog );

      /* Adjust the position of the dialog. */
      final Shell shell = dialog.getShell();
      shell.getDisplay().syncExec( new Runnable()
      {
        /**
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run( )
        {
          if( shell.isDisposed() )
            return;

          org.eclipse.swt.graphics.Point shellSize = shell.getSize();
          org.eclipse.swt.graphics.Point mousePos = shell.getDisplay().getCursorLocation();
          shell.setBounds( new Rectangle( mousePos.x, mousePos.y, shellSize.x, shellSize.y ) );
        }
      } );
    }
    catch( MalformedURLException ex )
    {
      ex.printStackTrace();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#finish()
   */
  @Override
  public void finish( )
  {
    m_wmsTheme = null;

    super.finish();
  }

  private void initialize( IMapPanel mapPanel )
  {
    try
    {
      /* Get the theme property. */
      m_themeProperty = getParameter( "getFeatureInfoProperty" );

      /* Get the map model. */
      IMapModell mapModel = mapPanel.getMapModell();
      if( !(mapModel instanceof GisTemplateMapModell) )
        throw new IllegalStateException( "Keine gültige Karte gefunden..." );

      /* Find the theme. */
      IKalypsoTheme theme = findTheme( (GisTemplateMapModell) mapModel );

      /* Check, if it is a WMS theme. */
      m_wmsTheme = checkIfWmsTheme( theme );
    }
    catch( IllegalStateException ex )
    {
      ex.printStackTrace();
      m_themeProperty = null;
      m_wmsTheme = null;
    }
  }

  /**
   * This function searches the map model for a {@link IKalypsoTheme} with the theme property set.
   * 
   * @param mapModel
   *          The map model.
   * @return The {@link IKalypsoTheme} or null.
   */
  private IKalypsoTheme findTheme( GisTemplateMapModell mapModel ) throws IllegalStateException
  {
    IKalypsoTheme[] themes = MapModellHelper.findThemeByProperty( mapModel, m_themeProperty, IKalypsoThemeVisitor.DEPTH_ZERO );
    if( themes == null || themes.length == 0 )
      throw new IllegalStateException( String.format( "Es wurde kein Thema mit der Eigenschaft '%s' in der aktiven Karte gefunden...", m_themeProperty ) );

    return themes[0];
  }

  private KalypsoWMSTheme checkIfWmsTheme( IKalypsoTheme theme ) throws IllegalStateException
  {
    if( theme instanceof KalypsoWMSTheme && theme.isVisible() )
      return (KalypsoWMSTheme) theme;

    if( theme instanceof AbstractCascadingLayerTheme && theme.isVisible() )
      return findFirstVisibleWmsTheme( (AbstractCascadingLayerTheme) theme );

    throw new IllegalStateException( "Kein (sichtbares) WMS Thema gefunden..." );
  }

  private KalypsoWMSTheme findFirstVisibleWmsTheme( AbstractCascadingLayerTheme cascadingTheme ) throws IllegalStateException
  {
    IKalypsoTheme[] themes = cascadingTheme.getAllThemes();
    for( IKalypsoTheme theme : themes )
    {
      if( theme instanceof KalypsoWMSTheme && theme.isVisible() )
        return (KalypsoWMSTheme) theme;
    }

    throw new IllegalStateException( "Kein (sichtbares) WMS Thema gefunden..." );
  }
}