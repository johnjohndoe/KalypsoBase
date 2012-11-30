/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.sensor.view.observationDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree_impl.gml.schema.SpecialPropertyMapper;

public class AxisRangeDialog extends TitleAreaDialog
{
  private final String m_axisType;

  private Text m_minText;

  private Text m_intText;

  private Text m_countText;

  private Text m_defaultText;

  private int m_count = 1;

  private Object m_int = null;

  private Object m_min = null;

  private boolean m_valid = false;

  private Object m_default = null;

  public AxisRangeDialog( final Shell parent, final String axisType )
  {
    super( parent );
    m_axisType = axisType;

    setHelpAvailable( false );
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    getShell().setText( Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.0" ) ); //$NON-NLS-1$

    setTitle( Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.0" ) ); //$NON-NLS-1$
    setMessage( Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.20" ) ); //$NON-NLS-1$

    final Group group = new Group( parent, SWT.NONE );
    group.setLayout( new GridLayout( 2, true ) );
    group.setLayoutData( new GridData( GridData.FILL_BOTH ) );

    // MIN
    final Label label = new Label( group, SWT.NONE );
    label.setText( Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.3" ) ); //$NON-NLS-1$
    m_minText = new Text( group, SWT.BORDER | SWT.TRAIL );
    m_minText.setText( "0" ); //$NON-NLS-1$
    final GridData m_minData = new GridData( GridData.FILL_HORIZONTAL );
    m_minData.widthHint = 150;
    m_minText.setLayoutData( m_minData );
    // Intervall
    final Label label2 = new Label( group, SWT.NONE );
    label2.setText( Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.5" ) ); //$NON-NLS-1$
    m_intText = new Text( group, SWT.BORDER | SWT.TRAIL );
    m_intText.setText( "1" ); //$NON-NLS-1$
    final GridData m_intData = new GridData( GridData.FILL_HORIZONTAL );
    m_intData.widthHint = 150;
    m_intText.setLayoutData( m_intData );
    // ANZAHL
    final Label label3 = new Label( group, SWT.NONE );
    label3.setText( Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.7" ) ); //$NON-NLS-1$
    m_countText = new Text( group, SWT.BORDER | SWT.TRAIL );
    m_countText.setText( "10" ); //$NON-NLS-1$
    final GridData m_countData = new GridData( GridData.FILL_HORIZONTAL );
    m_countData.widthHint = 150;
    m_countText.setLayoutData( m_countData );
    // Default Wert
    final Label label5 = new Label( group, SWT.NONE );
    label5.setText( Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.9" ) ); //$NON-NLS-1$
    m_defaultText = new Text( group, SWT.BORDER | SWT.TRAIL );
    m_defaultText.setText( "10" ); //$NON-NLS-1$
    final GridData m_defaultData = new GridData( GridData.FILL_HORIZONTAL );
    m_defaultData.widthHint = 150;
    m_defaultText.setLayoutData( m_defaultData );

    final ModifyListener modifyListener = new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        validate();
      }
    };

    m_minText.addModifyListener( modifyListener );
    m_intText.addModifyListener( modifyListener );
    m_countText.addModifyListener( modifyListener );
    m_defaultText.addModifyListener( modifyListener );

    validate();
    return group;
  }

  public Object getMin( )
  {
    return m_min;
  }

  public Object getInt( )
  {
    return m_int;
  }

  public Object getDefault( )
  {
    return m_default;

  }

  public int getCount( )
  {
    return m_count;
  }

  public void validate( )
  {
    String message = ""; //$NON-NLS-1$
    try
    {
      message = Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.14" ); //$NON-NLS-1$
      m_min = SpecialPropertyMapper.cast( m_minText.getText(), TimeseriesUtils.getDataClass( m_axisType ), false );
      message = Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.15" ); //$NON-NLS-1$
      m_int = SpecialPropertyMapper.cast( m_intText.getText(), TimeseriesUtils.getDataClass( m_axisType ), false );
      message = Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.16" ); //$NON-NLS-1$
      m_count = ((Integer)SpecialPropertyMapper.cast( m_countText.getText(), Integer.class, false )).intValue();
      message = Messages.getString( "org.kalypso.ogc.sensor.view.AxisRangeDialog.17" ); //$NON-NLS-1$
      m_default = SpecialPropertyMapper.cast( m_defaultText.getText(), TimeseriesUtils.getDataClass( m_axisType ), false );
      m_valid = true;
    }
    catch( final Exception e )
    {
      m_valid = false;
    }

    if( m_valid )
      setErrorMessage( null );
    else
      setErrorMessage( message );

    final Button button = getButton( IDialogConstants.OK_ID );
    if( button != null )
      button.setEnabled( m_valid );
  }

  public boolean isValid( )
  {
    return m_valid;
  }
}