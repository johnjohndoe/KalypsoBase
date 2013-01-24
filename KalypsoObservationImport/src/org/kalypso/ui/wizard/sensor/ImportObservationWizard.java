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
package org.kalypso.ui.wizard.sensor;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.wq.WQTuppleModel;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.ui.wizard.sensor.i18n.Messages;

public class ImportObservationWizard extends Wizard implements IImportWizard
{
  private ImportObservationSelectionWizardPage m_importPage = null;

  private IStructuredSelection m_selection;

  private ImportObservationAxisMappingWizardPage m_axisMappingPage;

  public ImportObservationWizard( )
  {
    setHelpAvailable( false );
    setNeedsProgressMonitor( false );
    setWindowTitle( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationWizard.0" ) ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection currentSelection )
  {
    m_selection = currentSelection;
    final List< ? > selectedResources = IDE.computeSelectedResources( currentSelection );
    if( !selectedResources.isEmpty() )
      m_selection = new StructuredSelection( selectedResources );

    setWindowTitle( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationWizard.1" ) ); //$NON-NLS-1$
    setNeedsProgressMonitor( true );
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  @Override
  public void addPages( )
  {
    super.addPages();

    m_importPage = new ImportObservationSelectionWizardPage( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationWizard.3" ) ); //$NON-NLS-1$
    m_axisMappingPage = new ImportObservationAxisMappingWizardPage( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationWizard.2" ) ); //$NON-NLS-1$

    addPage( m_importPage );
    addPage( m_axisMappingPage );

    m_importPage.setSelection( m_selection );
    m_importPage.addSelectionChangedListener( m_axisMappingPage );
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performCancel()
   */
  @Override
  public boolean performCancel( )
  {
    return true;
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    try
    {
      final ObservationImportSelection selection = (ObservationImportSelection) m_importPage.getSelection();
      final File fileSource = selection.getFileSource();
      final IFile fileTarget = selection.getFileTarget();
      final INativeObservationAdapter nativaAdapter = selection.getNativeAdapter();
      final TimeZone timezone = selection.getSourceTimezone();
      final IObservation srcObservation = nativaAdapter.createObservationFromSource( fileSource, timezone, false );

      final IAxis[] axesSrc = m_axisMappingPage.getAxisMappingSrc();
      final IAxis[] axesNew = m_axisMappingPage.getAxisMappingTarget();

      final ITupleModel tuppelModelSrc = srcObservation.getValues( null );
      final int countSrc = tuppelModelSrc.size();

      final IObservation targetObservation;
      final ITupleModel tuppelModelTarget;
      final int countTarget;
      if( fileTarget.exists() && (selection.isAppend() || selection.isRetainMetadata()) )
      {
        final URL targetLocation = ResourceUtilities.createURL( fileTarget );
        targetObservation = ImportObservationAxisMappingWizardPage.getTargetObservation( targetLocation );
        tuppelModelTarget = targetObservation.getValues( null );
        if( selection.isAppend() )
          countTarget = tuppelModelTarget.size();
        else
          countTarget = 0;
      }
      else
      {
        targetObservation = null;
        tuppelModelTarget = null;
        countTarget = 0;
      }
      // create new values
      final ITupleModel newTuppelModel;
      if( tuppelModelTarget != null )
      {
        // w/q specials...
        if( tuppelModelTarget instanceof WQTuppleModel )
        {
          final WQTuppleModel wq = (WQTuppleModel) (tuppelModelTarget);
          final Object[][] newValues = new Object[countSrc + countTarget][axesNew.length - 1];
          final ITupleModel model = new SimpleTupleModel( axesNew, newValues );

          newTuppelModel = new WQTuppleModel( model, wq.getMetadata(), axesNew, wq.getSourceAxes(), wq.getTargetAxes(), wq.getConverter() );
        }
        else
        {
          final Object[][] newValues = new Object[countSrc + countTarget][axesNew.length];
          newTuppelModel = new SimpleTupleModel( axesNew, newValues );
        }
      }
      else
      {
        final Object[][] newValues = new Object[countSrc + countTarget][axesNew.length];
        newTuppelModel = new SimpleTupleModel( axesNew, newValues );
      }
      // fill from source
      for( int i = 0; i < countSrc; i++ )
      {
        for( int a = 0; a < axesNew.length; a++ )
        {
          final Object newValue;
          if( axesSrc[a] == null )
          {
            if( KalypsoStatusUtils.isStatusAxis( axesNew[a] ) )
              newValue = new Integer( KalypsoStati.BIT_USER_MODIFIED );
            else
              newValue = null;
          }
          else
            newValue = tuppelModelSrc.get( i, axesSrc[a] );
          if( newValue != null )
            newTuppelModel.set( i, axesNew[a], newValue );
        }
      }
      // append from existing target
      if( tuppelModelTarget != null )
      {
        for( int i = 0; i < countTarget; i++ )
          for( final IAxis element : axesNew )
            newTuppelModel.set( countSrc + i, element, tuppelModelTarget.get( i, element ) );
      }

      final String href = ""; //$NON-NLS-1$
      final String name = srcObservation.getName();
      final MetadataList metadata = new MetadataList();
      if( targetObservation != null && selection.isRetainMetadata() )
        metadata.putAll( targetObservation.getMetadataList() );
      metadata.putAll( srcObservation.getMetadataList() );

      final IObservation newObservation = new SimpleObservation( href, name, metadata, newTuppelModel );
      ZmlFactory.writeToFile( newObservation, fileTarget );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }
}