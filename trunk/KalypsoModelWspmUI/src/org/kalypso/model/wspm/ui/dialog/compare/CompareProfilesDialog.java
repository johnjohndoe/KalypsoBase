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
package org.kalypso.model.wspm.ui.dialog.compare;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author Dirk Kuch
 */
public class CompareProfilesDialog extends TitleAreaDialog
{
  private static final int SCREEN_WIDTH = 800;

  private static final int SCREEN_HEIGHT = 600;

  private final ICompareProfileProvider m_provider;

  public CompareProfilesDialog( final Shell shell, final ICompareProfileProvider provider )
  {
    super( shell );
    m_provider = provider;
  }

  /**
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    getShell().setText( "Profilansicht" );

    setTitle( "Profilvergleich zwischen KalypsoWSPM Modellprofil und Maﬂnahmenprofil" );

    final FormToolkit toolkit = new FormToolkit( parent.getDisplay() );

    final Composite base = toolkit.createComposite( parent );

    final GridLayout baseLayout = new GridLayout();
    baseLayout.marginHeight = baseLayout.marginWidth = 0;
    base.setLayout( baseLayout );

    final GridData data = new GridData( GridData.FILL, GridData.FILL, true, true );
    data.widthHint = SCREEN_WIDTH;
    data.heightHint = SCREEN_HEIGHT;
    base.setLayoutData( data );

    final ScrolledForm form = toolkit.createScrolledForm( base );
    form.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    final Composite body = form.getBody();
    final GridLayout bodyLayout = new GridLayout();
    bodyLayout.marginHeight = bodyLayout.marginWidth = 0;
    body.setLayout( bodyLayout );

    final CompareProfileWrapper baseWrapper = m_provider.getBaseProfile();
    final CompareProfilesChartView baseChartView = createChartView( baseWrapper, body, toolkit );

    final List<CompareProfilesChartView> additionalChartViews = new ArrayList<CompareProfilesChartView>();

    final CompareProfileWrapper[] additional = m_provider.getAdditionalProfiles( baseWrapper.getProfil() );
    for( final CompareProfileWrapper ad : additional )
    {
      final CompareProfilesChartView additionalView = createChartView( ad, body, toolkit );
      additionalChartViews.add( additionalView );
    }

    final CompareSwitchProfileButtonDialog switchDialog = new CompareSwitchProfileButtonDialog( body, baseChartView, m_provider, additionalChartViews.toArray( new CompareProfilesChartView[] {} ) );
    switchDialog.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    return super.createDialogArea( parent );
  }

  private CompareProfilesChartView createChartView( final CompareProfileWrapper wrapper, final Composite body, final FormToolkit toolkit )
  {
    final Group group = new Group( body, SWT.NULL );

    final GridLayout layout = new GridLayout();
    layout.marginHeight = layout.marginWidth = 0;
    group.setLayout( layout );

    group.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );
    group.setText( wrapper.getLabel() );

    final CompareProfilesChartView chartView = new CompareProfilesChartView( wrapper.getProfil(), wrapper.getLayerProvider() );
    chartView.createControl( group );

    chartView.setProfil( wrapper.getProfil() );

    final ChartComposite chart = chartView.getChart();
    if( chart != null )
      ChartUtilities.maximize( chart.getChartModel() );

    toolkit.adapt( group );

    return chartView;
  }

  @Override
  protected void createButtonsForButtonBar( final Composite parent )
  {
    createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
  }

}
