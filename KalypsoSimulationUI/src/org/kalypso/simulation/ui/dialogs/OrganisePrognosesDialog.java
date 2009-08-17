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
package org.kalypso.simulation.ui.dialogs;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.kalypso.contribs.eclipse.core.runtime.HandleDoneJobChangeAdapter;
import org.kalypso.contribs.java.util.Arrays;
import org.kalypso.simulation.ui.SimulationImageProvider;
import org.kalypso.simulation.ui.actions.CommitCalcCaseDelegate;
import org.kalypso.simulation.ui.actions.CommitCalcCaseDelegate.CommitCalcCaseJob;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.calccase.jface.CalcCaseTableTreeViewer;
import org.kalypso.ui.ImageProvider;

/**
 * A dialog to organize the calc cases in the .prognose folder.
 * 
 * @author belger
 */
public class OrganisePrognosesDialog extends TitleAreaDialog implements ISelectionChangedListener
{
  private static final String VORHERSAGEN_ORGANISIEREN = "Vorhersagen verwalten";


  private final Image m_titleImage;

  private CalcCaseTableTreeViewer m_viewer;

  private final IFolder m_prognoseFolder;

  private final Action m_removeAction = new Action( "Löschen", SimulationImageProvider.ID_REMOVE )
  {
    /**
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent( final Event event )
    {
      deleteCalcCases( event );
    }
  };

  private final Action m_archiveAction = new Action( "Archivieren", SimulationImageProvider.IMAGE_CALCCASE_COPY2SERVER )
  {
    /**
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent( final Event event )
    {
      archiveCalcCases();
    }
  };

  public OrganisePrognosesDialog( final Shell parentShell, final IProject modelProject )
  {
    super( parentShell );

    m_prognoseFolder = modelProject.getFolder( ModelNature.PROGNOSE_FOLDER );
    m_titleImage = ImageProvider.IMAGE_KALYPSO_ICON_BIG.createImage();
    m_archiveAction.setToolTipText("Archivieren");
    m_removeAction.setToolTipText("Löschen");

    setShellStyle( getShellStyle() | SWT.RESIZE );
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#close()
   */
  @Override
  public boolean close()
  {
    m_titleImage.dispose();

    if( m_viewer != null )
      m_viewer.removeSelectionChangedListener( this );

    return super.close();
  }

  /**
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    setTitleImage( m_titleImage );

    getShell().setText( VORHERSAGEN_ORGANISIEREN );
    setTitle( VORHERSAGEN_ORGANISIEREN );
    setMessage( "Wählen Sie eine Vorhersage aus und löschen oder archivieren Sie diese." );

    // toolbar
    final ToolBarManager manager = new ToolBarManager( SWT.FLAT );
    manager.add( m_removeAction );
    manager.add( m_archiveAction );

    final ToolBar bar = manager.createControl( parent );
    bar.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

    // Tabelle einbauen, die die Vorhersagevarianten zeigt
    m_viewer = new CalcCaseTableTreeViewer( null, parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION );
    m_viewer.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ) );
    m_viewer.addSelectionChangedListener( this );

    m_viewer.setSelection( new StructuredSelection() );

    m_viewer.setAutoExpandLevel( 2 );

    m_viewer.setInput( m_prognoseFolder );

    return super.createDialogArea( parent );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  public void selectionChanged( final SelectionChangedEvent event )
  {
    // update actions: enabled, if only calc cases are selected (minimum 1)
    final IStructuredSelection selection = (IStructuredSelection)event.getSelection();
    final Object[] objects = selection.toArray();
    boolean enable = !selection.isEmpty();
    for( final Object element : objects )
    {
      if( !(element instanceof IFolder) || !ModelNature.isCalcCalseFolder( (IFolder)element ) )
      {
        enable = false;
        break;
      }
    }

    m_removeAction.setEnabled( enable );
    m_archiveAction.setEnabled( enable );
  }

  protected void deleteCalcCases( final Event event )
  {
    final IStructuredSelection selection = (IStructuredSelection)m_viewer.getSelection();
    final List<?> list = selection.toList();

    // TODO: does now work any more, as the workbench
    // now relies on the fact that the resources MUST be selected ni the
    final DeleteResourceAction action = new DeleteResourceAction( new SameShellProvider( getContents() ) )
    {
      /**
       * @see org.eclipse.ui.actions.BaseSelectionListenerAction#getStructuredSelection()
       */
      @Override
      public IStructuredSelection getStructuredSelection( )
      {
        return selection;
      }

      @Override
      protected List<?> getSelectedResources()
      {
        return list;
      }
    };
    action.runWithEvent( event );

    m_viewer.refresh();
  }

  protected void archiveCalcCases()
  {
    final IStructuredSelection selection = (IStructuredSelection)m_viewer.getSelection();
    if( selection.isEmpty() )
      return;

    final Object[] calcCaseObjs = selection.toArray();
    final IFolder[] calcCases = Arrays.castArray( calcCaseObjs, new IFolder[calcCaseObjs.length] );

    final CommitCalcCaseJob job = new CommitCalcCaseDelegate.CommitCalcCaseJob( m_prognoseFolder.getProject(),
        calcCases );

    // TODO see if autoRemoveListener (argument of HandleDoneJobChangeAdapter) should be true?
    job.addJobChangeListener( new HandleDoneJobChangeAdapter( getParentShell(), VORHERSAGEN_ORGANISIEREN,
        CommitCalcCaseDelegate.RECHENVARIANTEN_KOENNEN_NICHT_ARCHIVIERT_WERDEN_, false, false ) );
    job.setUser( true );
    job.schedule();
  }

}
