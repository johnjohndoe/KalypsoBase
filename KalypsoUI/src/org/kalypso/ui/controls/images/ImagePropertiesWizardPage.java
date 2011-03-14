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
package org.kalypso.ui.controls.images;

import java.awt.Insets;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.ui.controls.images.listener.IImagePropertyChangedListener;

/**
 * A page for selecting export information for an image. If a publishing configuration is given it will store the
 * properties there, too.
 * 
 * @author Holger Albert
 */
public class ImagePropertiesWizardPage extends WizardPage
{
  /**
   * Key for the configuration: image width.
   */
  public static final String CONFIG_IMAGE_WIDTH = "imageWidth";

  /**
   * Key for the configuration: image height.
   */
  public static final String CONFIG_IMAGE_HEIGHT = "imageHeight";

  /**
   * Key for the configuration: aspect ratio.
   */
  public static final String CONFIG_KEEP_ASPECT_RATIO = "aspectRatio";

  /**
   * Key for the configuration: insets.
   */
  public static final String CONFIG_INSETS = "insets";

  /**
   * Key for the configuration: border.
   */
  public static final String CONFIG_HAS_BORDER = "border";

  /**
   * Key for the configuration: image format.
   */
  public static final String CONFIG_IMAGE_FORMAT = "imageFormat";

  /**
   * The publishing configuration. May be null.
   */
  protected IPublishingConfiguration m_configuration;

  /**
   * The width of the image.
   */
  protected int m_width;

  /**
   * The height of the image.
   */
  protected int m_height;

  /**
   * True, if the aspect ratio should be maintained on change of the width or height.
   */
  protected boolean m_aspectRatio;

  /**
   * The insets of the image.
   */
  protected Insets m_insets;

  /**
   * True, if the drawn border is enabled.
   */
  protected boolean m_border;

  /**
   * The format of the image.
   */
  protected String m_format;

  /**
   * The constructor.
   * 
   * @param pageName
   *          The name of the page.
   * @param configuration
   *          The publishing configuration. May be null.
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
  public ImagePropertiesWizardPage( String pageName, IPublishingConfiguration configuration, int defaultWidth, int defaultHeight, boolean defaultAspectRatio, Insets defaultInsets, boolean defaultBorder, String defaultFormat )
  {
    this( pageName, "Bildeigenschaften", null, configuration, defaultWidth, defaultHeight, defaultAspectRatio, defaultInsets, defaultBorder, defaultFormat );
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
   * @param configuration
   *          The publishing configuration. May be null.
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
  public ImagePropertiesWizardPage( String pageName, String title, ImageDescriptor titleImage, IPublishingConfiguration configuration, int defaultWidth, int defaultHeight, boolean defaultAspectRatio, Insets defaultInsets, boolean defaultBorder, String defaultFormat )
  {
    super( pageName, title, titleImage );

    /* Initialize. */
    m_configuration = configuration;
    m_width = defaultWidth;
    m_height = defaultHeight;
    m_aspectRatio = defaultAspectRatio;
    m_insets = defaultInsets;
    m_border = defaultBorder;
    m_format = defaultFormat;

    /* Initialize the page. */
    setDescription( "W‰hlen Sie die Eigenschaften des Bildes." );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( Composite parent )
  {
    /* Create the main composite. */
    Composite main = new Composite( parent, SWT.NONE );
    main.setLayout( new FillLayout() );

    /* Create the image properties composite. */
    ImagePropertiesComposite imageComposite = new ImagePropertiesComposite( main, SWT.NONE, m_width, m_height, false, m_insets, m_border, m_format );

    /* Add a listener. */
    imageComposite.addImagePropertyChangedListener( new IImagePropertyChangedListener()
    {
      /**
       * @see org.kalypso.ui.controls.listener.IImagePropertyChangedListener#imagePropertyChanged(int, int, boolean,
       *      java.awt.Insets, boolean, java.lang.String)
       */
      @Override
      public void imagePropertyChanged( int width, int height, boolean aspectRatio, Insets insets, boolean border, String format )
      {
        /* Store the image properties. */
        m_width = width;
        m_height = height;
        m_aspectRatio = aspectRatio;
        m_insets = insets;
        m_border = border;
        m_format = format;

        /* Store also for the configuration, if given. */
        if( m_configuration != null )
        {
          m_configuration.setProperty( CONFIG_IMAGE_WIDTH, new Integer( m_width ) );
          m_configuration.setProperty( CONFIG_IMAGE_HEIGHT, new Integer( m_height ) );
          m_configuration.setProperty( CONFIG_KEEP_ASPECT_RATIO, new Boolean( m_aspectRatio ) );
          m_configuration.setProperty( CONFIG_INSETS, m_insets );
          m_configuration.setProperty( CONFIG_HAS_BORDER, new Boolean( m_border ) );
          m_configuration.setProperty( CONFIG_IMAGE_FORMAT, m_format );
        }

        /* Check, if all data entered is correct. */
        checkPageComplete();
      }
    } );

    /* Set the control to the page. */
    setControl( main );

    /* In the first time, the page cannot be completed. */
    setPageComplete( false );
  }

  /**
   * This function checks, if the page can be completed.
   */
  protected void checkPageComplete( )
  {
    /* The wizard page can be completed. */
    setMessage( null );
    setErrorMessage( null );
    setPageComplete( true );

    if( m_width <= 0 )
    {
      setErrorMessage( "Bitte geben Sie die Breite des Bildes an..." );
      setPageComplete( false );
      return;
    }

    if( m_height <= 0 )
    {
      setErrorMessage( "Bitte geben Sie die Hˆhe des Bildes an..." );
      setPageComplete( false );
      return;
    }

    if( m_format == null || m_format.length() == 0 )
    {
      setErrorMessage( "Bitte geben Sie das Format des Bildes an..." );
      setPageComplete( false );
      return;
    }
  }

  /**
   * This function returns the width of the image.
   * 
   * @return The width of the image or -1.
   */
  public int getWidth( )
  {
    return m_width;
  }

  /**
   * This function returns the height of the image.
   * 
   * @return The height of the image or -1.
   */

  public int getHeight( )
  {
    return m_height;
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
  public String getFormat( )
  {
    return m_format;
  }
}