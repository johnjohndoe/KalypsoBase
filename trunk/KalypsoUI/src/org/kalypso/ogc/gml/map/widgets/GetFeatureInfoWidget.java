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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.swt.awt.SWT_AWT_Utilities;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;
import org.kalypso.ogc.gml.map.utilities.tooltip.ToolTipRenderer;
import org.kalypso.ogc.gml.map.widgets.dialogs.GetFeatureInfoDialog;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemePredicate;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ogc.gml.mapmodel.visitor.KalypsoThemeVisitor;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * This widget executes a feature info request.
 * 
 * @author Holger Albert
 */
public class GetFeatureInfoWidget extends AbstractWidget
{
  /**
   * The map model listener.
   */
  private final IMapModellListener m_listener = new MapModellAdapter()
  {
    @Override
    public void themeVisibilityChanged( final IMapModell source, final IKalypsoTheme theme, final boolean visibility )
    {
      handleThemeChanged( theme );
    }

    @Override
    public void themeRemoved( final IMapModell source, final IKalypsoTheme theme, final boolean lastVisibility )
    {
      handleThemeChanged( theme );
    }
  };

  /**
   * The tooltip renderer.
   */
  private final ToolTipRenderer m_toolTipRenderer;

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

    m_toolTipRenderer = new ToolTipRenderer();
    m_wmsTheme = null;
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget, org.kalypso.ogc.gml.map.IMapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    initialize( mapPanel );

    final IKalypsoLayerModell mapModell = mapPanel.getMapModell();
    mapModell.addMapModelListener( m_listener );
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
        /* Initialize from configuration or user selection. */
        initialize( getMapPanel() );

        /* HINT 1: If the theme property is not set, the dialog was open. */
        /* HINT 1: So do not proceed, regardless if there is a theme. */
        /* HINT 1: This prevents from opening the feature info right after the dialog was open. */
        final String themeProperty = getParameter( "getFeatureInfoProperty" ); //$NON-NLS-1$
        if( StringUtils.isEmpty( themeProperty ) )
          return;

        /* HINT 2: If the theme property is set, the widget should configure itself without a dialog. */
        /* HINT 2: So the user was not interupted, so only leave, if there is no theme. */
        /* HINT 2: If there is a theme, show the feature info. */
        if( m_wmsTheme == null )
          return;
      }

      /* Create the dialog. */
      final GetFeatureInfoDialog dialog = new GetFeatureInfoDialog( SWT_AWT_Utilities.findActiveShell(), m_wmsTheme, event.getPoint().getX(), event.getPoint().getY() );

      /* Open the dialog. */
      SWT_AWT_Utilities.openSwtWindow( dialog );

      /* Adjust the position of the dialog. */
      final Shell shell = dialog.getShell();
      shell.getDisplay().syncExec( new Runnable()
      {
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
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#keyPressed(java.awt.event.KeyEvent)
   */
  @Override
  public void keyPressed( final KeyEvent event )
  {
    /* Get the key code. */
    final int keyCode = event.getKeyCode();
    switch( keyCode )
    {
      case KeyEvent.VK_SPACE:
      {
        openDialog();
        break;
      }
    }
  }

  /**
   * @see org.kalypso.ogc.gml.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    super.paint( g );

    /* HINT: If the theme property is set, the widget should configure itself without a dialog. */
    final String themeProperty = getParameter( "getFeatureInfoProperty" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( themeProperty ) )
      return;

    /* Get the map panel. */
    final IMapPanel panel = getMapPanel();
    if( panel == null )
      return;

    /* Prepare the tooltip. */
    final java.awt.Rectangle bounds = panel.getScreenBounds();
    final String tooltip = m_wmsTheme == null ? Messages.getString( "GetFeatureInfoWidget.0" ) : String.format( Messages.getString( "GetFeatureInfoWidget.1" ), m_wmsTheme.getName().getValue() ); //$NON-NLS-1$ //$NON-NLS-2$

    /* Draw the tooltip. */
    m_toolTipRenderer.setTooltip( tooltip ); //$NON-NLS-1$
    m_toolTipRenderer.paintToolTip( new Point( 5, bounds.height - 5 ), g, bounds );
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
      m_wmsTheme = findTheme( (GisTemplateMapModell)mapModel, themeProperty );

      /* Repaint the map. */
      repaintMap();
    }
    catch( final IllegalStateException ex )
    {
      ex.printStackTrace();
      m_wmsTheme = null;
      repaintMap();
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
      return (KalypsoWMSTheme)theme;

    if( theme instanceof IKalypsoCascadingTheme && theme.isVisible() )
      return findFirstVisibleWmsTheme( (IKalypsoCascadingTheme)theme );

    throw new IllegalStateException( Messages.getString( "GetFeatureInfoWidget_16" ) ); //$NON-NLS-1$
  }

  private KalypsoWMSTheme findFirstVisibleWmsTheme( final IKalypsoCascadingTheme cascadingTheme ) throws IllegalStateException
  {
    final IKalypsoTheme[] themes = cascadingTheme.getAllThemes();
    for( final IKalypsoTheme theme : themes )
    {
      if( theme instanceof KalypsoWMSTheme && theme.isVisible() )
        return (KalypsoWMSTheme)theme;
    }

    throw new IllegalStateException( Messages.getString( "GetFeatureInfoWidget_17" ) ); //$NON-NLS-1$
  }

  private KalypsoWMSTheme findThemeByUserSelection( final GisTemplateMapModell mapModel )
  {
    /* Find all wms themes. */
    final IKalypsoTheme[] wmsThemes = findWmsThemes( mapModel, IKalypsoThemeVisitor.DEPTH_INFINITE );

    /* No wms themes available. */
    if( wmsThemes.length == 0 )
      return null;

    /* If there is only one wms theme available, we do not need to have selected one by the user. */
    if( wmsThemes.length == 1 )
      return (KalypsoWMSTheme)wmsThemes[0];

    /* Create the dialog. */
    final ListDialog dialog = new ListDialog( SWT_AWT_Utilities.findActiveShell() );
    dialog.setTitle( getName() );
    dialog.setMessage( Messages.getString( "GetFeatureInfoWidget.2" ) ); //$NON-NLS-1$
    dialog.setLabelProvider( new LabelProvider() );
    dialog.setContentProvider( new ArrayContentProvider() );

    /* Set the input. */
    dialog.setInput( wmsThemes );

    /* Preselect the first wms theme. */
    dialog.setInitialSelections( new IKalypsoTheme[] { wmsThemes[0] } );

    /* Open the dialog. */
    final int open = SWT_AWT_Utilities.openSwtWindow( dialog );
    if( open != Window.OK )
      return null;

    final Object[] result = dialog.getResult();
    if( result == null || result.length == 0 )
      return null;

    return (KalypsoWMSTheme)result[0];
  }

  private IKalypsoTheme[] findWmsThemes( final IMapModell mapModel, final int depth )
  {
    final IKalypsoThemePredicate predicate = new IKalypsoThemePredicate()
    {
      @Override
      public boolean decide( final IKalypsoTheme theme )
      {
        if( !theme.isVisible() )
          return false;

        if( theme instanceof KalypsoWMSTheme )
          return true;

        return false;
      }
    };

    final KalypsoThemeVisitor visitor = new KalypsoThemeVisitor( predicate );
    mapModel.accept( visitor, depth );
    return visitor.getFoundThemes();
  }

  private void openDialog( )
  {
    /* HINT: If the theme property is set, the widget should configure itself without a dialog. */
    final String themeProperty = getParameter( "getFeatureInfoProperty" ); //$NON-NLS-1$
    if( !StringUtils.isEmpty( themeProperty ) )
      return;

    /* Get the map panel. */
    final IMapPanel mapPanel = getMapPanel();
    if( mapPanel == null )
      return;

    /* Initialize. */
    initialize( mapPanel );
  }

  protected void handleThemeChanged( final IKalypsoTheme theme )
  {
    if( m_wmsTheme == null )
      return;

    if( !m_wmsTheme.equals( theme ) )
      return;

    m_wmsTheme = null;
  }
}