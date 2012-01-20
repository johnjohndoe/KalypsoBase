/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.NodeFactory;
import org.kalypso.util.themes.ThemeUtilities;

/**
 * A legend dialog that shows the legend for a set of given themes.
 *
 * @author Holger Albert
 */
public class LegendDialog extends PopupDialog
{
  private final IKalypsoTheme[] m_themes;

  public LegendDialog( final Shell parentShell, final IKalypsoTheme[] themes )
  {
    super( parentShell, SWT.RESIZE, true, true, true, false, false, "Legende", StringUtils.EMPTY );

    m_themes = themes;
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    /* Create the main composite. */
    final Composite main = (Composite) super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    try
    {
      /* If there are no themes show only a notice. */
      if( m_themes == null || m_themes.length == 0 )
      {
        setInfoText( "Es sind keine Themen in der Karte vorhanden..." );
        return main;
      }

      /* If there are no visible themes show only a notice. */
      final IKalypsoTheme theme = ThemeUtilities.findFirstVisible( m_themes );
      if( theme == null )
      {
        setInfoText( "Es ist kein Thema sichtbar..." );
        return main;
      }

      /* Create the theme node. */
      final IThemeNode themeNode = NodeFactory.createNode( null, theme );

      /* This font will be used to generate the legend. */
      final Font font = new Font( getShell().getDisplay(), JFaceResources.DIALOG_FONT, 10, SWT.NORMAL );

      /* Create the legend graphic. */
      final Image legendGraphic = themeNode.getLegendGraphic( null, true, font );

      /* Set the background image. */
      final Label label = new Label( main, SWT.NONE );
      label.setLayoutData( new GridData( SWT.FILL, SWT.FILL, false, false ) );
      label.setImage( legendGraphic );
      label.addPaintListener( new PaintListener()
      {
        @Override
        public void paintControl( final PaintEvent e )
        {
          final Label source = (Label) e.getSource();
          final Rectangle bounds = source.getBounds();
          e.gc.drawRectangle( new Rectangle( 0, 0, bounds.width - 1, bounds.height - 1 ) );
        }
      } );

      return main;
    }
    catch( final Exception ex )
    {
      /* Log the error message. */
      final Status status = new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), ex.getLocalizedMessage(), ex );
      KalypsoGmlUIPlugin.getDefault().getLog().log( status );

      /* Show the error message to the user. */
      setInfoText( String.format( "Fehler: %s", ex.getLocalizedMessage() ) );

      return main;
    }
  }
}