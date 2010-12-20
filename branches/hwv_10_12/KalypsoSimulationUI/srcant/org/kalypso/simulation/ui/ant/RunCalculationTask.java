/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.simulation.ui.ant;

import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.simulation.core.simspec.Modeldata;
import org.kalypso.simulation.ui.calccase.simulation.SimulationFactory;

/**
 * This ant task starts the calculation of a kalypso calcCase.
 * 
 * @author belger
 */
public class RunCalculationTask extends Task
{
  private static final org.kalypso.simulation.core.simspec.ObjectFactory OF = new org.kalypso.simulation.core.simspec.ObjectFactory();

  private String m_calcCasePath;

  private final Modeldata m_modeldata;

  private IContainer m_calcCaseFolder;

  public RunCalculationTask( )
  {
    m_modeldata = OF.createModeldata();
  }

  public String getCalcCaseFolder( )
  {
    return m_calcCasePath;
  }

  public void setCalcCaseFolder( final String calcCaseFolder )
  {
    m_calcCasePath = calcCaseFolder;
  }

  public void setTypeID( final String typeID )
  {
    m_modeldata.setTypeID( typeID );
  }

  public void addConfiguredInput( final Modeldata.Input input )
  {
    m_modeldata.getInput().add( input );
  }

  public void addConfiguredOutput( final Modeldata.Output output )
  {
    m_modeldata.getOutput().add( output );
  }

  public void addConfiguredclearAfterCalc( final Modeldata.ClearAfterCalc clearAfterCalc )
  {
    m_modeldata.getClearAfterCalc().add( clearAfterCalc );
  }

  private void validateInput( )
  {
    if( m_calcCasePath == null )
      throw new BuildException( "calcCaseFolder is not set" );

    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IResource member = root.findMember( Path.fromPortableString( m_calcCasePath ) );
    if( !(member instanceof IContainer) )
      throw new BuildException( "calcCaseFolder not found: " + m_calcCaseFolder );

    m_calcCaseFolder = (IContainer) member;
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  @Override
  public void execute( ) throws BuildException
  {
    // give implementing class a chance to validate its input
    validateInput();

    final Project antProject = getProject();
    final Hashtable< ? , ? > references = antProject == null ? null : antProject.getReferences();
    final IProgressMonitor monitor = references == null ? null : (IProgressMonitor) references.get( AntCorePlugin.ECLIPSE_PROGRESS_MONITOR );

    IStatus status = null;
    try
    {
      status = SimulationFactory.runCalculation( m_calcCaseFolder, new SubProgressMonitor( monitor, 1 ), m_modeldata );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
      status = StatusUtilities.statusFromThrowable( t );
    }

    if( PlatformUI.isWorkbenchRunning() )
    {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      // REMARK: getActiveWorkbenchWindow return null here, because we are not in the display thread
      // so we just take the shell from the first window, this is ok in most cases
      final IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
      final Shell shell = window == null ? null : window.getShell();
      if( !status.isOK() )
        handleError( shell, status );
    }
    else
    {
      final Throwable t = status.getException();
      if( t != null )
        t.printStackTrace();
    }
  }

  private void handleError( final Shell shell, final IStatus status )
  {
    shell.getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        ErrorDialog.openError( shell, "Modellrechnung", "Modellrechnung wurde durchgeführt", status, IStatus.ERROR | IStatus.WARNING | IStatus.INFO | IStatus.CANCEL | IStatus.OK );
      }
    } );
  }
}