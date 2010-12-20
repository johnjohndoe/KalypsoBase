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
package org.kalypso.ogc.gml.map.handlers.dialogs;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * This dialog enables the selection of the format and a target for the image file.
 * 
 * @author Holger Albert
 */
public class ScreenshotDialog extends Dialog
{
  /**
   * Key for the dialog settings: target path.
   */
  private static final String SETTINGS_TARGET_PATH = "targetPath"; //$NON-NLS-1$

  /**
   * Key for the dialog settings: image width.
   */
  private static final String SETTINGS_IMAGE_WIDTH = "imageWidth"; //$NON-NLS-1$

  /**
   * Key for the dialog settings: image height.
   */
  private static final String SETTINGS_IMAGE_HEIGHT = "imageHeight"; //$NON-NLS-1$

  /**
   * Key for the dialog settings: image format.
   */
  private static final String SETTINGS_IMAGE_FORMAT = "imageFormat"; //$NON-NLS-1$

  /**
   * The dialog settings.
   */
  private IDialogSettings m_dialogSettings;

  /**
   * The text field, which contains the path of the target.
   */
  protected Text m_targetPathText;

  /**
   * The path of the target.
   */
  protected String m_targetPath;

  /**
   * The text field, which contains the width of the image.
   */
  protected Text m_imageWidthText;

  /**
   * The width of the image.
   */
  protected int m_imageWidth;

  /**
   * The text field, which contains the height of the image.
   */
  protected Text m_imageHeightText;

  /**
   * The height of the image.
   */
  protected int m_imageHeight;

  /**
   * The combo viewer, which contains the format of the image.
   */
  private ComboViewer m_imageFormatViewer;

  /**
   * The format of the image.
   */
  protected String m_imageFormat;

  /**
   * The constructor.
   * 
   * @param shell
   *          The parent shell, or null to create a top-level shell.
   */
  public ScreenshotDialog( Shell shell )
  {
    super( shell );

    m_dialogSettings = null;
    m_targetPathText = null;
    m_targetPath = null;
    m_imageWidthText = null;
    m_imageWidth = -1;
    m_imageHeightText = null;
    m_imageHeight = -1;
    m_imageFormatViewer = null;
    m_imageFormat = null;
  }

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The object that returns the current parent shell.
   */
  public ScreenshotDialog( IShellProvider parentShell )
  {
    super( parentShell );

    m_dialogSettings = null;
    m_targetPathText = null;
    m_targetPath = null;
    m_imageWidthText = null;
    m_imageWidth = -1;
    m_imageHeightText = null;
    m_imageHeight = -1;
    m_imageFormatViewer = null;
    m_imageFormat = null;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( Composite parent )
  {
    /* Initialize the dialog settings. */
    initializeDialogSettings();

    /* Set the title. */
    getShell().setText( "Bildexport" );

    /* Create the main composite. */
    Composite main = (Composite) super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create a group. */
    Group targetGroup = new Group( main, SWT.NONE );
    targetGroup.setLayout( new GridLayout( 2, false ) );
    targetGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    targetGroup.setText( "Ziel (ohne Endung)" );

    /* Create a text field. */
    m_targetPathText = new Text( targetGroup, SWT.BORDER );
    GridData targetData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    targetData.widthHint = 350;
    m_targetPathText.setLayoutData( targetData );
    m_targetPathText.addModifyListener( new ModifyListener()
    {
      /**
       * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
       */
      @Override
      public void modifyText( ModifyEvent e )
      {
        /* Get the source. */
        Text source = (Text) e.getSource();

        /* Store the text. */
        m_targetPath = source.getText();

        /* Check, if all data entered is correct. */
        checkDialogComplete();
      }
    } );

    /* Create a button. */
    Button targetButton = new Button( targetGroup, SWT.NONE );
    targetButton.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, false ) );
    targetButton.setText( "..." ); //$NON-NLS-1$
    targetButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        /* Get the source. */
        Button source = (Button) e.getSource();

        /* Create the dialog. */
        FileDialog dialog = new FileDialog( source.getParent().getShell(), SWT.OPEN );
        dialog.setText( "Bildexport" );
        File f = new File( m_targetPathText.getText() );
        dialog.setFilterPath( f.getPath() );
        dialog.setFileName( "" ); //$NON-NLS-1$

        /* Get the selection of the user. */
        String targetPath = dialog.open();
        if( targetPath == null || targetPath.length() == 0 )
          return;

        /* Remove the file extension. */
        targetPath = FilenameUtils.removeExtension( targetPath );

        /* Adjust the text field. */
        m_targetPathText.setText( targetPath );
      }
    } );

    /* Create a group. */
    Group imageGroup = new Group( main, SWT.NONE );
    imageGroup.setLayout( new GridLayout( 2, false ) );
    imageGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    imageGroup.setText( "Maße, Format" );

    /* Create a label. */
    Label imageWidthLabel = new Label( imageGroup, SWT.NONE );
    imageWidthLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageWidthLabel.setText( "Breite" );

    /* Create a text field. */
    m_imageWidthText = new Text( imageGroup, SWT.BORDER | SWT.RIGHT );
    m_imageWidthText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_imageWidthText.addModifyListener( new ModifyListener()
    {
      /**
       * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
       */
      @Override
      public void modifyText( ModifyEvent e )
      {
        /* Get the source. */
        Text source = (Text) e.getSource();

        /* Store the text. */
        Integer imageWidth = NumberUtils.parseQuietInteger( source.getText() );
        if( imageWidth != null )
          m_imageWidth = imageWidth.intValue();

        /* Check, if all data entered is correct. */
        checkDialogComplete();
      }
    } );

    /* Create a label. */
    Label imageHeightLabel = new Label( imageGroup, SWT.NONE );
    imageHeightLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageHeightLabel.setText( "Höhe" );

    /* Create a text field. */
    m_imageHeightText = new Text( imageGroup, SWT.BORDER | SWT.RIGHT );
    m_imageHeightText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_imageHeightText.addModifyListener( new ModifyListener()
    {
      /**
       * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
       */
      @Override
      public void modifyText( ModifyEvent e )
      {
        /* Get the source. */
        Text source = (Text) e.getSource();

        /* Store the text. */
        Integer imageHeight = NumberUtils.parseQuietInteger( source.getText() );
        if( imageHeight != null )
          m_imageHeight = imageHeight.intValue();

        /* Check, if all data entered is correct. */
        checkDialogComplete();
      }
    } );

    /* Create a label. */
    Label imageFormatLabel = new Label( imageGroup, SWT.NONE );
    imageFormatLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageFormatLabel.setText( "Format" );

    /* Create a combo viewer. */
    m_imageFormatViewer = new ComboViewer( imageGroup, SWT.READ_ONLY );
    m_imageFormatViewer.getCombo().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_imageFormatViewer.setContentProvider( new ArrayContentProvider() );
    m_imageFormatViewer.setLabelProvider( new LabelProvider() );
    m_imageFormatViewer.setInput( new String[] { "GIF", "PNG" } );
    m_imageFormatViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      /**
       * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
       */
      @Override
      public void selectionChanged( SelectionChangedEvent event )
      {
        /* Get the source. */
        ComboViewer source = (ComboViewer) event.getSource();

        /* Get the selection index. */
        int imageFormatIndex = source.getCombo().getSelectionIndex();
        if( imageFormatIndex == -1 )
          return;

        /* Get the selection. */
        ISelection selection = source.getSelection();
        if( selection == null || selection.isEmpty() || !(selection instanceof StructuredSelection) )
          return;

        /* Cast. */
        StructuredSelection structuredSelection = (StructuredSelection) selection;

        /* Get the first element. */
        Object firstElement = structuredSelection.getFirstElement();
        if( firstElement == null || !(firstElement instanceof String) )
          return;

        /* Cast. */
        String imageFormat = (String) firstElement;

        /* Store the values. */
        m_imageFormat = imageFormat;

        /* Check, if all data entered is correct. */
        checkDialogComplete();
      }
    } );

    /* Apply default values (if dialog settings are available). */
    applyDialogSettings();

    return main;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar( Composite parent )
  {
    super.createButtonsForButtonBar( parent );

    /* Check, if the dialog is allowed to be completed. */
    checkDialogComplete();
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed( )
  {
    /* Here a selection should be available. */
    /* The case that nothing is selected is taken care of in the checkDialogComplete() function. */

    /* Add the extension, if neccessary. */
    String pathname = m_targetPath;
    if( !m_targetPath.toLowerCase().endsWith( "." + m_imageFormat.toLowerCase() ) )
      pathname = m_targetPath + "." + m_imageFormat.toLowerCase();

    /* If the target exists already, give a warning and do only continue, if the user has confirmed it. */
    File targetFile = new File( pathname );
    if( targetFile.exists() )
    {
      /* Ask the user. */
      boolean confirmed = MessageDialog.openConfirm( getShell(), "Bildexport", String.format( "Die Datei '%s' existiert bereits, möchten Sie sie überschreiben?", targetFile.getAbsolutePath() ) );
      if( !confirmed )
        return;
    }

    /* Save the dialog settings. */
    saveDialogSettings();

    super.okPressed();
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
   */
  @Override
  protected void cancelPressed( )
  {
    /* Clear possible made selections. */
    m_targetPath = null;
    m_imageWidth = -1;
    m_imageHeight = -1;
    m_imageFormat = null;

    super.cancelPressed();
  }

  /**
   * This function initializes the dialog settings.
   */
  private void initializeDialogSettings( )
  {
    /* The dialog settings for Kalypso UI. */
    IDialogSettings dialogSettings = KalypsoGisPlugin.getDefault().getDialogSettings();

    /* This name will be used for the section of this dialog. */
    String sectionName = getClass().getCanonicalName();

    /* Check if a section for this dialog does exist. */
    /* If not, create one. */
    IDialogSettings section = dialogSettings.getSection( sectionName );
    if( section == null )
      section = dialogSettings.addNewSection( getClass().getCanonicalName() );

    /* Get the dialog settings. */
    m_dialogSettings = section;
  }

  /**
   * This function applies the values in the dialog settings as default values to the controls.
   */
  private void applyDialogSettings( )
  {
    /* No dialog settings available, for initializing the controls. */
    if( m_dialogSettings == null )
      return;

    /* Read from the dialog settings and apply to the controls. */

    /* The path of the target. */
    String targetPath = m_dialogSettings.get( SETTINGS_TARGET_PATH );
    if( targetPath != null && targetPath.length() > 0 )
      m_targetPathText.setText( targetPath );

    /* The width of the image. */
    String imageWidth = m_dialogSettings.get( SETTINGS_IMAGE_WIDTH );
    if( imageWidth != null && imageWidth.length() > 0 )
      m_imageWidthText.setText( imageWidth );
    else
      m_imageWidthText.setText( "640" );

    /* The height of the image. */
    String imageHeight = m_dialogSettings.get( SETTINGS_IMAGE_HEIGHT );
    if( imageHeight != null && imageHeight.length() > 0 )
      m_imageHeightText.setText( imageHeight );
    else
      m_imageHeightText.setText( "480" );

    /* The format of the image. */
    String imageFormat = m_dialogSettings.get( SETTINGS_IMAGE_FORMAT );
    if( imageFormat != null && imageFormat.length() > 0 )
      m_imageFormatViewer.setSelection( new StructuredSelection( imageFormat ) );
    else
      m_imageFormatViewer.setSelection( new StructuredSelection( m_imageFormatViewer.getElementAt( 0 ) ) );
  }

  /**
   * This function saves the values from the controls into the dialog settings.
   */
  private void saveDialogSettings( )
  {
    /* No dialog settings to write to. */
    if( m_dialogSettings == null )
      return;

    /* Read from the controls and apply to the dialog settings. */
    m_dialogSettings.put( SETTINGS_TARGET_PATH, m_targetPath );
    m_dialogSettings.put( SETTINGS_IMAGE_WIDTH, m_imageWidth );
    m_dialogSettings.put( SETTINGS_IMAGE_HEIGHT, m_imageHeight );
    m_dialogSettings.put( SETTINGS_IMAGE_FORMAT, m_imageFormat );
  }

  /**
   * This function checks, if the dialog is allowed to be completed.
   */
  protected void checkDialogComplete( )
  {
    /* Get the OK button. */
    Button okButton = getButton( IDialogConstants.OK_ID );
    if( okButton == null )
      return;

    /* First of all, it should be allowed to complete. */
    okButton.setEnabled( true );

    if( m_targetPath == null || m_targetPath.length() == 0 )
    {
      okButton.setEnabled( false );
      return;
    }

    if( m_imageWidth <= 0 )
    {
      okButton.setEnabled( false );
      return;
    }

    if( m_imageHeight <= 0 )
    {
      okButton.setEnabled( false );
      return;
    }

    if( m_imageFormat == null || m_imageFormat.length() == 0 )
    {
      okButton.setEnabled( false );
      return;
    }
  }

  /**
   * This function returns the path of the target.
   * 
   * @return The path of the target or null.
   */
  public String getTargetPath( )
  {
    /* Add the extension, if neccessary. */
    if( !m_targetPath.toLowerCase().endsWith( "." + m_imageFormat.toLowerCase() ) )
      return m_targetPath + "." + m_imageFormat.toLowerCase();

    return m_targetPath;
  }

  /**
   * This function returns the width of the image.
   * 
   * @return The width of the image or -1.
   */
  public int getImageWidth( )
  {
    return m_imageWidth;
  }

  /**
   * This function returns the height of the image.
   * 
   * @return The height of the image or -1.
   */

  public int getImageHeight( )
  {
    return m_imageHeight;
  }

  /**
   * This function returns the format of the image.
   * 
   * @return The format of the image or null.
   */
  public String getImageFormat( )
  {
    return m_imageFormat;
  }
}