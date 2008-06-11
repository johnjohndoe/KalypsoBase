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
package org.kalypso.gml.ui.wizard.flooddepth;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob;
import org.kalypso.gml.processes.floodDepth.FloodDepthCalcJob.TYPE;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;

/**
 * @author Gernot Belger
 */
public class FloodDepthWizard extends Wizard implements IWizard
{
  private final URL m_meshUrl;

  private DgmWizardPage m_dgmFilePage;

  private IFile m_shapeFile;

  private FloodDepthConfigPage m_configPage;

  public FloodDepthWizard( final URL meshUrl )
  {
    m_meshUrl = meshUrl;
    
    setWindowTitle( "Geländemodell abtragen" );
    setNeedsProgressMonitor( true );
    setDialogSettings( PluginUtilities.getDialogSettings( KalypsoGmlUIPlugin.getDefault(), getClass().getName() ) );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#addPages()
   */
  @Override
  public void addPages( )
  {
    m_dgmFilePage = new DgmWizardPage( m_meshUrl );
    
    m_configPage = new FloodDepthConfigPage();
    addPage( m_dgmFilePage );
    addPage( m_configPage );

    super.addPages();
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final URL meshUrl = m_meshUrl;
    final DgmWizardPage dgmFilePage = m_dgmFilePage;
    final TYPE floodDepthType = m_configPage.getType();
    final double floodDepthStep = m_configPage.getStep();
    
    final ICoreRunnableWithProgress runnable = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          monitor.beginTask( "Höhenmodelle werden voneinander abgetragen", 100 );

          final IPath dgmPath = dgmFilePage.getDgmPath();
          final String dtmUrlStr = ResourceUtilities.createURLSpec( dgmPath );
          final URL dtmGmlUrl = new URL( dtmUrlStr );

          final IFile shapeFile = dgmFilePage.getShapeFile();
          final String shapeBase = shapeFile.getLocation().toOSString();
          
          FloodDepthCalcJob.doFloodDepthIntersection( shapeBase, new SubProgressMonitor( monitor, 100 ), meshUrl, dtmGmlUrl, floodDepthType, floodDepthStep );
          
          shapeFile.getParent().refreshLocal( IResource.DEPTH_ONE, new SubProgressMonitor( monitor, 5 ) );
          
          setShapeFile( shapeFile );
          
          return Status.OK_STATUS;
        }
        catch( final Exception e )
        {
          throw new InvocationTargetException( e );
        }
      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, true, runnable );
    ErrorDialog.openError( getContainer().getShell(), getWindowTitle(), "Fehler bei der Fließtiefenermittlung", status );
    return status.isOK();
  }

  protected void setShapeFile( final IFile shapeFile )
  {
    m_shapeFile = shapeFile;
  }

  public IFile getShapeFile( )
  {
    return m_shapeFile;
  }
}
