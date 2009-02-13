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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.commons.resources.FolderUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.simulation.ui.calccase.ModelNature;
import org.kalypso.simulation.ui.calccase.jface.CalcCaseTableTreeViewer;
import org.kalypso.simulation.ui.wizards.calculation.CreateCalcCasePage;

/**
 * Die Implementierung erzeugt eine völlig neue Rechenvariante im Prognoseverzeichnis
 * 
 * @author belger
 */
public class AddNewCalcCaseChoice implements IAddCalcCaseChoice
{
  private Control m_control;

  private final String m_label;

  private final IProject m_project;

  private final CreateCalcCasePage m_page;

  public static final String TOOLTIP = "Geben Sie hier die Bezeichnung der Rechenvariante ein";

  private static final SimpleDateFormat m_format = new SimpleDateFormat( "yyyy-MM-dd_HH'h'mm" );

  protected String m_name;

  private Text m_edit;

  protected IFolder m_continueFolder = null;

  public AddNewCalcCaseChoice( final String label, final IProject project, final CreateCalcCasePage page )
  {
    m_label = label;
    m_project = project;
    m_page = page;
    m_page.getClass();
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout( 2, false ) );

    final Label label = new Label( panel, SWT.NONE );
    label.setText( "Bezeichnung: " );
    label.setToolTipText( TOOLTIP );

    final GridData labelData = new GridData();
    labelData.grabExcessHorizontalSpace = false;
    labelData.horizontalAlignment = GridData.BEGINNING;
    label.setLayoutData( labelData );

    final Text edit = new Text( panel, SWT.BORDER );
    edit.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    edit.setToolTipText( TOOLTIP );
    edit.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        setName( edit.getText() );
      }
    } );
    m_edit = edit;

    // Find existing prognoses
    final IFolder prognoseFolder = m_project.getFolder( ModelNature.PROGNOSE_FOLDER );
    if( prognoseFolder == null )
    {
      final Label errorLabel = new Label( panel, SWT.NONE );
      errorLabel.setLayoutData( new GridData( GridData.FILL_BOTH ) );
      errorLabel
          .setText( "Übernahme von Handeingaben nicht möglich: Das Verzeichnis der Vorhersagevarianten existiert nicht: "
              + ModelNature.PROGNOSE_FOLDER );
      return;
    }

    final boolean isContinueAllowed = isContinueAllowed();
    if( isContinueAllowed )
      createContinueControl( panel, prognoseFolder );

    m_control = panel;

    try
    {
      refresh( new NullProgressMonitor() );
    }
    catch( final CoreException e1 )
    {
      e1.printStackTrace();
    }
  }

  private void createContinueControl( final Composite panel, final IFolder prognoseFolder )
  {
    final Button continueCheckbox = new Button( panel, SWT.CHECK );
    continueCheckbox.setText( "bestehende Rechenvariante fortsetzen" );
    continueCheckbox
        .setToolTipText( "Übernimmt die Handeingaben einer bestehenden Rechenvariante.\nJe nach Modell besteht auch die Möglichkeit, Berechnungsergebnisse als Anfangswerte zu übernehmen." );
    continueCheckbox.setLayoutData( new GridData( SWT.LEFT, SWT.CENTER, true, false, 2, 1 ) );

    // Table of existing prognoses
    final Label continueLabel = new Label( panel, SWT.NONE );
    continueLabel.setText( "Bitte wählen Sie die Rechenvariante:" );
    GridData continueLabelData = new GridData( SWT.LEFT, SWT.CENTER, false, false, 2, 1 );
    continueLabel.setLayoutData( continueLabelData );

    final CalcCaseTableTreeViewer viewer = new CalcCaseTableTreeViewer( null, panel, SWT.BORDER | SWT.SINGLE
        | SWT.FULL_SELECTION );
    viewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 ) );

    viewer.setInput( prognoseFolder );

    viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
        if( selection.isEmpty() )
          setContinueFolder( null );
        else
        {
          final IFolder folder = (IFolder)selection.getFirstElement();
          if( ModelNature.isCalcCalseFolder( folder ) )
            setContinueFolder( folder );
          else
            setContinueFolder( null );
        }
      }
    } );

    /* If odl calc cases are available, alsways select the youngest. If none available, hide table. */
    final TableTreeItem[] items = viewer.getTableTree().getItems();

    final boolean showContinue = items.length > 0;

    continueLabel.setVisible( showContinue );
    viewer.getControl().setVisible( showContinue );
    continueCheckbox.setSelection( showContinue );

    if( showContinue )
    {
      // Select topmost element ()
      viewer.setSelection( new StructuredSelection( items[0].getData() ) );
    }

    continueCheckbox.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      public void widgetSelected( SelectionEvent e )
      {
        final boolean checked = continueCheckbox.getSelection();
        continueLabel.setVisible( checked );
        viewer.getControl().setVisible( checked );

        if( checked && viewer.getSelection().isEmpty() )
        {
          // Select topmost element ()
          if( items.length > 0 )
            viewer.setSelection( new StructuredSelection( items[0].getData() ) );
        }
        else if( !checked )
          m_continueFolder = null;
      }
    } );
  }

  /**
   * Check, if continouing calc cases is possible for this project type.
   */
  private boolean isContinueAllowed()
  {
    try
    {
      final ModelNature nature = (ModelNature)m_project.getNature( ModelNature.ID );
      String prop = nature.getMetadata( ModelNature.METADATA_KEY_CALCCASE_CONTINUE_ALLOWED );
      if( prop == null )
        return false;

      final Boolean isContinueAllowed = Boolean.valueOf( prop );
      return isContinueAllowed == null ? false : isContinueAllowed.booleanValue();
    }
    catch( CoreException e )
    {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#setFocus()
   */
  public void setFocus()
  {
    m_edit.setFocus();
  }

  protected void setContinueFolder( final IFolder folder )
  {
    m_continueFolder = folder;

    validateChoice();
  }

  protected void setName( final String text )
  {
    m_name = text;

    validateChoice();
  }

  public void validateChoice()
  {
    final IStatus status = m_project.getWorkspace().validateName( m_name, IResource.FOLDER );
    if( status.isOK() )
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

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#perform(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IFolder perform( final IProgressMonitor monitor ) throws CoreException
  {
    final ModelNature nature = (ModelNature)m_project.getNature( ModelNature.ID );

    final IFolder prognoseFolder = nature.getPrognoseFolder();
    if( m_name.length() == 0 )
      throw new CoreException( StatusUtilities.createErrorStatus( "Geben Sie einen Namen für die Vorhersage ein" ) );

    final IFolder calcCaseFolder = prognoseFolder.getFolder( m_name );
    if( calcCaseFolder.exists() )
      throw new CoreException( StatusUtilities
          .createErrorStatus( "Eine Vorhersage mit diesem Namen existiert bereits: " + m_name ) );

    FolderUtilities.mkdirs( calcCaseFolder );

    final Map antProperties = configureAntProperties( m_continueFolder );
    nature.createCalculationCaseInFolder( calcCaseFolder, antProperties, monitor );

    return calcCaseFolder;
  }

  public static Map configureAntProperties( final IFolder mergeCaseFolder )
  {
    final Map map = new HashMap();

    final String mergeRelPath;
    if( mergeCaseFolder == null )
      mergeRelPath = "";
    else
      mergeRelPath = mergeCaseFolder.getProjectRelativePath().toOSString();

    map.put( "calc.merge.relpath", mergeRelPath );

    return map;
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
   * @throws CoreException
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#refresh(org.eclipse.core.runtime.IProgressMonitor)
   */
  public void refresh( final IProgressMonitor monitor ) throws CoreException
  {
    final StringBuffer buffer = new StringBuffer( "Vorhersage-" );

    buffer.append( m_format.format( new Date() ) );

    final String newName = createNewName( buffer.toString() );

    final Text edit = m_edit;
    if( !edit.isDisposed() )
    {
      edit.getDisplay().syncExec( new Runnable()
      {
        public void run()
        {
          edit.setText( newName );
        }
      } );
    }
  }

  private String createNewName( final String dateString ) throws CoreException
  {
    final ModelNature nature = (ModelNature)m_project.getNature( ModelNature.ID );
    final IFolder prognoseFolder = nature.getPrognoseFolder();
    final IResource[] resources = prognoseFolder.exists() ? prognoseFolder.members() : new IResource[] {};

    int count = 0;
    while( true )
    {
      final String newName = count == 0 ? dateString : ( dateString + "_NR" + count );

      boolean bFound = false;
      for( int i = 0; i < resources.length; i++ )
      {
        if( resources[i].getName().equals( newName ) )
        {
          bFound = true;
          break;
        }
      }

      if( !bFound )
        return newName;

      count++;
    }
  }

  /**
   * @see org.kalypso.simulation.ui.wizards.calculation.createchoices.IAddCalcCaseChoice#shouldUpdate()
   */
  public boolean shouldUpdate()
  {
    return true;
  }
}