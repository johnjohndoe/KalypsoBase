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
package org.kalypso.project.database.client.ui.composites;

import java.awt.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.contribs.eclipse.swt.canvas.DefaultContentArea;
import org.kalypso.contribs.eclipse.swt.canvas.ImageCanvas2;
import org.kalypso.module.IKalypsoModule;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;

/**
 * FIXME: this does not belong into KalypsoBase.<br/>
 * We need an (extension?) mechanism to inject the additional functionality of the project database into the welcome
 * page: two ideas/strategies to do this: 1) use actions instead of fixed wizard 2) create an extension point mechanism
 * that provides the IProjectHandles (so only the Project database may provide remote projects)
 * 
 * @author Dirk Kuch
 */
public class ModulePageComposite extends Composite
{
  // FIXME: never disposed!
  private static final Color COLOR_BOX = new Color( null, 0x7f, 0xb2, 0x99 );

  private final FormToolkit m_toolkit;

  private final IKalypsoModule m_module;

  public ModulePageComposite( final IKalypsoModule module, final FormToolkit toolkit, final Composite parent, final int style )
  {
    super( parent, style );

    m_module = module;
    m_toolkit = toolkit;

    final GridLayout layout = new GridLayout( 2, false );
    layout.horizontalSpacing = 100;
    layout.verticalSpacing = 25;
    layout.marginWidth = 75;

    this.setLayout( layout );

    createControl();
  }

  private void createControl( )
  {
    /* header */
    // icon / button
    final ImageCanvas2 headerCanvas = new ImageCanvas2( this, SWT.NO_REDRAW_RESIZE );
    final GridData headerIconData = new GridData( GridData.FILL, GridData.FILL, true, false, 2, 0 );
    headerIconData.heightHint = headerIconData.minimumHeight = 110;
    headerCanvas.setLayoutData( headerIconData );

    final DefaultContentArea headerContent = new DefaultContentArea()
    {
      @Override
      public Point getContentAreaAnchorPoint( )
      {
        return new Point( 5, 40 );
      }
    };

    headerContent.setText( m_module.getHeader(), KalypsoProjectDatabaseClient.WELCOME_PAGE_HEADING, KalypsoProjectDatabaseClient.COLOR_WELCOME_PAGE_HEADING, SWT.RIGHT );
    headerCanvas.addContentArea( headerContent );

    /* left pane */
    final Composite leftPane = m_toolkit.createComposite( this, SWT.NONE );
    leftPane.setLayout( new GridLayout() );
    final GridData leftGridData = new GridData( GridData.FILL, GridData.FILL, false, true );
    leftGridData.widthHint = leftGridData.minimumWidth = 400;
    leftPane.setLayoutData( leftGridData );
    leftPane.setBackground( COLOR_BOX );

    /* right pane */
    final Composite rightPane = m_toolkit.createComposite( this, SWT.NONE );
    rightPane.setLayout( new GridLayout() );
    rightPane.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    rightPane.setBackground( COLOR_BOX );

    final ProjectListViewer projectListViewer = new ProjectListViewer( m_module, m_toolkit );
    projectListViewer.createProjectList( leftPane );
    projectListViewer.createProjectInfo( rightPane );
  }
}
