/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraï¿½e 22
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.grid.GridFileVerifier;
import org.kalypso.grid.IGridMetaReader;
import org.kalypso.transformation.ui.CRSSelectionListener;
import org.kalypso.transformation.ui.CRSSelectionPanel;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;

/**
 * @author Dirk Kuch
 */
public class PageSelectGeodataFile extends WizardPage
{
  private static final String SETTINGS_FILE_PATH = "fullFilePath";

  private static final String SETTINGS_SRS_NAME = "srsName";

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

  private String m_crs;

  public PageSelectGeodataFile( final WizardNewFileCreationPage creationPage )
  {
    super( "importGeoData" );
    m_creationPage = creationPage;

    setTitle( "Rasterdaten-Import" );
    setDescription( "Wählen Sie aus, welche Rasterdatei importiert werden soll." );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    setPageComplete( false );

    final Composite container = new Composite( parent, SWT.NULL );
    container.setLayout( new GridLayout( 2, false ) );
    setControl( container );

    final Label lFiller = new Label( container, SWT.NONE );
    lFiller.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false, 2, 0 ) );

    final Label label = new Label( container, SWT.NONE );
    label.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false, 2, 0 ) );
    label.setText( "Selektieren Sie die Rasterdatei, die importiert werden soll:" );

    m_tFile = new Text( container, SWT.BORDER );
    m_tFile.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        m_sFile = m_tFile.getText();
        updatePageComplete();

        m_detailsGroup.setVisible( true );

        /* set proposed file name */
        final String[] parts = new Path( m_tFile.getText() ).toFile().getName().split( "\\." );
        if( parts.length == 2 && m_creationPage != null )
        {
          m_creationPage.setFileName( parts[0] + ".gml" );
        }

      }
    } );
    m_tFile.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    m_buttonFile = new Button( container, SWT.NONE );
    m_buttonFile.setText( "..." );
    m_buttonFile.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        browseForFile();
      }
    } );

    /* Coordinate system combo */
    final Composite crsContainer = new Composite( container, SWT.NULL );
    final GridLayout crsGridLayout = new GridLayout();
    crsGridLayout.numColumns = 1;
    crsContainer.setLayout( crsGridLayout );

    final GridData crsGridData = new GridData( SWT.FILL, SWT.FILL, true, true );
    crsGridData.horizontalSpan = 3;
    crsContainer.setLayoutData( crsGridData );

    m_crsPanel = new CRSSelectionPanel();
    final Control crsControl = m_crsPanel.createControl( crsContainer );
    crsControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    crsControl.setToolTipText( "Koordinatensystem der Raster-Datei" );

    m_crs = KalypsoCorePlugin.getDefault().getCoordinatesSystem();
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

    final Label lSpacer = new Label( container, SWT.NONE );
    lSpacer.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true, 2, 0 ) );

    m_detailsGroup = new Group( container, SWT.NONE );
    m_detailsGroup.setText( "Details" );
    m_detailsGroup.setLayout( new GridLayout( 2, false ) );
    m_detailsGroup.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false, 2, 0 ) );
    m_detailsGroup.setVisible( false );

    // VectorXx
    final Label lVectorXx = new Label( m_detailsGroup, SWT.NONE );
    lVectorXx.setText( "X scale in resulting X direction" );

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
    lVectorXy.setText( "Y scale in resulting X direction" );

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
    lVectorYx.setText( "X scale in resulting Y direction" );

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
    lVectorYy.setText( "Y scale in resulting Y direction" );

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
    lUlcX.setText( "Upper left corner (X)" );

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
    lUlcY.setText( "Upper left corner (Y)" );

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
      setErrorMessage( "Please select an existing file" );

      return;
    }

    verifier = new GridFileVerifier();
    try
    {
      if( !verifier.verify( toFile.toURL() ) )
      {
        setMessage( null );
        setErrorMessage( "Please select a valid geodata file " );

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
      setErrorMessage( "No projection defined. Choose one projection from the list, please." );

      return;
    }

    if( m_crs != null )
    {
      try
      {
        m_rasterReader = verifier.getRasterMetaReader( docLocation.toFile().toURL(), m_crs );
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

    String text = "";

    if( containsIllegalChars( tVectorXx.getText() ) )
    {
      text += "\"X scale in resulting X direction\" contains illegal characters\n";
    }
    if( containsIllegalChars( tVectorXy.getText() ) )
    {
      text += "\"Y scale in resulting X direction\" contains illegal characters\n";
    }
    if( containsIllegalChars( tVectorYx.getText() ) )
    {
      text += "\"X scale in resulting Y direction\" contains illegal characters\n";
    }
    if( containsIllegalChars( tVectorYy.getText() ) )
    {
      text += "\"Y scale in resulting Y direction\" contains illegal characters\n";
    }
    if( containsIllegalChars( tUlcX.getText() ) )
    {
      text += "Upper left corner (X) contains illegal characters\n";
    }
    if( containsIllegalChars( tUlcY.getText() ) )
    {
      text += "Upper left corner (Y) contains illegal characters\n";
    }

    if( !"".equals( text ) )
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
    final Pattern p = Pattern.compile( "[+-]?[\\d]+[\\.]?[\\d]*?" );
    final Matcher m = p.matcher( text.toLowerCase() );
    return !m.matches();
  }

  protected void updateTextBoxes( final IGridMetaReader reader )
  {
    if( reader == null )
    {
      return;
    }

    tVectorXx.setText( reader.getVectorXx() );
    tVectorYy.setText( reader.getVectorYy() );
    tVectorXy.setText( reader.getVectorXy() );
    tVectorYx.setText( reader.getVectorYx() );
    tUlcX.setText( reader.getOriginCornerX() );
    tUlcY.setText( reader.getOriginCornerY() );
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

    dialog.setFilterExtensions( new String[] { "*.jpg;*.gif;*.tif;*.asc;*.asg;*.dat", "*.tif", "*.jpg", "*.gif", "*.asc;*.dat;*.asg" } );
    dialog.setFilterNames( new String[] { "All supported files", "TIFF image (*.tif)", "JPEG image (*.jpg)", "GIF image (*.gif)", "ASCII grid (*.asc, *.dat, *.asg)" } );

    final String result = dialog.open();
    if( result == null )
    {
      return null;
    }

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
// final Object element = m_comboSel.getFirstElement();
// if( element instanceof String )
// return (String) element;

// return null;
    return m_crs;
  }

  public RectifiedGridDomain.OffsetVector getOffsetX( )
  {
    return new RectifiedGridDomain.OffsetVector( Double.valueOf( tVectorXx.getText().replaceAll( ",", "." ) ), Double.valueOf( tVectorXy.getText().replaceAll( ",", "." ) ) );
  }

  public RectifiedGridDomain.OffsetVector getOffsetY( )
  {
    return new RectifiedGridDomain.OffsetVector( Double.valueOf( tVectorYx.getText().replaceAll( ",", "." ) ), Double.valueOf( tVectorYy.getText().replaceAll( ",", "." ) ) );
  }

  public Double[] getUpperLeftCorner( )
  {
    return new Double[] { Double.valueOf( tUlcX.getText().replaceAll( ",", "." ) ), Double.valueOf( tUlcY.getText().replaceAll( ",", "." ) ) };
  }

  public IGridMetaReader getReader( )
  {
    return m_rasterReader;
  }
}