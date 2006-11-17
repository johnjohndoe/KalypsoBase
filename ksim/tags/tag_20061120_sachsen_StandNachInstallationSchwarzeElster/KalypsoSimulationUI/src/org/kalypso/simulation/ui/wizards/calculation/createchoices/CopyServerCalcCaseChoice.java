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
package org.kalypso.simulation.ui.wizards.calculation.createchoices;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.viewers.FileLabelProvider;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.calccase.ModelSynchronizer;
import org.kalypso.simulation.ui.wizards.calculation.CreateCalcCasePage;

/**
 * Diese Implementierung erzeugt einen völlig neuen Rechenfall im Prognoseverzeichnis
 * 
 * @author belger
 */
public class CopyServerCalcCaseChoice implements IAddCalcCaseChoice
{
  private Control m_control;

  private File m_dir;

  private final String m_label;

  private final IProject m_project;

  private ListViewer m_viewer;

  private Collection m_serverDirs = new LinkedList();

  private final CreateCalcCasePage m_page;

  private String m_name;

  private File m_serverPrognoseDir;

  private final ModelSynchronizer m_synchronizer;

  public CopyServerCalcCaseChoice( final String label, final IProject project, final CreateCalcCasePage page,
      final ModelSynchronizer synchronizer )
  {
    m_label = label;
    m_project = project;
    m_page = page;
    m_synchronizer = synchronizer;
    m_serverPrognoseDir = new File( m_synchronizer.getServerRoot(), ModelNature.PROGNOSE_FOLDER );
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout() );

    final Label nameLabel = new Label( panel, SWT.NONE );
    nameLabel.setLayoutData( new GridData() );
    nameLabel.setText( "neue Bezeichnung:" );
    nameLabel.setToolTipText( AddNewCalcCaseChoice.TOOLTIP );

    final Text edit = new Text( panel, SWT.BORDER );
    edit.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    edit.setToolTipText( AddNewCalcCaseChoice.TOOLTIP );
    edit.addModifyListener( new ModifyListener()
    {
      public void modifyText( ModifyEvent e )
      {
        setName( edit.getText() );
      }
    } );

    final Label label = new Label( panel, SWT.NONE );
    label.setText( "auf dem Server vorliegende Hochwasser-Vorhersagen:" );

    final ListViewer viewer = new ListViewer( panel, SWT.BORDER );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new FileLabelProvider() );

    viewer.setInput( m_serverDirs );

    final GridData viewerData = new GridData( GridData.FILL_BOTH );
    viewerData.horizontalSpan = 2;
    viewer.getControl().setLayoutData( viewerData );

    viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
        if( selection.isEmpty() )
          setDir( null );
        else
          setDir( (File)selection.getFirstElement() );
      }
    } );
    ViewerUtilities.selectFirstElement( viewer );
    m_viewer = viewer;

    m_control = panel;

    // may take a while and blocks the gui, so do it in a job
    final Job job = new Job( "Archivierte Rechenvarianten werden ermittelt" )
    {
      protected IStatus run( IProgressMonitor monitor )
      {
        refresh( new NullProgressMonitor() );
        return Status.OK_STATUS;
      }
    };

    job.setUser( true );
    job.schedule();
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#setFocus()
   */
  public void setFocus()
  {
    m_viewer.getControl().setFocus();
  }

  protected void setName( final String text )
  {
    m_name = text;

    validateChoice();
  }

  protected void setDir( final File dir )
  {
    m_dir = dir;

    validateChoice();
  }

  public void refresh( final IProgressMonitor monitor )
  {
    // alle Prognosen finden
    final File[] serverCalcCases = m_serverPrognoseDir.listFiles();
    final boolean bEnabled = m_serverPrognoseDir.exists() && serverCalcCases != null;

    m_serverDirs.clear();
    if( serverCalcCases != null )
    {
      for( int i = 0; i < serverCalcCases.length; i++ )
        m_serverDirs.add( serverCalcCases[i] );
    }

    final File newSelectFinal = ( serverCalcCases == null || serverCalcCases.length == 0 ) ? null : serverCalcCases[0];
    final Viewer viewer = m_viewer;
    final Control control = m_control;
    if( viewer != null )
    {
      viewer.getControl().getDisplay().syncExec( new Runnable()
      {
        public void run()
        {
          viewer.refresh();

          if( newSelectFinal == null )
            viewer.setSelection( StructuredSelection.EMPTY );
          else
            viewer.setSelection( new StructuredSelection( newSelectFinal ) );

          control.setEnabled( bEnabled );
        }
      } );
    }
  }

  /**
   * @throws CoreException
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#perform(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IFolder perform( final IProgressMonitor monitor ) throws CoreException
  {
    final ModelNature nature = (ModelNature)m_project.getNature( ModelNature.ID );

    final IFolder folder = nature.getPrognoseFolder();

    if( m_name.length() == 0 )
      throw new CoreException( StatusUtilities.createErrorStatus( "Geben Sie einen Namen für die Vorhersage ein" ) );

    final IFolder calcCaseFolder = folder.getFolder( m_name );
    if( calcCaseFolder.exists() )
      throw new CoreException( StatusUtilities
          .createErrorStatus( "Eine Vorhersage mit diesem Namen existiert bereits: " + m_name ) );

    if( m_dir == null )
      throw new CoreException( StatusUtilities
          .createErrorStatus( "Es muss eine vorhandene Berechnung ausgewählt werden" ) );

    // quellverzeichnis holen
    m_synchronizer.getFolder( m_dir, ModelNature.PROGNOSE_FOLDER + "/" + m_name, monitor );

    return calcCaseFolder;
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#getControl()
   */
  public Control getControl()
  {
    return m_control;
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#toString()
   */
  public String toString()
  {
    return m_label;
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#shouldUpdate()
   */
  public boolean shouldUpdate()
  {
    return false;
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#validateChoice()
   */
  public void validateChoice()
  {
    // do not do anything if we are not the current choice
    if( m_page.getCurrentChoice() != this )
      return;

    if( m_serverDirs.isEmpty() )
    {
      m_page.setErrorMessage( null );
      m_page.setMessage( "Keine archivierten Rechenvarianten vorhanden", IMessageProvider.WARNING );
      m_page.setPageComplete( false );
      return;
    }

    if( m_dir == null )
    {
      m_page.setErrorMessage( "Es muss eine vorhandene Rechenvariante ausgewählt werden." );
      m_page.setMessage( null );
      m_page.setPageComplete( false );
    }

    final IStatus status = m_project.getWorkspace().validateName( m_name, IResource.FOLDER );
    if( status.getSeverity() == IStatus.OK )
    {
      m_page.setErrorMessage( null );
      m_page.setMessage( null );
      m_page.setPageComplete( true );
    }
    else
    {
      m_page.setErrorMessage( status.getMessage() );
      m_page.setMessage( null );
      m_page.setPageComplete( false );
    }
  }
}