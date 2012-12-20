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
package org.kalypso.ogc.sensor.view.observationDialog;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * ObservationViewerDialog
 * <p>
 * 
 * @author schlienger (24.05.2005)
 */
public class ObservationViewerDialog extends Dialog
{
  private static final String SETTINGS_BOUNDS = "dialogBounds"; //$NON-NLS-1$

  private static final String SETTINGS_VIEWER = "observationViewer"; //$NON-NLS-1$

  private final List<IObservationAction> m_actions = new ArrayList<>();

  private final boolean m_withHeader;

  protected Object m_input = null;

  private IDialogSettings m_settings;

  private Clipboard m_clipboard;

  private URL m_context;

  private ObservationViewer m_viewer;

  private int m_viewerStyle = SWT.NONE;

  private String m_windowTitle;

  public ObservationViewerDialog( final Shell parent, final boolean withHeaderForm )
  {
    super( parent );

    setShellStyle( getShellStyle() | SWT.RESIZE );

    m_withHeader = withHeaderForm;
  }

  public ObservationViewerDialog( final Shell parent )
  {
    this( parent, true );
  }

  public void setViewerStyle( final int viewerStyle )
  {
    m_viewerStyle = viewerStyle;
  }

  @Override
  protected final Control createDialogArea( final Composite parent )
  {
    final Composite composite = (Composite)super.createDialogArea( parent );
    composite.setLayout( new FillLayout() );

    m_clipboard = new Clipboard( parent.getDisplay() );

    ControlUtils.addDisposeListener( composite );

    final IDialogSettings viewerSettings = DialogSettingsUtils.getSection( m_settings, SETTINGS_VIEWER );
    final IObservationAction[] actions = m_actions.toArray( new IObservationAction[m_actions.size()] );
    m_viewer = new ObservationViewer( composite, m_viewerStyle, m_withHeader, actions, viewerSettings, m_clipboard );

    updateViewer();

    final String windowTitle = getWindowTitle();
    getShell().setText( windowTitle );

    return composite;
  }

  private String getWindowTitle( )
  {
    final String title = Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.0" ); //$NON-NLS-1$
    if( m_windowTitle == null )
      return title;

    return String.format( "%s - %s", title, m_windowTitle ); //$NON-NLS-1$
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

  public final Object getInput( )
  {
    if( m_viewer != null )
      return m_viewer.getInput();
    return m_input;
  }

  @Override
  protected IDialogSettings getDialogBoundsSettings( )
  {
    return DialogSettingsUtils.getSection( m_settings, SETTINGS_BOUNDS );
  }

  public void setDialogSettings( final IDialogSettings settings )
  {
    m_settings = settings;
  }

  public void addObservationAction( final IObservationAction action )
  {
    m_actions.add( action );
  }

  public void setWindowTitle( final String windowTitle )
  {
    m_windowTitle = windowTitle;
  }
}