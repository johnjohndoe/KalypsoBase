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
package org.kalypso.ui.wizard.sensor;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.impl.SimpleAxis;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.ui.wizard.sensor.i18n.Messages;

/**
 * @author doemming
 */
public class AxisWidget extends Composite
{
  private SimpleAxis m_axis = null;

  private Text m_textName;

  private Combo m_comboTypes;

  private boolean m_changeName = false;

  private boolean m_changeType = false;

  public AxisWidget( final Composite parent, final int style )
  {
    super( parent, style );

    createControl();

    updateGUIFromMember();

    validate();
  }

  private void createControl( )
  {
    setLayout( new GridLayout() );

    final Group group = new Group( this, SWT.NONE );
    group.setLayout( new GridLayout( 2, false ) );
    group.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.AxisWidget.0" ) ); //$NON-NLS-1$

    group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    final Label labelName = new Label( group, SWT.NONE );
    labelName.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.AxisWidget.1" ) ); //$NON-NLS-1$
    labelName.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, false ) );

    m_textName = new Text( group, SWT.BORDER );
    m_textName.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.AxisWidget.2" ) ); //$NON-NLS-1$
    m_textName.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final Label labelType = new Label( group, SWT.NONE );
    labelType.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.AxisWidget.3" ) ); //$NON-NLS-1$
    labelType.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, false ) );

    m_comboTypes = new Combo( group, SWT.NONE );
    m_comboTypes.setItems( TimeseriesUtils.TYPES_ALL );
    m_comboTypes.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
  }

  private void validate( )
  {
    if( m_changeName )
      m_textName.setEditable( true );
    else
      m_textName.setEditable( false );
    if( m_changeType )
      m_comboTypes.setEnabled( true );
    else
      m_comboTypes.setEnabled( false );
    if( m_axis == null )
      setVisible( false );
    else
      setVisible( true );
  }

  public IAxis getAxis( )
  {
    return m_axis;
  }

  public void setAxis( final IAxis axis )
  {
    if( axis != null )
      m_axis = new SimpleAxis( axis );
    else
      m_axis = null;
    updateGUIFromMember();
    validate();
  }

  private void updateGUIFromMember( )
  {
    if( m_axis != null )
    {
      m_textName.setText( m_axis.getName() );
      m_comboTypes.select( Arrays.binarySearch( TimeseriesUtils.TYPES_ALL, m_axis.getType() ) );
    }
  }

  public void setMode( final boolean changeName, final boolean changeType )
  {
    m_changeName = changeName;
    m_changeType = changeType;
    validate();
  }
}