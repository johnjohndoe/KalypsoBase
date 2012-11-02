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
package org.kalypso.ui.controls.images;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.i18n.Messages;
import org.kalypso.ui.controls.images.listener.IImagePropertyChangedListener;

/**
 * This composite provides controls for editing image properties like width, height and format.
 *
 * @author Holger Albert
 */
public class ImagePropertiesComposite extends Composite
{
  /**
   * This listeners will be notified, if one property in this control has changed.
   */
  private List<IImagePropertyChangedListener> m_listener;

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
   * The button, which allows the selection of keeping the aspect ratio.
   */
  private Button m_aspectRatioButton;

  /**
   * True, if the aspect ratio should be maintained on change of the width or height.
   */
  protected boolean m_aspectRatio;

  /**
   * This variable holds the factor which is used to maintain the aspect ratio.
   */
  protected double m_aspectFactor;

  /**
   * The spinner for selecting the insets of the image.
   */
  private Spinner m_insetsSpinner;

  /**
   * The insets of the image.
   */
  protected Insets m_insets;

  /**
   * The button, which allows the enablement of a drawn border.
   */
  private Button m_borderButton;

  /**
   * True, if the drawn border is enabled.
   */
  protected boolean m_border;

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
   * @param parent
   *          A widget which will be the parent of the new instance (cannot be null).
   * @param style
   *          The style of widget to construct.
   * @param defaultWidth
   *          The default width.
   * @param defaultHeight
   *          The default height.
   * @param defaultAspectRatio
   *          The default aspect ratio.
   * @param defaultInsets
   *          The default insets.
   * @param defaultBorder
   *          The default border status.
   * @param defaultFormat
   *          The default image format.
   */
  public ImagePropertiesComposite( final Composite parent, final int style, final int defaultWidth, final int defaultHeight, final boolean defaultAspectRatio, final Insets defaultInsets, final boolean defaultBorder, final String defaultFormat )
  {
    super( parent, style );

    /* Initialize the members. */
    m_listener = new ArrayList<>();
    m_imageWidthText = null;
    m_imageWidth = defaultWidth;
    m_imageHeightText = null;
    m_imageHeight = defaultHeight;
    m_aspectRatioButton = null;
    m_aspectRatio = defaultAspectRatio;
    m_aspectFactor = 0.0;
    m_insetsSpinner = null;
    m_insets = defaultInsets;
    m_borderButton = null;
    m_border = defaultBorder;
    m_imageFormatViewer = null;
    m_imageFormat = defaultFormat;

    /* Create the controls. */
    createControls();
  }

  @Override
  public void setLayout( final Layout layout )
  {
    /* Ignore user set layouts, only layout datas are permitted. */
  }

  @Override
  public void dispose( )
  {
    if( m_listener != null )
      m_listener.clear();

    m_listener = null;
    m_imageWidthText = null;
    m_imageWidth = -1;
    m_imageHeightText = null;
    m_imageHeight = -1;
    m_aspectRatioButton = null;
    m_aspectRatio = false;
    m_aspectFactor = 0.0;
    m_insetsSpinner = null;
    m_insets = null;
    m_borderButton = null;
    m_border = false;
    m_imageFormatViewer = null;
    m_imageFormat = null;

    super.dispose();
  }

  /**
   * This function creates the controls.
   */
  private void createControls( )
  {
    /* Create the layout. */
    super.setLayout( GridLayoutFactory.fillDefaults().create() );

    /* The main composite. */
    final Composite main = new Composite( this, SWT.NONE );
    GridLayoutFactory.fillDefaults().applyTo( main );
    main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create a group. */
    final Group imageGroup = new Group( main, SWT.NONE );
    imageGroup.setLayout( new GridLayout( 2, false ) );
    imageGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    imageGroup.setText( Messages.getString( "ImagePropertiesComposite_0" ) ); //$NON-NLS-1$

    /* Create a label. */
    final Label imageWidthLabel = new Label( imageGroup, SWT.NONE );
    imageWidthLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageWidthLabel.setText( Messages.getString( "ImagePropertiesComposite_1" ) ); //$NON-NLS-1$

    /* Create a text field. */
    m_imageWidthText = new Text( imageGroup, SWT.BORDER | SWT.RIGHT );
    m_imageWidthText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    if( m_imageWidth >= 0 )
      m_imageWidthText.setText( String.format( "%d", m_imageWidth ) ); //$NON-NLS-1$
    m_imageWidthText.addModifyListener( new ModifyListener()
    {
      /**
       * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
       */
      @Override
      public void modifyText( final ModifyEvent e )
      {
        /* Get the source. */
        final Text source = (Text)e.getSource();

        /* Store the text. */
        final Integer imageWidth = NumberUtils.parseQuietInteger( source.getText() );
        if( imageWidth != null )
        {
          m_imageWidth = imageWidth.intValue();

          if( m_aspectRatio )
          {
            /* Do not change the width, which was already set. Destroy the loop. */
            m_aspectRatio = false;

            /* Calculate and set the adjusted height. */
            final int adjustedHeight = (int)Math.floor( m_imageWidth / m_aspectFactor );
            m_imageHeightText.setText( String.format( "%d", adjustedHeight ) ); //$NON-NLS-1$

            /* Reset the aspect ratio. */
            m_aspectRatio = true;
          }
        }

        /* Fire an image property changed event. */
        fireImagePropertyChanged( m_imageWidth, m_imageHeight, m_aspectRatio, m_insets, m_border, m_imageFormat );
      }
    } );

    /* Create a label. */
    final Label imageHeightLabel = new Label( imageGroup, SWT.NONE );
    imageHeightLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageHeightLabel.setText( Messages.getString( "ImagePropertiesComposite_4" ) ); //$NON-NLS-1$

    /* Create a text field. */
    m_imageHeightText = new Text( imageGroup, SWT.BORDER | SWT.RIGHT );
    m_imageHeightText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    if( m_imageHeight >= 0 )
      m_imageHeightText.setText( String.format( "%d", m_imageHeight ) ); //$NON-NLS-1$
    m_imageHeightText.addModifyListener( new ModifyListener()
    {
      /**
       * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
       */
      @Override
      public void modifyText( final ModifyEvent e )
      {
        /* Get the source. */
        final Text source = (Text)e.getSource();

        /* Store the text. */
        final Integer imageHeight = NumberUtils.parseQuietInteger( source.getText() );
        if( imageHeight != null )
        {
          m_imageHeight = imageHeight.intValue();

          if( m_aspectRatio )
          {
            /* Do not change the height, which was already set. Destroy the loop. */
            m_aspectRatio = false;

            /* Calculate and set the adjusted height. */
            final int adjustedWidth = (int)Math.floor( m_imageHeight * m_aspectFactor );
            m_imageWidthText.setText( String.format( "%d", adjustedWidth ) ); //$NON-NLS-1$

            /* Reset the aspect ratio. */
            m_aspectRatio = true;
          }
        }

        /* Fire an image property changed event. */
        fireImagePropertyChanged( m_imageWidth, m_imageHeight, m_aspectRatio, m_insets, m_border, m_imageFormat );
      }
    } );

    /* Create a button. */
    m_aspectRatioButton = new Button( imageGroup, SWT.CHECK );
    m_aspectRatioButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 2, 1 ) );
    m_aspectRatioButton.setText( Messages.getString( "ImagePropertiesComposite_7" ) ); //$NON-NLS-1$
    m_aspectRatioButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Button source = (Button)e.getSource();

        final boolean aspectRatio = source.getSelection();
        if( aspectRatio && m_imageHeight > 0 )
          m_aspectFactor = (double)m_imageWidth / (double)m_imageHeight;

        m_aspectRatio = aspectRatio;
      }
    } );

    /* Set the initial selection. */
    m_aspectRatioButton.setSelection( m_aspectRatio );

    /* Separator. */
    final Label separator = new Label( imageGroup, SWT.HORIZONTAL );
    separator.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    separator.setText( "" ); //$NON-NLS-1$

    /* Create a label. */
    final Label insetsLabel = new Label( imageGroup, SWT.NONE );
    insetsLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    insetsLabel.setText( Messages.getString( "ImagePropertiesComposite_9" ) ); //$NON-NLS-1$
    insetsLabel.setAlignment( SWT.LEFT );

    /* Create a spinner. */
    m_insetsSpinner = new Spinner( imageGroup, SWT.BORDER );
    int insets = 0;
    if( m_insets != null )
      insets = m_insets.left;
    m_insetsSpinner.setValues( insets, 0, 25, 0, 1, 5 );
    m_insetsSpinner.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_insetsSpinner.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        /* Get the source. */
        final Spinner source = (Spinner)e.getSource();

        /* Get the selection. */
        final int selection = source.getSelection();

        /* Store the values. */
        if( selection > 0 )
          m_insets = new Insets( selection, selection, selection, selection );
        else
          m_insets = null;

        /* Fire an image property changed event. */
        fireImagePropertyChanged( m_imageWidth, m_imageHeight, m_aspectRatio, m_insets, m_border, m_imageFormat );
      }
    } );

    /* Create a button. */
    m_borderButton = new Button( imageGroup, SWT.CHECK );
    m_borderButton.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false, 2, 1 ) );
    m_borderButton.setText( Messages.getString( "ImagePropertiesComposite_10" ) ); //$NON-NLS-1$
    m_borderButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Button source = (Button)e.getSource();
        final boolean border = source.getSelection();
        m_border = border;

        /* Fire an image property changed event. */
        fireImagePropertyChanged( m_imageWidth, m_imageHeight, m_aspectRatio, m_insets, m_border, m_imageFormat );
      }
    } );

    /* Set the initial selection. */
    m_borderButton.setSelection( m_border );

    /* Separator. */
    final Label separator1 = new Label( imageGroup, SWT.HORIZONTAL );
    separator1.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    separator1.setText( "" ); //$NON-NLS-1$

    /* Create a label. */
    final Label imageFormatLabel = new Label( imageGroup, SWT.NONE );
    imageFormatLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    imageFormatLabel.setText( Messages.getString( "ImagePropertiesComposite_12" ) ); //$NON-NLS-1$

    /* Create a combo viewer. */
    m_imageFormatViewer = new ComboViewer( imageGroup, SWT.READ_ONLY );
    m_imageFormatViewer.getCombo().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_imageFormatViewer.setContentProvider( new ArrayContentProvider() );
    m_imageFormatViewer.setLabelProvider( new LabelProvider() );
    m_imageFormatViewer.setInput( new String[] { "GIF", "PNG" } ); //$NON-NLS-1$ //$NON-NLS-2$
    m_imageFormatViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      /**
       * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
       */
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        /* Get the source. */
        final ComboViewer source = (ComboViewer)event.getSource();

        /* Get the selection index. */
        final int imageFormatIndex = source.getCombo().getSelectionIndex();
        if( imageFormatIndex == -1 )
          return;

        /* Get the selection. */
        final ISelection selection = source.getSelection();
        if( selection == null || selection.isEmpty() || !(selection instanceof StructuredSelection) )
          return;

        /* Cast. */
        final StructuredSelection structuredSelection = (StructuredSelection)selection;

        /* Get the first element. */
        final Object firstElement = structuredSelection.getFirstElement();
        if( firstElement == null || !(firstElement instanceof String) )
          return;

        /* Cast. */
        final String imageFormat = (String)firstElement;

        /* Store the values. */
        m_imageFormat = imageFormat;

        /* Fire an image property changed event. */
        fireImagePropertyChanged( m_imageWidth, m_imageHeight, m_aspectRatio, m_insets, m_border, m_imageFormat );
      }
    } );

    /* Set the initial selection. */
    if( m_imageFormat != null && m_imageFormat.length() > 0 )
      m_imageFormatViewer.setSelection( new StructuredSelection( m_imageFormat ) );
    else
      m_imageFormatViewer.setSelection( new StructuredSelection( "PNG" ) ); //$NON-NLS-1$
  }

  /**
   * This function fires the image property changed event.
   *
   * @param width
   *          The width.
   * @param height
   *          The height.
   * @param aspectRatio
   *          The aspect ratio.
   * @param insets
   *          The insets.
   * @param border
   *          The border status.
   * @param format
   *          The image format.
   */
  protected void fireImagePropertyChanged( final int imageWidth, final int imageHeight, final boolean aspectRatio, final Insets insets, final boolean border, final String imageFormat )
  {
    for( final IImagePropertyChangedListener listener : m_listener )
      listener.imagePropertyChanged( imageWidth, imageHeight, aspectRatio, insets, border, imageFormat );
  }

  /**
   * This function adds a image property changed listener.
   *
   * @param listener
   *          The image property changed listener to add.
   */
  public void addImagePropertyChangedListener( final IImagePropertyChangedListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  /**
   * This function removes a image property changed listener.
   *
   * @param listener
   *          The image property changed listener to remove.
   */
  public void removeImagePropertyChangedListener( final IImagePropertyChangedListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  /**
   * This function sets the width of the image. The aspect ratio flag is ignored.
   *
   * @param imageWidth
   *          The width of the image or -1.
   */
  public void setImageWidth( final int imageWidth )
  {
    if( m_imageWidthText == null || m_imageWidthText.isDisposed() )
      return;

    /* We do not want to change the height as well, if the width is explicitly set. */
    final boolean tmpAspectRatio = m_aspectRatio;
    m_aspectRatio = false;

    /* The width of the image. */
    if( imageWidth > 0 )
      m_imageWidthText.setText( String.format( "%d", imageWidth ) ); //$NON-NLS-1$
    else
      m_imageWidthText.setText( "640" ); //$NON-NLS-1$

    /* Reset the aspect ratio. */
    m_aspectRatio = tmpAspectRatio;
  }

  /**
   * This function sets the height of the image. The aspect ratio flag is ignored.
   *
   * @param imageHeight
   *          The height of the image or -1.
   */
  public void setImageHeight( final int imageHeight )
  {
    if( m_imageHeightText == null || m_imageHeightText.isDisposed() )
      return;

    /* We do not want to change the width as well, if the height is explicitly set. */
    final boolean tmpAspectRatio = m_aspectRatio;
    m_aspectRatio = false;

    /* The height of the image. */
    if( imageHeight > 0 )
      m_imageHeightText.setText( String.format( "%d", imageHeight ) ); //$NON-NLS-1$
    else
      m_imageHeightText.setText( "480" ); //$NON-NLS-1$

    /* Reset the aspect ratio. */
    m_aspectRatio = tmpAspectRatio;
  }

  /**
   * This function sets the aspect ratio.
   *
   * @param aspectRatio
   *          True, if the aspect ratio should be maintained on change of the width or height.
   */
  public void setAspectRatio( final boolean aspectRatio )
  {
    if( m_aspectRatioButton == null || m_aspectRatioButton.isDisposed() )
      return;

    /* Set the selection. */
    m_aspectRatioButton.setSelection( aspectRatio );

    /* Create an event. */
    final Event newEvent = new Event();
    newEvent.display = m_aspectRatioButton.getDisplay();
    newEvent.doit = true;
    newEvent.widget = m_aspectRatioButton;

    /* Notify the listeners. */
    m_aspectRatioButton.notifyListeners( SWT.Selection, newEvent );
  }

  /**
   * This function sets the insets of the image.
   *
   * @param insets
   *          The insets of the image or null.
   */
  public void setInsets( final Insets insets )
  {
    if( m_insetsSpinner == null || m_insetsSpinner.isDisposed() )
      return;

    if( insets.left >= 0 && insets.left <= 25 )
    {
      /* Set the selection. */
      m_insetsSpinner.setSelection( insets.left );

      /* Create an event. */
      final Event newEvent = new Event();
      newEvent.display = m_insetsSpinner.getDisplay();
      newEvent.doit = true;
      newEvent.widget = m_insetsSpinner;

      /* Notify the listeners. */
      m_insetsSpinner.notifyListeners( SWT.Selection, newEvent );
    }
  }

  /**
   * This function sets border status.
   *
   * @param aspectRatio
   *          True, if the drawn border is enabled.
   */
  public void setBorder( final boolean border )
  {
    if( m_borderButton == null || m_borderButton.isDisposed() )
      return;

    /* Set the selection. */
    m_borderButton.setSelection( border );

    /* Create an event. */
    final Event newEvent = new Event();
    newEvent.display = m_borderButton.getDisplay();
    newEvent.doit = true;
    newEvent.widget = m_borderButton;

    /* Notify the listeners. */
    m_borderButton.notifyListeners( SWT.Selection, newEvent );
  }

  /**
   * This function sets the format of the image.
   *
   * @param imageFormat
   *          The format of the image or null.
   */
  public void setImageFormat( final String imageFormat )
  {
    if( m_imageFormatViewer == null || m_imageFormatViewer.getCombo().isDisposed() )
      return;

    if( imageFormat != null && imageFormat.length() > 0 )
      m_imageFormatViewer.setSelection( new StructuredSelection( imageFormat ) );
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
   * This function returns true, if the aspect ratio should be maintained on change of the width or height.
   *
   * @return True, if the aspect ratio should be maintained on change of the width or height.
   */
  public boolean keepAspectRatio( )
  {
    return m_aspectRatio;
  }

  /**
   * This function returns the insets of the image.
   *
   * @return The insets of the image or null.
   */
  public Insets getInsets( )
  {
    return m_insets;
  }

  /**
   * This function returns true, if the drawn border is enabled.
   *
   * @return True, if the drawn border is enabled.
   */
  public boolean hasBorder( )
  {
    return m_border;
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