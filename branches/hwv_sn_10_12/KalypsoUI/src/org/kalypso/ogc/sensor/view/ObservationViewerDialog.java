/*
 * --------------- Kalypso-Header --------------------------------------------
 *
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 *
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 *
 * and
 *
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact:
 *
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 *
 * ------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.sensor.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction;
import org.kalypso.ogc.sensor.view.observationDialog.ClipboardExportAction;
import org.kalypso.ogc.sensor.view.observationDialog.ClipboardImportAction;
import org.kalypso.ogc.sensor.view.observationDialog.IObservationAction;
import org.kalypso.ogc.sensor.view.observationDialog.NewIdealLanduseAction;
import org.kalypso.ogc.sensor.view.observationDialog.NewObservationAction;
import org.kalypso.ogc.sensor.view.observationDialog.RemoveObservationAction;

/**
 * ObservationViewerDialog
 * <p>
 * 
 * @author schlienger (24.05.2005)
 */
public class ObservationViewerDialog extends Dialog
{
  private ObservationViewer m_viewer;

  private URL m_context;

  private final boolean m_withHeader;

  private final int m_buttonControls;

  // button types are bitmask !
  public static final int NO_BUTTON = 0;

  public static final int BUTTON_NEW = 1;

  public static final int BUTTON_REMOVE = 2;

  public static final int BUTTON_NEW_IDEAL_LANDUSE = 32;

  public static final int BUTTON_EXEL_IMPORT = 4;

  public static final int BUTTON_EXEL_EXPORT = 8;

  private static final String SETTINGS_BOUNDS = "dialogBounds";

  private static final String SETTINGS_VIEWER = "observationViewer";

  private final String[] m_axisTypes;

  protected Object m_input = null;

  private IDialogSettings m_settings;

  private Clipboard m_clipboard;

  public ObservationViewerDialog( final Shell parent, final boolean withHeaderForm, final int buttonControls, final String[] axisTypes )
  {
    super( parent );

    setShellStyle( getShellStyle() | SWT.RESIZE );

    m_withHeader = withHeaderForm;
    m_buttonControls = buttonControls;
    m_axisTypes = axisTypes;
  }

  public ObservationViewerDialog( final Shell parent )
  {
    this( parent, true, NO_BUTTON, null );
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected final Control createDialogArea( final Composite parent )
  {
    final Composite composite = (Composite) super.createDialogArea( parent );
    // composite.setLayout( new FillLayout() );
    composite.setLayout( new GridLayout() );

    m_clipboard = new Clipboard( parent.getDisplay() );
    composite.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        handleDispose();
      }
    } );

    final IDialogSettings viewerSettings = PluginUtilities.getSection( m_settings, SETTINGS_VIEWER );
    final IObservationAction[] buttons = createButtonControls( m_clipboard );
    m_viewer = new ObservationViewer( composite, SWT.NONE, m_withHeader, buttons, viewerSettings );
    m_viewer.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    updateViewer();

    getShell().setText( Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.0" ) ); //$NON-NLS-1$

    return composite;
  }

  protected void handleDispose( )
  {
    if( m_clipboard != null )
    {
      m_clipboard.dispose();
      m_clipboard = null;
    }
  }

  public final void setContext( final URL context )
  {
    m_context = context;
    updateViewer();
  }

  public final void setInput( final Object newInput )
  {
    m_input = newInput;

    updateViewer();
  }

  private void updateViewer( )
  {
    if( m_viewer != null )
    {
      m_viewer.setContext( m_context );
      m_viewer.setInput( m_input, m_viewer.getShow() );
    }
  }

  private IObservationAction[] createButtonControls( final Clipboard clipboard )
  {
    final List<IObservationAction> result = new ArrayList<IObservationAction>();

    if( (m_buttonControls & BUTTON_REMOVE) == BUTTON_REMOVE )
      result.add( new RemoveObservationAction( this ) );

    if( (m_buttonControls & BUTTON_NEW) == BUTTON_NEW )
      result.add( new NewObservationAction( this ) );

    // FIXME: this specialized stuff has to be refaktored out of this general dialog!
    if( (m_buttonControls & BUTTON_NEW_IDEAL_LANDUSE) == BUTTON_NEW_IDEAL_LANDUSE )
      result.add( new NewIdealLanduseAction( this ) );

    if( (m_buttonControls & BUTTON_EXEL_IMPORT) == BUTTON_EXEL_IMPORT )
      result.add( new ClipboardImportAction( this, clipboard ) );

    if( (m_buttonControls & BUTTON_EXEL_EXPORT) == BUTTON_EXEL_EXPORT )
      result.add( new ClipboardExportAction( this, clipboard ) );

    return result.toArray( new AbstractObservationAction[result.size()] );
  }

  public final Object getInput( )
  {
    if( m_viewer != null )
      return m_viewer.getInput();
    return m_input;
  }

  public final String[] getAxisTypes( )
  {
    return m_axisTypes;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
   */
  @Override
  protected IDialogSettings getDialogBoundsSettings( )
  {
    return PluginUtilities.getSection( m_settings, SETTINGS_BOUNDS );
  }

  public void setDialogSettings( final IDialogSettings settings )
  {
    m_settings = settings;
  }

}
