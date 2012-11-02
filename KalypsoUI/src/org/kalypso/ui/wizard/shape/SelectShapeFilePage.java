/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.ui.wizard.shape;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.transformation.ui.CRSSelectionPanel;
import org.kalypso.transformation.ui.listener.CRSSelectionListener;
import org.kalypso.ui.controls.files.FileChooserComposite;
import org.kalypso.ui.controls.files.listener.IFileChooserListener;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * General page that enables the user to select a shape file in the file system and it's coordinate system.<br/>
 * TODO: use data binding.<br/>
 * TODO: use dialog settings
 * 
 * @author Holger Albert
 */
public class SelectShapeFilePage extends WizardPage implements IShapeFileSelection
{
  private static final String SETTINGS_FILE = "shapeFile"; //$NON-NLS-1$

  private static final String SETTINGS_SRS = "sourceSRS"; //$NON-NLS-1$

  /**
   * The selected shape file.<br/>
   * TODO: change to {@link File}
   */
  protected String m_shapeFile = null;

  /**
   * The selected source coordinate system.
   */
  protected String m_sourceCRS = null;

  /**
   * The constructor.
   * 
   * @param pageName
   *          The name of the page.
   */
  public SelectShapeFilePage( final String pageName )
  {
    super( pageName );
  }

  /**
   * The constructor.
   * 
   * @param pageName
   *          The name of the page.
   * @param title
   *          The title for this wizard page, or null if none.
   * @param titleImage
   *          The image descriptor for the title of this wizard page, or null if none.
   */
  public SelectShapeFilePage( final String pageName, final String title, final ImageDescriptor titleImage )
  {
    super( pageName, title, titleImage );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    init();

    /* The content. */
    final Composite content = new Composite( parent, SWT.NONE );
    GridLayoutFactory.swtDefaults().applyTo( content );

    /* Create the file chooser composite. */
    final FileChooserComposite fileComposite = new FileChooserComposite( content, FileChooserComposite.NO_GROUP, new String[] { "*.SHP", "*.*" }, new String[] { Messages.getString( "SelectShapeFilePage_2" ), Messages.getString( "SelectShapeFilePage_3" ) }, Messages.getString( "SelectShapeFilePage_4" ), m_shapeFile ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    fileComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    fileComposite.addFileChooserListener( new IFileChooserListener()
    {
      @Override
      public void pathChanged( final String path )
      {
        /* Store the text. */
        m_shapeFile = path;

        /* Check, if the page can be completed. */
        checkPageComplete();
      }
    } );

    /* Create the CRS selection panel. */
    final CRSSelectionPanel crsPanel = new CRSSelectionPanel( content, SWT.NONE );
    crsPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    crsPanel.addSelectionChangedListener( new CRSSelectionListener()
    {
      @Override
      protected void selectionChanged( final String selectedCRS )
      {
        /* Store the coordinate system. */
        m_sourceCRS = selectedCRS;

        /* Check, if the page can be completed. */
        checkPageComplete();
      }
    } );

    /* Set the control to the page. */
    setControl( content );

    /* Check page, but do not show error message */
    checkPageComplete();
    setMessage( null );
  }

  private void init( )
  {
    final IDialogSettings settings = getDialogSettings();
    if( settings == null )
      return;

    m_shapeFile = settings.get( SETTINGS_FILE );
    m_sourceCRS = settings.get( SETTINGS_SRS );
  }

  /**
   * This function checks, if the page can be completed.
   */
  protected void checkPageComplete( )
  {
    /* The wizard page can be completed. */
    setMessage( null );
    setPageComplete( true );

    if( m_shapeFile == null || m_shapeFile.length() == 0 )
    {
      setMessage( Messages.getString( "SelectShapeFilePage_5" ), ERROR ); //$NON-NLS-1$
      setPageComplete( false );
      return;
    }

    final File shapeFile = new File( m_shapeFile );
    if( !shapeFile.exists() )
    {
      setMessage( String.format( Messages.getString( "SelectShapeFilePage_6" ), shapeFile.getName() ), ERROR ); //$NON-NLS-1$
      setPageComplete( false );
      return;
    }

    if( m_sourceCRS == null || m_sourceCRS.length() == 0 )
    {
      setMessage( Messages.getString( "SelectShapeFilePage_7" ), ERROR ); //$NON-NLS-1$
      setPageComplete( false );
      return;
    }
  }

  /**
   * This function returns the selected shape file.
   * 
   * @return The selected shape file.
   */
  @Override
  public String getShapeFile( )
  {
    return m_shapeFile;
  }

  /**
   * This function returns the selected source coordinate system.
   * 
   * @return The selected source coordinate system.
   */
  @Override
  public String getSoureCRS( )
  {
    return m_sourceCRS;
  }
}