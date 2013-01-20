/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.afgui.internal.scenario;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.views.ScenarioContentProvider;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;

import de.renew.workflow.connector.cases.IScenario;

/**
 * A popup dialog that lets select the user one scenario.
 * 
 * @author Gernot Belger
 */
public class ScenarioSelectionPopup extends PopupDialog
{
  private final IDialogSettings m_settings = DialogSettingsUtils.getDialogSettings( KalypsoAFGUIFrameworkPlugin.getDefault(), getClass().getName() );

  private final IProject m_project;

  private final Point m_initialLocation;

  private ISelectionChangedListener m_selectionListener;

  private IOpenListener m_openListener;

  private TreeViewer m_viewer;

  public ScenarioSelectionPopup( final Shell parent, final IProject project, final String title, final Point initialLocation )
  {
    super( parent, SWT.RESIZE, true, true, false, false, false, title, null );

    m_project = project;
    m_initialLocation = initialLocation;
  }

  public void setSelectionListener( final ISelectionChangedListener selectionListener )
  {
    m_selectionListener = selectionListener;

    if( m_selectionListener != null )
      m_viewer.addPostSelectionChangedListener( selectionListener );
  }

  public void setOpenListener( final IOpenListener openListener )
  {
    m_openListener = openListener;

    if( m_openListener != null )
      m_viewer.addOpenListener( openListener );
  }

  @Override
  protected Point getInitialLocation( final Point initialSize )
  {
    if( m_initialLocation != null )
      return m_initialLocation;

    return super.getInitialLocation( initialSize );
  }

  @Override
  protected IDialogSettings getDialogSettings( )
  {
    return m_settings;
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    m_viewer = new TreeViewer( parent, SWT.NONE );

    m_viewer.setContentProvider( new ScenarioContentProvider( false ) );
    m_viewer.setLabelProvider( WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider() );
    m_viewer.setAutoExpandLevel( 2 );

    m_viewer.setInput( m_project );

    setSelectionListener( m_selectionListener );
    setOpenListener( m_openListener );

    return m_viewer.getControl();
  }

  public IScenario getSelectedScenario( )
  {
    return null;
  }
}