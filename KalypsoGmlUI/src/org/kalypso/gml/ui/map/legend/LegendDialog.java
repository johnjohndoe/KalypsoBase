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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.ThemeUtilities;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.NodeFactory;
import org.kalypso.ogc.gml.outline.nodes.NodeLegendBuilder;

/**
 * A legend dialog that shows the legend for a set of given themes.
 *
 * @author Holger Albert
 */
public class LegendDialog extends PopupDialog
{
  /**
   * The themes.
   */
  private final IKalypsoTheme[] m_themes;

  /**
   * The constructor.
   *
   * @param parentShell
   *          The parent shell.
   * @param themes
   *          The themes.
   */
  public LegendDialog( final Shell parentShell, final IKalypsoTheme[] themes )
  {
    this( parentShell, Messages.getString("LegendDialog_0"), themes ); //$NON-NLS-1$
  }

  /**
   * The constructor.
   *
   * @param parentShell
   *          The parent shell.
   * @param title
   *          The title for the legend.
   * @param themes
   *          The themes.
   */
  public LegendDialog( final Shell parentShell, final String title, final IKalypsoTheme[] themes )
  {
    super( parentShell, SWT.RESIZE, true, true, true, false, false, title, "" ); //$NON-NLS-1$

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
        setInfoText( Messages.getString("LegendDialog_2") ); //$NON-NLS-1$
        return main;
      }

      /* If there are no visible themes show only a notice. */
      final IKalypsoTheme theme = ThemeUtilities.findFirstVisible( m_themes );
      if( theme == null )
      {
        setInfoText( Messages.getString("LegendDialog_3") ); //$NON-NLS-1$
        return main;
      }

      /* Create the theme node. */
      final IThemeNode themeNode = NodeFactory.createNode( null, theme );

      /* Create the legend graphic. */
      final NodeLegendBuilder legendBuilder = new NodeLegendBuilder( null, true );
      final Image legendGraphic = legendBuilder.createLegend( new IThemeNode[] { themeNode }, getShell().getDisplay(), null );

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

      label.addDisposeListener( new DisposeListener()
      {
        @Override
        public void widgetDisposed( final DisposeEvent e )
        {
          legendGraphic.dispose();
        }
      } );

      return main;
    }
    catch( final Exception ex )
    {
      /* Log the error message. */
      KalypsoGmlUIPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), ex.getLocalizedMessage(), ex ) );

      /* Show the error message to the user. */
      setInfoText( String.format( Messages.getString("LegendDialog_4"), ex.getLocalizedMessage() ) ); //$NON-NLS-1$

      return main;
    }
  }
}