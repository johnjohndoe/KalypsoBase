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
package org.kalypso.model.wspm.ui.dialog.compare;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartView;

import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author Dirk Kuch
 */
public class SwitchProfileButtonDialog extends Composite
{
  private static final Font TXT_BOLD = new Font( Display.getDefault(), "Tahoma", 8, SWT.BOLD ); //$NON-NLS-1$

  protected final IProfilChartView m_chartView;

  protected final IProfil[] m_profiles;

  private Label m_label;

  public SwitchProfileButtonDialog( final Composite parent, final IProfilChartView chartView, final IProfil[] profiles )
  {
    super( parent, SWT.NULL );
    m_chartView = chartView;
    m_profiles = profiles;

    final GridLayout layout = new GridLayout( 4, false );
    layout.marginHeight = layout.marginWidth = 0;
    this.setLayout( layout );

    final FormToolkit toolkit = new FormToolkit( parent.getDisplay() );
    render( toolkit );

    toolkit.adapt( this );
  }

  private void render( final FormToolkit toolkit )
  {
    m_label = toolkit.createLabel( this, getProfileLabel() );
    m_label.setFont( TXT_BOLD );
    final GridData data = new GridData( GridData.FILL, GridData.CENTER, false, false );
    data.minimumWidth = 250;

    toolkit.createLabel( this, "" ).setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    m_label.setLayoutData( data );

    final Button back = toolkit.createButton( this, "Profil zurück", SWT.PUSH );
    back.setLayoutData( getButtonLayoutData() );

    final Button next = toolkit.createButton( this, "Profil vor", SWT.PUSH );
    next.setLayoutData( getButtonLayoutData() );

    back.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final int index = getIndex();
        if( index == 0 )
          return;

        setProfile( m_profiles[index - 1] );

      }
    } );

    next.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final int index = getIndex();
        if( index == m_profiles.length - 1 )
          return;

        setProfile( m_profiles[index + 1] );

      }
    } );
  }

  protected void setProfile( final IProfil profile )
  {
    m_chartView.setProfil( profile );

    final ChartComposite chart = m_chartView.getChart();
    if( chart != null )
      ChartUtilities.maximize( chart.getChartModel() );

    m_label.setText( getProfileLabel() );
  }

  protected int getIndex( )
  {
    final IProfil profil = m_chartView.getProfil();
    return ArrayUtils.indexOf( m_profiles, profil );
  }

  protected String getProfileLabel( )
  {
    final IProfil profil = m_chartView.getProfil();
    final double station = profil.getStation();

    final String msg = String.format( "Profil %.2f km (%d / %d)", station, getIndex() + 1, m_profiles.length );
    return msg;
  }

  private Object getButtonLayoutData( )
  {
    final GridData data = new GridData( GridData.FILL, GridData.FILL, false, false );
    data.widthHint = 150;

    return data;
  }

}
