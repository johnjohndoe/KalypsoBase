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
package org.kalypso.ui.editor.styleeditor.colorMapEntryTable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;

/**
 * Dialog for determining a color range.
 * 
 * @author Holger Albert
 */
public class GenerateColorRangeDialog extends Dialog
{
  /**
   * The color map entries.
   */
  private ColorMapEntry[] m_entries;

  /**
   * The raster color map editor composite.
   */
  private RasterColorMapEditorComposite m_rasterComponent;

  /**
   * The constructor.
   * 
   * @param shell
   *          The parent shell, or null to create a top-level shell.
   * @param entries
   *          The color map entries.
   */
  public GenerateColorRangeDialog( final Shell parentShell, final ColorMapEntry[] entries )
  {
    super( parentShell );

    m_entries = entries;
  }

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The object that returns the current parent shell.
   * @param entries
   *          The color map entries.
   */
  public GenerateColorRangeDialog( final IShellProvider parentShell, final ColorMapEntry[] entries )
  {
    super( parentShell );

    m_entries = entries;
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    /* Set the title. */
    getShell().setText( Messages.getString( "GenerateColorRangeDialog_0" ) ); //$NON-NLS-1$

    /* Create the main composite. */
    final Composite main = (Composite)super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the raster color map editor composite. */
    m_rasterComponent = new RasterColorMapEditorComposite( main, SWT.NONE, m_entries, null, null, false )
    {
      @Override
      protected void colorMapChanged( )
      {
        handleColorMapChanged();
      }
    };

    /* Set a layout data. */
    m_rasterComponent.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return main;
  }

  protected void handleColorMapChanged( )
  {
    try
    {
      /* Store the changed entries. */
      m_entries = m_rasterComponent.getColorMap();
    }
    catch( final CoreException e )
    {
      StatusDialog.open( getParentShell(), e.getStatus(), getParentShell().getText() );
    }
  }

  @Override
  protected void okPressed( )
  {
    if( m_entries.length > 50 )
    {
      final String dialogMessage = String.format( Messages.getString( "GenerateColorRangeDialog_1" ), 50 ); //$NON-NLS-1$
      final MessageDialog dialog = new MessageDialog( getShell(), getShell().getText(), null, dialogMessage, MessageDialog.WARNING, new String[] {
          Messages.getString( "GenerateColorRangeDialog_2" ), Messages.getString( "GenerateColorRangeDialog_3" ) }, 1 ); //$NON-NLS-1$ //$NON-NLS-2$
      if( dialog.open() == Window.CANCEL )
        return;
    }

    super.okPressed();
  }

  /**
   * This function returns the color map entries.
   * 
   * @return The color map entries.
   */
  public ColorMapEntry[] getEntries( )
  {
    return m_entries;
  }
}