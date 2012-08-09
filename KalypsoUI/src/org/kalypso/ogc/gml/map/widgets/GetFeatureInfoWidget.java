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

import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.swt.awt.SWT_AWT_Utilities;
import org.kalypso.contribs.java.net.UrlUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;
import org.kalypso.ogc.gml.map.widgets.dialogs.GetFeatureInfoDialog;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemePredicate;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ogc.gml.mapmodel.visitor.KalypsoThemeVisitor;
import org.kalypso.ogc.gml.widgets.AbstractWidget;

/**
 * This widget executes a feature info request.
 * 
 * @author Holger Albert
 */
public class GetFeatureInfoWidget extends AbstractWidget
{
  /**
   * The wms theme.
   */
  private KalypsoWMSTheme m_wmsTheme;

  /**
   * The constructor.
   */
  public GetFeatureInfoWidget( )
  {
    super( "GetFeatureInfo", Messages.getString( "GetFeatureInfoWidget_1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    m_wmsTheme = null;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.IMapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    initialize( mapPanel );
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked( final MouseEvent event )
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
      final String lastRequest = m_wmsTheme.getLastRequest();
      if( lastRequest == null || lastRequest.length() == 0 )
        throw new IllegalStateException( Messages.getString( "GetFeatureInfoWidget_2" ) ); //$NON-NLS-1$

      /* Get the parameter of the last request. */
      final URL lastRequestUrl = new URL( lastRequest );
      final Map<String, String> lastRequestParams = UrlUtilities.parseQuery( lastRequestUrl );

      /* Greate the additional parameters. */
      /* May be existing ones, which will be replaced. */
      final Map<String, String> parameters = new HashMap<String, String>();
      parameters.put( "REQUEST", "GetFeatureInfo" ); //$NON-NLS-1$ //$NON-NLS-2$
      parameters.put( "QUERY_LAYERS", lastRequestParams.get( "LAYERS" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      parameters.put( "X", String.format( Locale.PRC, "%.0f", event.getPoint().getX() ) ); //$NON-NLS-1$ //$NON-NLS-2$
      parameters.put( "Y", String.format( Locale.PRC, "%.0f", event.getPoint().getY() ) ); //$NON-NLS-1$ //$NON-NLS-2$
      parameters.put( "INFO_FORMAT", "text/html" ); //$NON-NLS-1$ //$NON-NLS-2$
      parameters.put( "VERSION", lastRequestParams.get( "VERSION" ) );

      /* Adjust the request. */
      final URL newRequestUrl = UrlUtilities.addQuery( lastRequestUrl, parameters );

      /* Create the dialog. */
      final GetFeatureInfoDialog dialog = new GetFeatureInfoDialog( SWT_AWT_Utilities.findActiveShell(), newRequestUrl );

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

          final org.eclipse.swt.graphics.Point shellSize = shell.getSize();
          final org.eclipse.swt.graphics.Point mousePos = shell.getDisplay().getCursorLocation();
          shell.setBounds( new Rectangle( mousePos.x, mousePos.y, shellSize.x, shellSize.y ) );
        }
      } );
    }
    catch( final MalformedURLException ex )
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

  private void initialize( final IMapPanel mapPanel )
  {
    try
    {
      /* Get the theme property. */
      final String themeProperty = getParameter( "getFeatureInfoProperty" ); //$NON-NLS-1$

      /* Get the map model. */
      final IMapModell mapModel = mapPanel.getMapModell();
      if( !(mapModel instanceof GisTemplateMapModell) )
        throw new IllegalStateException( Messages.getString( "GetFeatureInfoWidget_14" ) ); //$NON-NLS-1$

      /* Find the theme. */
      m_wmsTheme = findTheme( (GisTemplateMapModell) mapModel, themeProperty );
    }
    catch( final IllegalStateException ex )
    {
      ex.printStackTrace();
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
  private KalypsoWMSTheme findTheme( final GisTemplateMapModell mapModel, final String themeProperty ) throws IllegalStateException
  {
    if( !StringUtils.isEmpty( themeProperty ) )
      return findThemeWithProperty( mapModel, themeProperty );

    return findThemeByUserSelection( mapModel );
  }

  private KalypsoWMSTheme findThemeWithProperty( final GisTemplateMapModell mapModel, final String themeProperty )
  {
    final IKalypsoTheme[] themes = MapModellHelper.findThemeByProperty( mapModel, themeProperty, IKalypsoThemeVisitor.DEPTH_ZERO );
    if( themes == null || themes.length == 0 )
      throw new IllegalStateException( String.format( Messages.getString( "GetFeatureInfoWidget_15" ), themeProperty ) ); //$NON-NLS-1$

    return checkIfWmsTheme( themes[0] );
  }

  private KalypsoWMSTheme checkIfWmsTheme( final IKalypsoTheme theme ) throws IllegalStateException
  {
    if( theme instanceof KalypsoWMSTheme && theme.isVisible() )
      return (KalypsoWMSTheme) theme;

    if( theme instanceof IKalypsoCascadingTheme && theme.isVisible() )
      return findFirstVisibleWmsTheme( (IKalypsoCascadingTheme) theme );

    throw new IllegalStateException( Messages.getString( "GetFeatureInfoWidget_16" ) ); //$NON-NLS-1$
  }

  private KalypsoWMSTheme findFirstVisibleWmsTheme( final IKalypsoCascadingTheme cascadingTheme ) throws IllegalStateException
  {
    final IKalypsoTheme[] themes = cascadingTheme.getAllThemes();
    for( final IKalypsoTheme theme : themes )
    {
      if( theme instanceof KalypsoWMSTheme && theme.isVisible() )
        return (KalypsoWMSTheme) theme;
    }

    throw new IllegalStateException( Messages.getString( "GetFeatureInfoWidget_17" ) ); //$NON-NLS-1$
  }

  private KalypsoWMSTheme findThemeByUserSelection( final GisTemplateMapModell mapModel )
  {
    /* Find all wms themes. */
    final IKalypsoTheme[] wmsThemes = findWmsThemes( mapModel, IKalypsoThemeVisitor.DEPTH_INFINITE );

    /* Create the dialog. */
    final ListDialog dialog = new ListDialog( SWT_AWT_Utilities.findActiveShell() );
    dialog.setTitle( getName() );
    dialog.setMessage( "Select WMS theme" );
    dialog.setLabelProvider( new LabelProvider() );
    dialog.setContentProvider( new ArrayContentProvider() );

    /* Set the input. */
    dialog.setInput( wmsThemes );

    /* Open the dialog. */
    final int open = SWT_AWT_Utilities.openSwtWindow( dialog );
    if( open != Window.OK )
      return null;

    final Object[] result = dialog.getResult();

    return (KalypsoWMSTheme) result[0];
  }

  public IKalypsoTheme[] findWmsThemes( final IMapModell mapModel, final int depth )
  {
    final IKalypsoThemePredicate predicate = new IKalypsoThemePredicate()
    {
      @Override
      public boolean decide( final IKalypsoTheme theme )
      {
        if( theme instanceof KalypsoWMSTheme )
          return true;

        return false;
      }
    };

    final KalypsoThemeVisitor visitor = new KalypsoThemeVisitor( predicate );
    mapModel.accept( visitor, depth );
    return visitor.getFoundThemes();
  }
}