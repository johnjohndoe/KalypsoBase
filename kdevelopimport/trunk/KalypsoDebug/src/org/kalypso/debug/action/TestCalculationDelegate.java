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
package org.kalypso.debug.action;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.internal.ResourceCompareInputWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.kalypso.compare.CompareType;
import org.kalypso.compare.ObjectFactory;
import org.kalypso.compare.ResourceType;
import org.kalypso.compare.ResourceType.PathType;
import org.kalypso.debug.KalypsoDebugPlugin;
import org.kalypso.ui.action.CalcCaseHelper;
import org.xml.sax.InputSource;

/**
 * @author belger
 */
public class TestCalculationDelegate implements IWorkbenchWindowActionDelegate
{
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
    final ISelection selection = m_window.getSelectionService().getSelection(
        IPageLayout.ID_RES_NAV );

    final IFolder[] calcCasesToCalc = CalcCaseHelper.chooseCalcCases( m_window.getShell(),
        selection, "Berechnung starten", "Folgende Rechenvarianten werden berechnet:" );

    if( calcCasesToCalc == null )
      return;

    for( int i = 0; i < calcCasesToCalc.length; i++ )
    {
      final IFolder folder = calcCasesToCalc[i];

      // dann ein diff-ant-skript ausführen

      final Job job = new Job( "Vergleiche Ergebnisse" )
      {
        protected IStatus run( final IProgressMonitor monitor )
        {
          try
          {
            final Unmarshaller unmarshaller = new ObjectFactory().createUnmarshaller();
            final IFile file = folder.getFile( ".test.xml" );
            final InputStreamReader reader = new InputStreamReader( new BufferedInputStream( file
                .getContents() ), file.getCharset() );
            final CompareType compare = (CompareType)unmarshaller.unmarshal( new InputSource(
                reader ) );

            final IProject project = folder.getProject();

            final List diffList = compare.getDiff();
            for( final Iterator diffIt = diffList.iterator(); diffIt.hasNext(); )
            {
              final Object diff = diffIt.next();
              if( diff instanceof ResourceType )
              {
                final ResourceType resource = (ResourceType)diff;
                final List pathList = resource.getPath();
                final ResourceType.PathType first = (PathType)pathList.get( 0 );
                final ResourceType.PathType second = (PathType)pathList.get( 1 );

                final IResource firstRes = first.isRelativeToCalcCase() ? folder.findMember( first
                    .getName() ) : project.findMember( first.getName() );
                final IResource secondRes = second.isRelativeToCalcCase() ? folder
                    .findMember( second.getName() ) : project.findMember( second.getName() );

                if( firstRes == null || secondRes == null )
                  return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0,
                      "Resourcen-Diff enthält Resource, die nicht vorhanden sind.", null );

                final CompareConfiguration cc = new CompareConfiguration();
                cc.setProperty( CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean( false ) );
                cc.setLeftEditable( false );
                cc.setRightEditable( false );

                final ResourceCompareInputWrapper inputWrapper = new ResourceCompareInputWrapper(
                    cc );
                inputWrapper.setSelection( new StructuredSelection( new IResource[]
                {
                    firstRes,
                    secondRes } ) );
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
              else
                return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0,
                    "Unbekannter Diff-Typ: " + diff.getClass().getName(), null );
            }

          }
          catch( final JAXBException e )
          {
            e.printStackTrace();

            return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0,
                "Konnte Test-Konfiguration nicht lesen.", e );
          }
          catch( final UnsupportedEncodingException e )
          {
            e.printStackTrace();

            return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0,
                "Konnte Test-Konfiguration nicht lesen.", e );
          }
          catch( CoreException e )
          {
            e.printStackTrace();
          }

          return Status.OK_STATUS;
        }
      };
      job.setUser( true );
      job.schedule();
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
}
