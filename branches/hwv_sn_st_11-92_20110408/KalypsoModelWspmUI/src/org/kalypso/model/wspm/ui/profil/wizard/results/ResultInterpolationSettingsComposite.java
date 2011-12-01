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
package org.kalypso.model.wspm.ui.profil.wizard.results;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class ResultInterpolationSettingsComposite implements IResultInterpolationSettings
{
  private static final String SETTINGS_DO_INTERPOLATION = "doInterpolation"; //$NON-NLS-1$

  private static final String SETTINGS_DO_FORELAND = "doForeland"; //$NON-NLS-1$

  private boolean m_doInterpolation = true;

  private boolean m_doForelandInterpolation = false;

  private final IDialogSettings m_settings;

  public ResultInterpolationSettingsComposite( final IDialogSettings settings )
  {
    m_settings = settings;
  }

  protected IDialogSettings getDialogSettings( )
  {
    return m_settings;
  }

  public Control createControl( final Composite parent )
  {
    readDialogSettings();

    final Group group = new Group( parent, SWT.NONE );

    group.setText( Messages.getString( "ResultInterpolationSettingsComposite_4" ) ); //$NON-NLS-1$
    group.setLayout( new GridLayout() );

    final Button doInterpolationButton = new Button( group, SWT.CHECK );
    doInterpolationButton.setText( Messages.getString( "ResultInterpolationSettingsComposite_5" ) ); //$NON-NLS-1$
    doInterpolationButton.setToolTipText( Messages.getString( "ResultInterpolationSettingsComposite_6" ) ); //$NON-NLS-1$
    doInterpolationButton.setSelection( m_doInterpolation );

    final Button interpolationForelandButton = new Button( group, SWT.CHECK );
    interpolationForelandButton.setText( Messages.getString( "ResultInterpolationSettingsComposite_7" ) ); //$NON-NLS-1$
    interpolationForelandButton.setToolTipText( Messages.getString( "ResultInterpolationSettingsComposite_8" ) ); //$NON-NLS-1$
    interpolationForelandButton.setSelection( m_doForelandInterpolation );
    interpolationForelandButton.setEnabled( m_doInterpolation );

    doInterpolationButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final boolean selection = doInterpolationButton.getSelection();
        setDoInterpolation( selection );
        interpolationForelandButton.setEnabled( selection );
      }
    } );

    interpolationForelandButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        setDoForeland( interpolationForelandButton.getSelection() );
      }
    } );

    return group;
  }

  private void readDialogSettings( )
  {
    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings == null )
      return;

    m_doInterpolation = dialogSettings.getBoolean( SETTINGS_DO_INTERPOLATION );
    m_doForelandInterpolation = dialogSettings.getBoolean( SETTINGS_DO_FORELAND );
  }

  protected void setDoInterpolation( final boolean doInterpolation )
  {
    m_doInterpolation = doInterpolation;

    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
      dialogSettings.put( SETTINGS_DO_INTERPOLATION, m_doInterpolation );
  }

  protected void setDoForeland( final boolean doForelandInterpolation )
  {
    m_doForelandInterpolation = doForelandInterpolation;

    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
      dialogSettings.put( SETTINGS_DO_FORELAND, m_doForelandInterpolation );
  }

  @Override
  public boolean shouldAddInterpolatedProfiles( )
  {
    return m_doInterpolation;
  }

  @Override
  public boolean shouldInterpolateForland( )
  {
    return m_doForelandInterpolation;
  }
}
