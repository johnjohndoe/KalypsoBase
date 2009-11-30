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
package org.kalypso.gml.ui.wizard.grid;

import java.io.File;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.grid.GridFileVerifier;
import org.kalypso.grid.IGridMetaReader;
import org.kalypso.transformation.ui.CRSSelectionListener;
import org.kalypso.transformation.ui.CRSSelectionPanel;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;

/**
 * @author Dirk Kuch
 */
public class PageSelectGeodataFile extends WizardPage
{
  private static final String SETTINGS_FILE_PATH = "fullFilePath"; //$NON-NLS-1$

  private static final String SETTINGS_SRS_NAME = "srsName"; //$NON-NLS-1$

  protected Button m_buttonFile;

  protected Text m_tFile;

  protected String m_sFile;

  protected GridFileVerifier verifier;

  protected StructuredSelection m_comboSel;

  /* details */
  private IGridMetaReader m_rasterReader = null;

  boolean m_bBoxesSet = false;

  Group m_detailsGroup;

  private Text tUlcY;

  private Text tUlcX;

  private Text tVectorYy;

  private Text tVectorYx;

  private Text tVectorXy;

  private Text tVectorXx;

  protected final WizardNewFileCreationPage m_creationPage;

  private CRSSelectionPanel m_crsPanel;

  protected String m_crs;

  public PageSelectGeodataFile( final WizardNewFileCreationPage creationPage )
  {
    super( "importGeoData" ); //$NON-NLS-1$
    m_creationPage = creationPage;
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    setPageComplete( false );

    final Composite container = new Composite( parent, SWT.NULL );
    container.setLayout( new GridLayout() );
    setControl( container );

    final Group fileGroup = new Group( container, SWT.NONE );
    fileGroup.setLayout( new GridLayout( 2, false ) );
    fileGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    fileGroup.setText( "Rasterdatei" ); //$NON-NLS-1$

// final Label label = new Label( container, SWT.NONE );
// label.setLayoutData( new GridData( GridData.BEGINNING, GridData.CENTER, false, false, 2, 0 ) );
// label.setText( "Selektieren Sie die Rasterdatei, die importiert werden soll:" );

    m_tFile = new Text( fileGroup, SWT.BORDER );
    m_tFile.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        m_sFile = m_tFile.getText();
        updatePageComplete();

        m_detailsGroup.setVisible( true );

        /* set proposed file name */
        final String[] parts = new Path( m_tFile.getText() ).toFile().getName().split( "\\." ); //$NON-NLS-1$
        if( parts.length == 2 && m_creationPage != null )
        {
          m_creationPage.setFileName( parts[0] + ".gml" ); //$NON-NLS-1$
        }

      }
    } );
    m_tFile.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    m_buttonFile = new Button( fileGroup, SWT.NONE );
    m_buttonFile.setText( "..." ); //$NON-NLS-1$
    m_buttonFile.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        browseForFile();
      }
    } );

    /* Coordinate system combo */
    m_crsPanel = new CRSSelectionPanel( container, SWT.NONE );
    m_crsPanel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    m_crsPanel.setToolTipText( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.0") ); //$NON-NLS-1$
    m_crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    m_crsPanel.setSelectedCRS( m_crs );
    m_crsPanel.addSelectionChangedListener( new CRSSelectionListener()
    {
      @Override
      protected void selectionChanged( final String selectedCRS )
      {
        m_crs = selectedCRS;
        updatePageComplete();
      }

    } );

    m_detailsGroup = new Group( container, SWT.NONE );
    m_detailsGroup.setText( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.1") ); //$NON-NLS-1$
    m_detailsGroup.setLayout( new GridLayout( 2, false ) );
    m_detailsGroup.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    m_detailsGroup.setVisible( false );

    // VectorXx
    final Label lVectorXx = new Label( m_detailsGroup, SWT.NONE );
    lVectorXx.setText( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.2") ); //$NON-NLS-1$

    tVectorXx = new Text( m_detailsGroup, SWT.BORDER );
    tVectorXx.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    tVectorXx.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        checkBoxes();
      }
    } );

    // VectorXy
    final Label lVectorXy = new Label( m_detailsGroup, SWT.NONE );
    lVectorXy.setText( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.3") ); //$NON-NLS-1$

    tVectorXy = new Text( m_detailsGroup, SWT.BORDER );
    tVectorXy.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    tVectorXy.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        checkBoxes();
      }
    } );

    // VectorYx
    final Label lVectorYx = new Label( m_detailsGroup, SWT.NONE );
    lVectorYx.setText( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.4") ); //$NON-NLS-1$

    tVectorYx = new Text( m_detailsGroup, SWT.BORDER );
    tVectorYx.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    tVectorYx.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        checkBoxes();
      }
    } );

    // VectorYy
    final Label lVectorYy = new Label( m_detailsGroup, SWT.NONE );
    lVectorYy.setText( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.5") ); //$NON-NLS-1$

    tVectorYy = new Text( m_detailsGroup, SWT.BORDER );
    tVectorYy.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    tVectorYy.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        checkBoxes();
      }
    } );

    // Upper left corner (X)
    final Label lUlcX = new Label( m_detailsGroup, SWT.NONE );
    lUlcX.setText( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.6") ); //$NON-NLS-1$

    tUlcX = new Text( m_detailsGroup, SWT.BORDER );
    tUlcX.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    tUlcX.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        checkBoxes();
      }
    } );

    // Upper left corner (Y)
    final Label lUlcY = new Label( m_detailsGroup, SWT.NONE );
    lUlcY.setText( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.7") ); //$NON-NLS-1$

    tUlcY = new Text( m_detailsGroup, SWT.BORDER );
    tUlcY.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    tUlcY.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        checkBoxes();
      }
    } );

    updatePageComplete();

    final IDialogSettings dialogSettings = getDialogSettings();
    final String fullPath = dialogSettings.get( SETTINGS_FILE_PATH );

    if( fullPath != null )
      m_tFile.setText( fullPath );

  }

  protected void updatePageComplete( )
  {
    setPageComplete( false );

    final IPath docLocation = new Path( m_tFile.getText() );

    final File toFile = docLocation.toFile();
    if( (docLocation == null) || !toFile.exists() )
    {
      setMessage( null );
      setErrorMessage( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.8") ); //$NON-NLS-1$

      return;
    }

    verifier = new GridFileVerifier();
    try
    {
      if( !GridFileVerifier.verify( toFile.toURI().toURL() ) )
      {
        setMessage( null );
        setErrorMessage( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.9") ); //$NON-NLS-1$

        return;
      }
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      return;
    }

    if( m_crs == null )
    {
      setMessage( null );
      setErrorMessage( Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.10") ); //$NON-NLS-1$

      return;
    }

    if( m_crs != null )
    {
      try
      {
        m_rasterReader = GridFileVerifier.getRasterMetaReader( docLocation.toFile().toURI().toURL(), m_crs );
      }
      catch( final MalformedURLException e )
      {
        e.printStackTrace();
      }

      updateTextBoxes( m_rasterReader );
    }

    setPageComplete( true );
    setMessage( null );
    setErrorMessage( null );

    /* If page is complete, we may save the settings */
    getDialogSettings().put( SETTINGS_FILE_PATH, m_sFile );
    getDialogSettings().put( SETTINGS_SRS_NAME, m_crs );

    // TODO: why disable these controls here?
// m_tFile.setEnabled( false );
// m_buttonFile.setEnabled( false );
  }

  protected void checkBoxes( )
  {
    setMessage( null );
    setErrorMessage( null );

    String text = ""; //$NON-NLS-1$

    if( containsIllegalChars( tVectorXx.getText() ) )
    {
      text += Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.11"); //$NON-NLS-1$
    }
    if( containsIllegalChars( tVectorXy.getText() ) )
    {
      text += Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.12"); //$NON-NLS-1$
    }
    if( containsIllegalChars( tVectorYx.getText() ) )
    {
      text += Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.13"); //$NON-NLS-1$
    }
    if( containsIllegalChars( tVectorYy.getText() ) )
    {
      text += Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.14"); //$NON-NLS-1$
    }
    if( containsIllegalChars( tUlcX.getText() ) )
    {
      text += Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.15"); //$NON-NLS-1$
    }
    if( containsIllegalChars( tUlcY.getText() ) )
    {
      text += Messages.getString("org.kalypso.gml.ui.wizard.grid.PageSelectGeodataFile.16"); //$NON-NLS-1$
    }

    if( !"".equals( text ) ) //$NON-NLS-1$
    {
      setErrorMessage( text );

      setPageComplete( false );
      return;
    }

    setMessage( null );
    setErrorMessage( null );

    setPageComplete( true );
  }

  private boolean containsIllegalChars( final String text )
  {
    final Pattern p = Pattern.compile( "[+-]?[\\d]+[\\.]?[\\d]*?" ); //$NON-NLS-1$
    final Matcher m = p.matcher( text.toLowerCase() );
    return !m.matches();
  }

  protected void updateTextBoxes( final IGridMetaReader reader )
  {
    if( reader == null )
    {
      return;
    }

    tVectorXx.setText( Double.toString( reader.getVectorXx() ) );
    tVectorYy.setText( Double.toString( reader.getVectorYy() ) );
    tVectorXy.setText( Double.toString( reader.getVectorXy() ) );
    tVectorYx.setText( Double.toString( reader.getVectorYx() ) );
    tUlcX.setText( Double.toString( reader.getOriginCornerX() ) );
    tUlcY.setText( Double.toString( reader.getOriginCornerY() ) );
  }

  protected void browseForFile( )
  {
    final IPath path = browse();
    if( path != null )
      m_tFile.setText( path.toString() );
  }

  private IPath browse( )
  {
    final FileDialog dialog = new FileDialog( getShell(), SWT.OPEN );
    dialog.setFilterExtensions( ImportGridUtilities.SUPPORTED_GRID_FILE_PATTERNS );
    dialog.setFilterNames( ImportGridUtilities.SUPPORTED_GRID_FILE_NAMES );

    final String result = dialog.open();
    if( result == null )
      return null;

    return new Path( result );
  }

  public File getSelectedFile( )
  {
    final File file = new File( m_sFile );
    if( !file.exists() )
    {
      throw (new IllegalStateException());
    }

    return file;
  }

  public String getProjection( )
  {
    return m_crs;
  }

  public RectifiedGridDomain.OffsetVector getOffsetX( )
  {
    return new RectifiedGridDomain.OffsetVector( Double.valueOf( tVectorXx.getText().replaceAll( ",", "." ) ), Double.valueOf( tVectorXy.getText().replaceAll( ",", "." ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public RectifiedGridDomain.OffsetVector getOffsetY( )
  {
    return new RectifiedGridDomain.OffsetVector( Double.valueOf( tVectorYx.getText().replaceAll( ",", "." ) ), Double.valueOf( tVectorYy.getText().replaceAll( ",", "." ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public Double[] getUpperLeftCorner( )
  {
    // TOD: user Number utils
    return new Double[] { Double.valueOf( tUlcX.getText().replaceAll( ",", "." ) ), Double.valueOf( tUlcY.getText().replaceAll( ",", "." ) ) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public IGridMetaReader getReader( )
  {
    return m_rasterReader;
  }
}