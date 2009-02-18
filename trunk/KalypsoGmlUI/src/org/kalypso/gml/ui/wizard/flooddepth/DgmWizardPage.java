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

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.contribs.eclipse.jface.wizard.ResourceChooserGroup;
import org.kalypso.contribs.eclipse.ui.dialogs.KalypsoResourceSelectionDialog;
import org.kalypso.contribs.eclipse.ui.dialogs.ResourceSelectionValidator;

/**
 * @author Gernot Belger
 */
public class DgmWizardPage extends WizardPage implements IUpdateable
{
  private static final String PAGE_MESSAGE = "Auf dieser Seite wählen Sie das Geländemodell aus.";

  private final ResourceChooserGroup m_dgmPathGroup = new ResourceChooserGroup( this, "Geländemodell", "Datei" );

  private String m_shapeName = "";

  private IFolder m_shapeFolder;

  public DgmWizardPage( final URL contextUrl )
  {
    super( "dgmFileWizardPage", "Geländemodell", null );

    setMessage( PAGE_MESSAGE );

    if( contextUrl != null )
    {
      final IFile contextFile = ResourceUtilities.findFileFromURL( contextUrl );
      m_shapeFolder = (IFolder) contextFile.getParent();
    }
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout() );

    /* Dgm Group */
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    m_dgmPathGroup.setDialogSettings( getDialogSettings() );
    final IResource initialSelection = getDgmPath() == null ? null : root.findMember( getDgmPath() );
    final KalypsoResourceSelectionDialog dialog = new KalypsoResourceSelectionDialog( getShell(), initialSelection, "Geländemodell", new String[] { "gml" }, root, new ResourceSelectionValidator() );
    m_dgmPathGroup.setSelectionDialog( dialog );

    final Control assignmentGroup = m_dgmPathGroup.createControl( composite );
    assignmentGroup.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

    /* Shape Target Group */
    createShapeGroup( composite );

    update();

    setControl( composite );
  }

  private void createShapeGroup( final Composite parent )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    group.setLayout( new GridLayout( 3, false ) );

    /* Name */
    new Label( group, SWT.NONE ).setText( "Dateiname" );
    final Text nameText = new Text( group, SWT.BORDER );
    nameText.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    nameText.setEditable( true );
    nameText.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        handleNameTextModified( nameText.getText() );
      }
    } );
    new Label( group, SWT.NONE );

    /* Folder */
    new Label( group, SWT.NONE ).setText( "Verzeichnis" );
    final Text pathText = new Text( group, SWT.BORDER );
    pathText.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    pathText.setEditable( false );
    if( m_shapeFolder != null )
      pathText.setText( m_shapeFolder.getFullPath().toPortableString() );

    new Label( group, SWT.NONE );
//    final Button button = new Button( group, SWT.NONE );
//    button.setText( "..." );
//    button.addSelectionListener( new SelectionAdapter()
//    {
//      /**
//       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
//       */
//      @Override
//      public void widgetSelected( final SelectionEvent e )
//      {
//        handlePathButtonPressed();
//      }
//    } );
  }

  protected void handlePathButtonPressed( )
  {
//    final FileFolderSelectionDialog dialog = new FileFolderSelectionDialog( getShell(), false, IResource.FOLDER );
    // TODO: implement resource selection
    final SelectionDialog dialog = null;
//    if( m_shapeFolder != null )
//      dialog.setInitialSelection( m_shapeFolder );
    dialog.setTitle( "Shape-Datei" );
    dialog.setMessage( "Wählen Sie das Verzeichnis, in welches die Shape-Datei gespeichert wird." );
    if( !(dialog.open() == Window.OK))
      return;

//    m_shapeFolder = (IFolder) dialog.getFirstResult();
    
    update();
  }

  protected void handleNameTextModified( final String text )
  {
    m_shapeName = text;

    update();
  }

  public IPath getDgmPath( )
  {
    return m_dgmPathGroup.getPath();
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IUpdateable#update()
   */
  public void update( )
  {
    final boolean pageComplete = getDgmPath() != null && m_shapeName != null && m_shapeName.length() > 0 && m_shapeFolder != null;

    setPageComplete( pageComplete );

    if( getDgmPath() == null )
      setErrorMessage( "Es muss ein Geländemodell ausgewählt werden." );
    else if( m_shapeName == null || m_shapeName.length() == 0 )
      setErrorMessage( "Es muss ein Name für die Shape-Datei angegeben werden." );
    else if( m_shapeFolder == null )
      setErrorMessage( "Es muss ein Verzeichnis für die Shape-Datei." );
    else
    {
      setErrorMessage( null );
      setMessage( PAGE_MESSAGE );
    }
  }

  public IFile getShapeFile( )
  {
    if( m_shapeFolder == null || m_shapeName == null )
      return null;

    return m_shapeFolder.getFile( m_shapeName );
  }

}
