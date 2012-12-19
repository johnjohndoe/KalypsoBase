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
package org.kalypso.gml.ui.map.legend;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoCascadingTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemeVisitor;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;

/**
 * This handler opens the legend of the map.<br/>
 * Optionally, only the legend of a given theme is shown.
 * 
 * @author Holger Albert
 */
public class OpenLegendHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event )
  {
    /* Get the evaluation context. */
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    /* Get the shell. */
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );

    try
    {
      /* Get the map panel. */
      final IMapPanel mapPanel = MapHandlerUtils.getMapPanelChecked( context );

      /* Get the map model. */
      final IMapModell mapModel = mapPanel.getMapModell();

      /* Find the themes. */
      final String themeProperty = ObjectUtils.toString( context.getVariable( "themeProperty" ) ); //$NON-NLS-1$
      final IKalypsoTheme[] themes = findThemesForLegend( mapModel, themeProperty );
      if( themes == null )
        throw new Exception( Messages.getString("OpenLegendHandler_0") ); //$NON-NLS-1$

      /* Create the dialog. */
      final LegendDialog dialog = new LegendDialog( shell, themes );

      /* Open the dialog. */
      dialog.open();

      /* Adjust the position of the dialog. */
      final Shell dialogShell = dialog.getShell();
      final Point shellSize = dialogShell.getSize();
      final Point mousePos = dialogShell.getDisplay().getCursorLocation();
      dialogShell.setBounds( new Rectangle( mousePos.x, mousePos.y, shellSize.x, shellSize.y ) );

      return null;
    }
    catch( final Exception ex )
    {
      /* Create a status. */
      final IStatus status = new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), ex.getLocalizedMessage(), ex );

      /* Log the error message. */
      KalypsoGmlUIPlugin.getDefault().getLog().log( status );

      /* Show an error, if the operation has failed. */
      ErrorDialog.openError( shell, Messages.getString("OpenLegendHandler_1"), Messages.getString("OpenLegendHandler_2"), status ); //$NON-NLS-1$ //$NON-NLS-2$

      return null;
    }
  }

  private IKalypsoTheme[] findThemesForLegend( final IMapModell mapModel, final String property )
  {
    if( StringUtils.isBlank( property ) )
      return mapModel.getAllThemes();

    final IKalypsoCascadingTheme cascadingTheme = findCascadingTheme( mapModel, property );
    if( cascadingTheme == null )
      return null;

    return cascadingTheme.getAllThemes();
  }

  /**
   * This function searches the map model for a {@link IKalypsoCascadingTheme} with the given property set.
   * 
   * @param mapModel
   *          The map model.
   * @param property
   *          The property.
   * @return The {@link IKalypsoCascadingTheme} or <code>null</code>.
   */
  public static IKalypsoCascadingTheme findCascadingTheme( final IMapModell mapModel, final String property )
  {
    final IKalypsoTheme[] themes = MapModellHelper.findThemeByProperty( mapModel, property, IKalypsoThemeVisitor.DEPTH_ZERO );
    if( themes == null || themes.length == 0 )
      return null;

    final IKalypsoTheme theme = themes[0];
    if( !(theme instanceof IKalypsoCascadingTheme) )
      return null;

    return (IKalypsoCascadingTheme) theme;
  }
}