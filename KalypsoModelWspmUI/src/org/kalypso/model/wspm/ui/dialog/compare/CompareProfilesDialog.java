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
package org.kalypso.model.wspm.ui.dialog.compare;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.kalypso.contribs.eclipse.jface.dialog.EnhancedTitleAreaDialog;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author Dirk Kuch
 */
public class CompareProfilesDialog extends EnhancedTitleAreaDialog
{
  private static final String SCREEN_SIZE = "compare.profiles.dialog"; // $NON-NLS-1$

  protected final String m_screen_setting;

  private final ICompareProfileProvider m_provider;

  public CompareProfilesDialog( final Shell shell, final ICompareProfileProvider provider, final String screenSetting )
  {
    super( shell );
    setShellStyle( SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE );
    m_provider = provider;
    m_screen_setting = screenSetting;
  }

  public CompareProfilesDialog( final Shell shell, final ICompareProfileProvider provider )
  {
    this( shell, provider, SCREEN_SIZE );
  }

  /**
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    getShell().setText( Messages.getString("CompareProfilesDialog_0") ); //$NON-NLS-1$

    // FIXME: title should be set from outside, we always have different reasons to compare two profiles
    setTitle( "Profilvergleich zwischen KalypsoWSPM Modellprofil und Ma�nahmenprofil" );

    final FormToolkit toolkit = new FormToolkit( parent.getDisplay() );

    final Composite base = toolkit.createComposite( parent, SWT.NULL );
    base.setLayout( new GridLayout() );

    final Point screen = getScreenSize( m_screen_setting );

    final GridData data = new GridData( GridData.FILL, GridData.FILL, true, true );
    data.widthHint = screen.x;
    data.heightHint = screen.y;
    base.setLayoutData( data );

    final ScrolledForm form = toolkit.createScrolledForm( base );
    form.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    final Composite body = form.getBody();
    final GridLayout bodyLayout = new GridLayout();
    bodyLayout.marginHeight = bodyLayout.marginWidth = 0;
    body.setLayout( bodyLayout );

    base.addControlListener( new ControlAdapter()
    {
      /**
       * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
       */
      @Override
      public void controlResized( final ControlEvent e )
      {
        setScreenSize( m_screen_setting, base.getSize() );
      }
    } );

    final CompareProfileWrapper baseWrapper = m_provider.getBaseProfile();

    final ProfileChartComposite baseChartView = createChartView( baseWrapper, body, toolkit );

    final List<ProfileChartComposite> additionalChartViews = new ArrayList<ProfileChartComposite>();

    final CompareProfileWrapper[] additional = m_provider.getAdditionalProfiles( baseWrapper.getProfil() );
    for( final CompareProfileWrapper ad : additional )
    {
      final ProfileChartComposite additionalView = createChartView( ad, body, toolkit );
      additionalChartViews.add( additionalView );
    }

    final CompareSwitchProfileButtonDialog switchDialog = new CompareSwitchProfileButtonDialog( body, baseChartView, m_provider, additionalChartViews.toArray( new ProfileChartComposite[] {} ) );
    switchDialog.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    return super.createDialogArea( parent );
  }

  private ProfileChartComposite createChartView( final CompareProfileWrapper wrapper, final Composite body, final FormToolkit toolkit )
  {
    final Group group = new Group( body, SWT.NULL );

    final GridLayout layout = new GridLayout();
    layout.marginHeight = layout.marginWidth = 0;
    group.setLayout( layout );

    group.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    group.setText( wrapper.getLabel() );

    final ProfileChartComposite chart = new ProfileChartComposite( group, SWT.NONE, wrapper.getLayerProvider(), wrapper.getProfil() );
    chart.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    toolkit.adapt( group );

    return chart;
  }

  @Override
  protected void createButtonsForButtonBar( final Composite parent )
  {
    createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
  }

}
