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
package org.kalypso.model.wspm.ui.profil.wizard.classification.apply;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author Dirk Kuch
 */
public class ApplyClassificationsPage extends WizardPage
{
  protected String m_type = IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS;

  protected boolean m_overwrite = false;

  protected ApplyClassificationsPage( )
  {
    super( "ApplyClassificationsPage" ); //$NON-NLS-1$

    setTitle( Messages.getString( "ApplyClassificationsPage.0" ) ); //$NON-NLS-1$
    setDescription( Messages.getString( "ApplyClassificationsPage.1" ) ); //$NON-NLS-1$
  }

  @Override
  public void createControl( final Composite parent )
  {
    final Composite body = new Composite( parent, SWT.NULL );
    GridLayoutFactory.fillDefaults().applyTo( body );

    createTypeControl( body );
    createPropertiesControl( body );

    setControl( body );
  }

  private void createPropertiesControl( final Composite body )
  {
    new Label( body, SWT.NULL ).setText( "" ); // spacer //$NON-NLS-1$

    final Button buttonOverwrite = new Button( body, SWT.CHECK );
    buttonOverwrite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    buttonOverwrite.setText( Messages.getString( "ApplyClassificationsPage.3" ) ); //$NON-NLS-1$

    buttonOverwrite.addSelectionListener( new SelectionAdapter()
    {

      @Override
      public void widgetSelected( final org.eclipse.swt.events.SelectionEvent e )
      {
        m_overwrite = buttonOverwrite.getSelection();
      }
    } );

  }

  private void createTypeControl( final Composite body )
  {
    final Group group = new Group( body, SWT.NONE );
    group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    group.setLayout( new GridLayout() );
    group.setText( Messages.getString( "ApplyClassificationsPage.4" ) ); //$NON-NLS-1$

    // TODO: ugly: instead use a combo box and data binding!

    final Button buttonVegetation = new Button( group, SWT.RADIO );
    buttonVegetation.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    buttonVegetation.setText( Messages.getString( "ApplyClassificationsPage.5" ) ); //$NON-NLS-1$
    buttonVegetation.setSelection( true );

    buttonVegetation.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final org.eclipse.swt.events.SelectionEvent e )
      {
        if( buttonVegetation.getSelection() )
          m_type = IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS;
      }
    } );

    final Button buttonRoughness = new Button( group, SWT.RADIO );
    buttonRoughness.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    buttonRoughness.setText( Messages.getString( "ApplyClassificationsPage.6" ) ); //$NON-NLS-1$

    buttonRoughness.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final org.eclipse.swt.events.SelectionEvent e )
      {
        if( buttonRoughness.getSelection() )
          m_type = IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS;
      }
    } );
  }

  public String getType( )
  {
    return m_type;
  }

  public Boolean isOverwriteEnabled( )
  {
    return m_overwrite;
  }
}