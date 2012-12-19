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
package org.kalypso.model.wspm.ui.dialog.straighten;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.Profiles;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.action.ProfilesSelection;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_ENABLEMENT;
import org.kalypso.model.wspm.ui.dialog.straighten.provider.CorrectPointsEnablementLabelProvider;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypsodeegree.model.geometry.GM_Exception;

import com.vividsolutions.jts.geom.Point;

/**
 * Dialog that asks for some settings, used by the {@link org.kalypso.model.wspm.ui.action.straighten.StraightenProfileWidget}.
 * 
 * @author Holger Albert
 */
public class StraightenProfileDialog extends TitleAreaDialog
{
  /**
   * The dialog settings.
   */
  private IDialogSettings m_settings;

  /**
   * The straighten profile data.
   */
  private StraightenProfileData m_data;

  /**
   * The data binding context.
   */
  private final DataBindingContext m_bindingContext;

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The parent SWT shell.
   * @param profileSelection
   *          The selection, which contains the profile.
   * @param profile
   *          The profile.
   * @param firstPoint
   *          The first point.
   * @param secondPoint
   *          The second point.
   */
  public StraightenProfileDialog( final Shell parentShell, final ProfilesSelection profileSelection, final IProfileFeature profile, final Point firstPoint, final Point secondPoint )
  {
    super( parentShell );

    initializeData( profileSelection, profile, firstPoint, secondPoint );

    m_bindingContext = new DataBindingContext();
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    /* Set the title. */
    final String title = Messages.getString("StraightenProfileDialog_0"); //$NON-NLS-1$

    getShell().setText( title );
    setTitle( title );

    /* Create the main composite. */
    final Composite main = (Composite)super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create a label. */
    final Label questionLabel = new Label( main, SWT.NONE );
    questionLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    questionLabel.setText( String.format( Messages.getString("StraightenProfileDialog_1"), m_data.getFirstWidth(), m_data.getSecondWidth() ) ); //$NON-NLS-1$

    /* Create a label. */
    final Label emptyLabel = new Label( main, SWT.NONE );
    emptyLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    emptyLabel.setText( "" ); //$NON-NLS-1$

    /* Create a combo viewer. */
    final ComboViewer viewer1 = new ComboViewer( main, SWT.READ_ONLY );
    viewer1.getCombo().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    viewer1.setLabelProvider( new CorrectPointsEnablementLabelProvider() );
    viewer1.setContentProvider( new ArrayContentProvider() );
    viewer1.setInput( CORRECT_POINTS_ENABLEMENT.values() );

    /* Create a combo viewer. */
    // final ComboViewer viewer2 = new ComboViewer( main, SWT.READ_ONLY );
    // viewer2.getCombo().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    // viewer2.setLabelProvider( new CorrectPointsAmountLabelProvider() );
    // viewer2.setContentProvider( new ArrayContentProvider() );
    // viewer2.setInput( CORRECT_POINTS_AMOUNT.values() );

    /* Do the data binding. */
    bindViewer1( viewer1 );
    // bindViewer2( viewer2 );
    // bindEnablement( viewer2 );

    return main;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed( )
  {
    m_data.storeSettings( m_settings );

    super.okPressed();
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#isResizable()
   */
  @Override
  protected boolean isResizable( )
  {
    return true;
  }

  /**
   * This function initializes the points.
   * 
   * @param profileSelection
   *          The selection, which contains the profile.
   * @param profile
   *          The profile.
   * @param firstPoint
   *          The first point.
   * @param secondPoint
   *          The second point.
   */
  private void initializeData( final ProfilesSelection profileSelection, final IProfileFeature profile, final Point firstPoint, final Point secondPoint )
  {
    try
    {
      /* Get the profile. */
      final IProfile profil = profile.getProfile();

      /* Get the width of both points. */
      final double firstWidth = Profiles.getWidth( profil, firstPoint );
      final double secondWidth = Profiles.getWidth( profil, secondPoint );

      /* Get the dialog settings. */
      m_settings = DialogSettingsUtils.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), getClass().getName() );

      /* The first point lies before the second point in the profile. */
      if( firstWidth <= secondWidth )
      {
        m_data = new StraightenProfileData( profileSelection, profile, firstPoint, secondPoint, firstWidth, secondWidth );
        m_data.loadSettings( m_settings );
        return;
      }

      /* Switch the points. */
      m_data = new StraightenProfileData( profileSelection, profile, secondPoint, firstPoint, secondWidth, firstWidth );
      m_data.loadSettings( m_settings );
    }
    catch( final GM_Exception ex )
    {
      ex.printStackTrace();
    }
  }

  /**
   * This function binds the viewer.
   * 
   * @param viewer
   *          The viewer to bind.
   */
  private void bindViewer1( final ComboViewer viewer )
  {
    /* The values. */
    final IObservableValue comboValue = ViewersObservables.observeSingleSelection( viewer );
    final IObservableValue typeValue = BeansObservables.observeValue( m_data, StraightenProfileData.PROPERTY_CORRECT_POINTS_ENABLEMENT );

    /* Bind the value. */
    m_bindingContext.bindValue( comboValue, typeValue );
  }

  /**
   * This function binds the viewer.
   * 
   * @param viewer
   *          The viewer to bind.
   */
  @SuppressWarnings( "unused" )
  private void bindViewer2( final ComboViewer viewer )
  {
    /* The values. */
    final IObservableValue comboValue = ViewersObservables.observeSingleSelection( viewer );
    final IObservableValue typeValue = BeansObservables.observeValue( m_data, StraightenProfileData.PROPERTY_CORRECT_POINTS_AMOUNT );

    /* Bind the value. */
    m_bindingContext.bindValue( comboValue, typeValue );
  }

  /**
   * This function enables/disables the second viewer, according to the state of the first viewer.
   * 
   * @param viewer2
   *          The second viewer.
   */
  @SuppressWarnings( "unused" )
  private void bindEnablement( final ComboViewer viewer2 )
  {
    /* The values. */
    final IObservableValue targetEnablement = SWTObservables.observeEnabled( viewer2.getCombo() );
    final IObservableValue modelEnablement = BeansObservables.observeValue( m_data, StraightenProfileData.PROPERTY_CORRECT_POINTS_AMOUNT_ENABLED );

    /* Bind the value. */
    m_bindingContext.bindValue( targetEnablement, modelEnablement );
  }

  /**
   * This function returns the the straighten profile data.
   * 
   * @return The straighten profile data.
   */
  public StraightenProfileData getData( )
  {
    return m_data;
  }
}