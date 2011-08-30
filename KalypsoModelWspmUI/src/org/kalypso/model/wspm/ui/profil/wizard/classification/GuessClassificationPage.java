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
package org.kalypso.model.wspm.ui.profil.wizard.classification;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.swt.layout.Layouts;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.model.wspm.core.IWspmPointProperties;

/**
 * @author Dirk Kuch
 */
public class GuessClassificationPage extends WizardPage
{
  protected String m_type = IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS;

  protected boolean m_overwrite = false;

  protected Double m_delta = Double.MAX_VALUE;

  protected GuessClassificationPage( )
  {
    super( "GuessClassificationPage" ); //$NON-NLS-1$

    setTitle( "Guess Classification Classes" );
    setDescription( "Which kind of classification classes you like to guess?" );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Composite body = new Composite( parent, SWT.NULL );
    body.setLayout( Layouts.createGridLayout() );

    createTypeControl( body );
    createPropertiesControl( body );

    setControl( body );
  }

  private void createPropertiesControl( final Composite body )
  {
    new Label( body, SWT.NULL ).setText( "Fuziness (Max. Delta of class value)" );

    // TODO validated text box
    final Text text = new Text( body, SWT.BORDER );
    text.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    text.setText( "9999" );

    text.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        final double delta = NumberUtils.parseQuietDouble( text.getText() );
        if( Double.isNaN( delta ) )
          m_delta = Double.MAX_VALUE;
        else
          m_delta = delta;

      }
    } );

    new Label( body, SWT.NULL ).setText( "" ); // spacer

    final Button buttonOverwrite = new Button( body, SWT.CHECK );
    buttonOverwrite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    buttonOverwrite.setText( "Overwrite existing classes" );

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
    group.setText( "Type" );

    final Button buttonVegetation = new Button( group, SWT.RADIO );
    buttonVegetation.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    buttonVegetation.setText( "Guess Vegetation Classes" );
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
    buttonRoughness.setText( "Guess Roughness Classes" );

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

  public Double getDelta( )
  {
    return m_delta;
  }
}
