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
package org.kalypso.model.wspm.ui.profil.wizard.interpolate.geomtry;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author Dirk Kuch
 */
public class FillMissingProfilesGeometriesPage extends WizardPage
{
  protected boolean m_extrapolate = false;

  protected FillMissingProfilesGeometriesPage( )
  {
    super( "InterpolateMissingProfilesGeometriesPage" ); //$NON-NLS-1$

    setTitle( Messages.getString("FillMissingProfilesGeometriesPage_0") ); //$NON-NLS-1$
    setDescription( Messages.getString("FillMissingProfilesGeometriesPage_1") ); //$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    final Composite body = new Composite( parent, SWT.NULL );
    GridLayoutFactory.fillDefaults().applyTo( body );

    final Group group = new Group( body, SWT.NONE );
    group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    group.setLayout( new GridLayout() );
    group.setText( Messages.getString("FillMissingProfilesGeometriesPage_2") ); //$NON-NLS-1$

    final Button buttonInterpolate = new Button( group, SWT.CHECK );
    buttonInterpolate.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    buttonInterpolate.setText( Messages.getString("FillMissingProfilesGeometriesPage_3") ); //$NON-NLS-1$
    buttonInterpolate.setEnabled( false );
    buttonInterpolate.setSelection( true );

    final Button buttonExtrapolate = new Button( group, SWT.CHECK );
    buttonExtrapolate.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    buttonExtrapolate.setText( Messages.getString("FillMissingProfilesGeometriesPage_4") ); //$NON-NLS-1$

    buttonExtrapolate.addSelectionListener( new SelectionAdapter()
    {

      @Override
      public void widgetSelected( final org.eclipse.swt.events.SelectionEvent e )
      {
        m_extrapolate = buttonExtrapolate.getSelection();
      }
    } );

    setControl( body );
  }

  public boolean doExtrapolation( )
  {
    return m_extrapolate;
  }

}
