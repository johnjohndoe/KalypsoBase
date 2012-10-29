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
package org.kalypso.gml.ui.commands.exportgrid;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.jface.wizard.SaveFileWizardPage;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.grid.AscGridExporter;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.IGeoGrid;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;

/**
 * @author Gernot Belger
 */
public class RectifiedGridCoverageExportWizard extends Wizard implements IImportWizard
{
  private static final String FMT_ASC = Messages.getString( "RectifiedGridCoverageExportWizard.0" ); //$NON-NLS-1$

  private ICoverage[] m_coverages;

  private SaveFileWizardPage m_saveFileWizardPage;

  public RectifiedGridCoverageExportWizard( )
  {
    final IDialogSettings settings = DialogSettingsUtils.getDialogSettings( KalypsoGmlUIPlugin.getDefault(), "ExportRectifiedGridCoverageWizardSettings" ); //$NON-NLS-1$
    setDialogSettings( settings );
    setNeedsProgressMonitor( true );

    setWindowTitle( Messages.getString( "org.kalypso.gml.ui.wizard.grid.RectifiedGridCoverageExportWizard.0" ) ); //$NON-NLS-1$
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    final List< ? > list = selection.toList();
    final List<ICoverage> coverages = new ArrayList<>();
    for( final Object object : list )
    {
      final ICoverage coverage = toCoverage( object );
      if( coverage != null )
        coverages.add( coverage );
    }

    m_coverages = coverages.toArray( new ICoverage[coverages.size()] );
  }

  private ICoverage toCoverage( final Object object )
  {
    if( object instanceof ICoverage )
      return (ICoverage) object;

    if( object instanceof IAdaptable )
      return (ICoverage) ((IAdaptable) object).getAdapter( ICoverage.class );

    return null;
  }

  @Override
  public void addPages( )
  {
    final Map<Object, String> formats = new HashMap<>();
    formats.put( FMT_ASC, "asc" ); //$NON-NLS-1$

    m_saveFileWizardPage = new SaveFileWizardPage( "saveFilePage", Messages.getString( "org.kalypso.gml.ui.wizard.grid.RectifiedGridCoverageExportWizard.1" ), null, Messages.getString( "org.kalypso.gml.ui.wizard.grid.RectifiedGridCoverageExportWizard.2" ), formats ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    addPage( m_saveFileWizardPage );

    super.addPages();
  }

  @Override
  public boolean performFinish( )
  {
    // read/copy input
    final Object format = m_saveFileWizardPage.getDestinationFormat();
    final String path = m_saveFileWizardPage.getDestinationValue();

    // FIXME: move into own class
    if( format == FMT_ASC )
    {
      // FIXME: handle more than one coverage
      final ICoverage coverage = m_coverages[0];
      final ICoreRunnableWithProgress op = new ICoreRunnableWithProgress()
      {
        @Override
        public IStatus execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException
        {
          try (IGeoGrid grid = GeoGridUtilities.toGrid( coverage ))
          {
            final AscGridExporter gridExporter = new AscGridExporter( -9999, 2 );
            monitor.beginTask( Messages.getString( "org.kalypso.gml.ui.wizard.grid.RectifiedGridCoverageExportWizard.3" ), 100 ); //$NON-NLS-1$

            /* Values */
            gridExporter.export( grid, new File( path ), monitor );
            return Status.OK_STATUS;
          }
          catch( final CoreException e )
          {
            throw e;
          }
          catch( final Exception e )
          {
            e.printStackTrace();
            throw new InvocationTargetException( e );
          }
        }
      };

      final IStatus result = RunnableContextHelper.execute( getContainer(), true, true, op );
      ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString( "org.kalypso.gml.ui.wizard.grid.RectifiedGridCoverageExportWizard.4" ), result ); //$NON-NLS-1$
      return result.isOK();
    }

    return false;
  }
}
