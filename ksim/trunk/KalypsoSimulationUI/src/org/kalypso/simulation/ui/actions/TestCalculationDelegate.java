/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.actions;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.compare.Compare;
import org.kalypso.compare.ObjectFactory;
import org.kalypso.compare.Resource;
import org.kalypso.compare.Resource.Path;
import org.kalypso.contribs.eclipse.compare.ResourceCompareInputCopy;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.calccase.CalcCaseJob;
import org.xml.sax.InputSource;

/**
 * @author belger
 */
@SuppressWarnings("restriction")
public class TestCalculationDelegate implements IWorkbenchWindowActionDelegate
{
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );

  private IWorkbenchWindow m_window;

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
   */
  public void dispose()
  {
  // nix zu disposen
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
   */
  public void init( final IWorkbenchWindow window )
  {
    m_window = window;
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run( final IAction action )
  {
    final Display display = m_window.getShell().getDisplay();

    // erstmal genauso wie eie normale berechnung
    final ISelection selection = m_window.getSelectionService().getSelection( IPageLayout.ID_RES_NAV );

    final IFolder[] calcCasesToCalc = CalcCaseHelper.chooseCalcCases( m_window.getShell(), selection,
        "Berechnung starten", "Folgende Rechenvarianten werden berechnet:" );

    if( calcCasesToCalc == null )
      return;

    for( int i = 0; i < calcCasesToCalc.length; i++ )
    {
      final IFolder folder = calcCasesToCalc[i];

      final Job diffJob = new Job( "Vergleiche Ergebnisse für: " + folder.getName() )
      {
        @Override
        protected IStatus run( final IProgressMonitor monitor )
        {
          try
          {
            monitor.beginTask( "Öffne Vergleichsdaten für: " + folder.getName(), 2000 );

            // Für jedes Vergleichspaar wird ein eigener Vergleichseditor geöffnet
            final ISelection[] selections = readTestDiffForCalcCase( folder, new SubProgressMonitor( monitor, 1000 ) );
            for( int j = 0; j < selections.length; j++ )
            {
              final ISelection resourceSel = selections[j];

              final CompareConfiguration cc = new CompareConfiguration();
              cc.setProperty( CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean( false ) );
              cc.setLeftEditable( false );
              cc.setRightEditable( false );

              final ResourceCompareInputCopy inputWrapper = new ResourceCompareInputCopy( cc );
              inputWrapper.setSelection( resourceSel );
              inputWrapper.initializeCompareConfiguration();

              final Runnable runnable = new Runnable()
              {
                public void run()
                {
                  CompareUI.openCompareEditor( inputWrapper );
                }
              };

              display.asyncExec( runnable );
            }

          }
          catch( final CoreException e )
          {
            e.printStackTrace();

            return e.getStatus();
          }
          finally
          {
            monitor.done();
          }

          return Status.OK_STATUS;
        }
      };
      diffJob.setUser( true );

      // die Berechnung durchführen
      final CalcCaseJob calcJob = new CalcCaseJob( calcCasesToCalc[i] );

      calcJob.addJobChangeListener( new JobChangeAdapter()
      {
        /**
         * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
         */
        @Override
        public void done( final IJobChangeEvent event )
        {
          // nur falls die Berechnung erfolgreich war, den diff-job starten
          if( event.getResult().isOK() )
            diffJob.schedule();
        }
      } );

      calcJob.schedule();
    }
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged( final IAction action, final ISelection selection )
  {
  // egal
  }

  protected static ISelection[] readTestDiffForCalcCase( final IFolder calcCaseFolder, final IProgressMonitor monitor )
      throws CoreException
  {
    InputStreamReader reader = null;
    try
    {
      monitor.beginTask( "Lese Vergleichskonfiguration", 1000 );

      final Unmarshaller unmarshaller = JC.createUnmarshaller();

      final IFile file = calcCaseFolder.getFile( ".test.xml" );
      reader = new InputStreamReader( new BufferedInputStream( file.getContents() ), file.getCharset() );
      final Compare compare = (Compare)unmarshaller.unmarshal( new InputSource( reader ) );
      reader.close();

      final List<JAXBElement<?>> diffList = compare.getDiff();

      final ISelection[] selections = new ISelection[diffList.size()];
      int count = 0;
      for( final Iterator<JAXBElement<?>> diffIt = diffList.iterator(); diffIt.hasNext(); )
      {
        final Object diff = diffIt.next();
        if( diff instanceof Resource )
        {
          final Resource resource = (Resource)diff;
          final List<Path> pathList = resource.getPath();
          final Resource.Path first = pathList.get( 0 );
          final Resource.Path second = pathList.get( 1 );

          final IResource firstRes = getResource( first, calcCaseFolder );
          final IResource secondRes = getResource( second, calcCaseFolder );

          selections[count++] = new StructuredSelection( new IResource[]
          {
              firstRes,
              secondRes } );
        }
        else
          throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), 0,
              "Unbekannter Diff-Typ: " + diff.getClass().getName(), null ) );
      }

      return selections;
    }
    catch( final CoreException ce )
    {
      throw ce;
    }
    catch( final Exception e )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), 0,
          "Konnte Test-Konfiguration nicht lesen.", e ) );
    }
    finally
    {
      monitor.done();
      IOUtils.closeQuietly( reader );
    }
  }

  private static final IResource getResource( final Resource.Path path, final IFolder calcCaseFolder )
      throws CoreException
  {
    final IProject project = calcCaseFolder.getProject();
    final String name = path.getName();
    final boolean relativeToCalcCase = path.isRelativeToCalcCase();
    final IResource resource = relativeToCalcCase ? calcCaseFolder.findMember( name ) : project.findMember( name );

    if( resource == null )
    {
      final String location = relativeToCalcCase ? "der Rechenvariante" : "des Projekts";
      final String message = "Pfad existiert nicht innerhalb " + location + ": " + name;
      throw new CoreException( new Status( IStatus.ERROR, KalypsoSimulationUIPlugin.getID(), 0, message, null ) );
    }

    return resource;
  }
}
