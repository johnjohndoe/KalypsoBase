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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ui.wizard.sensor.i18n.Messages;

/**
 * @author doemming
 */
public class ImportObservationAxisMappingWizardPage extends WizardPage implements FocusListener, ISelectionChangedListener
{
  private Composite m_topLevel;

  private final List<WidgetLine> m_widgetLines = new ArrayList<WidgetLine>();

  private INativeObservationAdapter m_nativeAdapter;

  private IFile m_fileTarget;

  public ImportObservationAxisMappingWizardPage( final String pageName )
  {
    super( pageName );
    setTitle( pageName );
    setDescription( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationAxisMappingWizardPage.0" ) ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    initializeDialogUnits( parent );

    m_topLevel = new Composite( parent, SWT.NONE );

    m_topLevel.setLayout( new GridLayout( 2, true ) );

    final Label labelSource = new Label( m_topLevel, SWT.NONE );
    labelSource.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationAxisMappingWizardPage.1" ) ); //$NON-NLS-1$
    labelSource.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );

    final Label labelTarget = new Label( m_topLevel, SWT.NONE );
    labelTarget.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationAxisMappingWizardPage.2" ) ); //$NON-NLS-1$
    labelTarget.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );

    setControl( m_topLevel );

    validate();
  }

  public void validate( )
  {
    // page is always complete
    final IWizardContainer container = getWizard().getContainer();
    final IWizardPage currentPage = container.getCurrentPage();
    if( currentPage != null )
      container.updateButtons();
    setPageComplete( true );
  }

  /**
   * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
   */
  @Override
  public void focusGained( final FocusEvent e )
  {
    //
  }

  /**
   * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
   */
  @Override
  public void focusLost( final FocusEvent e )
  {
    //
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    final ISelection eventSelection = event.getSelection();
    if( !(eventSelection instanceof ObservationImportSelection) )
      return;

    final ObservationImportSelection selection = (ObservationImportSelection) eventSelection;
    final INativeObservationAdapter nativeAdapter = selection.getNativeAdapter();
    if( nativeAdapter != m_nativeAdapter )
      m_nativeAdapter = nativeAdapter;

    m_fileTarget = selection.getFileTarget();

    final IAxis[] axisSrc = m_nativeAdapter.createAxis();
    IAxis[] axisDest = axisSrc;
    if( m_fileTarget != null && m_fileTarget.exists() )
    {
      try
      {
        final URL targetLocation = ResourceUtilities.createURL( m_fileTarget );
        final IObservation targetObservation = getTargetObservation( targetLocation );
        axisDest = createMappedAxis( axisSrc, targetObservation.getAxisList() );
      }
      catch( final Exception e )
      {
        MessageDialog.openInformation( getShell(), Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationAxisMappingWizardPage.3" ), //$NON-NLS-1$
            Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationAxisMappingWizardPage.4" ) ); //$NON-NLS-1$
      }
    }

    if( !selection.isEmpty() )
      updateAxisWidgets( axisSrc, axisDest );

    validate();
  }

  private IAxis[] createMappedAxis( final IAxis[] axisSrc, final IAxis[] axisTarget )
  {
    final List<IAxis> result = new ArrayList<IAxis>();
    for( final IAxis src : axisSrc )
    {
      boolean mapped = false;
      for( int i_target = 0; i_target < axisTarget.length; i_target++ )
      {
        final IAxis target = axisTarget[i_target];
        if( target != null && target.getType().equals( src.getType() ) )
        {
          axisTarget[i_target] = null;
          result.add( target );
          mapped = true;
          break;
        }
      }
      if( !mapped )
        result.add( null );
    }
    // add others last
    for( final IAxis axis : axisTarget )
    {
      if( axis != null )
        result.add( axis );
    }
    return result.toArray( new IAxis[result.size()] );
  }

  public static IObservation getTargetObservation( final URL url ) throws SensorException
  {
    return ZmlFactory.parseXML( url );
  }

  private void updateAxisWidgets( final IAxis[] axisLeft, final IAxis[] axisRight )
  {
    final int maxLines = axisLeft.length > axisRight.length ? axisLeft.length : axisRight.length;
    // apply count of widgets
    while( !(maxLines == m_widgetLines.size()) )
    {
      if( maxLines > m_widgetLines.size() )
      {
        // add widgets
        m_widgetLines.add( new WidgetLine( m_topLevel ) );
      }
      if( maxLines < m_widgetLines.size() )
      {
        // remove widgets
        final int last = m_widgetLines.size() - 1;
        final WidgetLine wLine = m_widgetLines.get( last );
        wLine.getLeft().dispose();
        wLine.getRight().dispose();
        m_widgetLines.remove( last );
      }
    }
    // update widgets
    for( int i = 0; i < axisRight.length; i++ )
    {
      final WidgetLine wLine = m_widgetLines.get( i );
      final AxisWidget leftAxisWidget = wLine.getLeft();
      final AxisWidget rightAxisWidget = wLine.getRight();
      if( i < axisLeft.length )
        leftAxisWidget.setAxis( axisLeft[i] );
      else
        leftAxisWidget.setAxis( null );
      if( i < axisRight.length )
        rightAxisWidget.setAxis( axisRight[i] );
      else
        rightAxisWidget.setAxis( null );

      leftAxisWidget.setMode( false, false );
      if( m_fileTarget.exists() )
        rightAxisWidget.setMode( false, false );
      else
        rightAxisWidget.setMode( true, false );
    }

    m_topLevel.layout( true, true );
  }

  static class WidgetLine
  {
    private final AxisWidget m_left;

    private final AxisWidget m_right;

    public WidgetLine( final Composite parent )
    {
      m_left = new AxisWidget( parent, SWT.NONE );
      m_left.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

      m_right = new AxisWidget( parent, SWT.NONE );
      m_right.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    }

    public AxisWidget getLeft( )
    {
      return m_left;
    }

    public AxisWidget getRight( )
    {
      return m_right;
    }
  }

  public IAxis[] getAxisMappingSrc( )
  {
    final List<IAxis> result = new ArrayList<IAxis>();
    for( int i = 0; i < m_widgetLines.size(); i++ )
    {
      final WidgetLine wl = m_widgetLines.get( i );
      final IAxis axisTarget = wl.getRight().getAxis();
      if( axisTarget != null )
      {
        final IAxis axisSrc = wl.getLeft().getAxis();
        result.add( axisSrc );
      }
    }
    return result.toArray( new IAxis[result.size()] );
  }

  public IAxis[] getAxisMappingTarget( )
  {
    final List<IAxis> result = new ArrayList<IAxis>();
    for( int i = 0; i < m_widgetLines.size(); i++ )
    {
      final WidgetLine wl = m_widgetLines.get( i );
      final IAxis axisTarget = wl.getRight().getAxis();
      if( axisTarget != null )
        result.add( axisTarget );
    }
    return result.toArray( new IAxis[result.size()] );
  }
}