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
package org.kalypso.ui.wizard.others;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.ogc.gml.ThemeUtilities;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.util.themes.image.ImageUtilities;
import org.kalypso.util.themes.image.controls.ImageComposite;
import org.kalypso.util.themes.image.listener.IImageChangedListener;
import org.kalypso.util.themes.position.PositionUtilities;

/**
 * A wizard page for entering properties of for an image theme.
 *
 * @author Holger Albert
 */
public class ImageThemeWizardPage extends WizardPage
{
  /**
   * The selected properties.
   */
  protected Map<String, String> m_properties;

  /**
   * The constructor.
   *
   * @param pageName
   *          The name of the page.
   */
  public ImageThemeWizardPage( final String pageName )
  {
    super( pageName );

    m_properties = new HashMap<>();

    setTitle( Messages.getString("ImageThemeWizardPage_0") ); //$NON-NLS-1$
    setDescription( Messages.getString("ImageThemeWizardPage_1") ); //$NON-NLS-1$
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
  public ImageThemeWizardPage( final String pageName, final String title, final ImageDescriptor titleImage )
  {
    super( pageName, title, titleImage );

    m_properties = new HashMap<>();

    setDescription( Messages.getString("ImageThemeWizardPage_2") ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    /* Create the main composite. */
    final Composite main = new Composite( parent, SWT.NONE );
    main.setLayout( new GridLayout( 1, false ) );

    /* Create the image composite. */
    final ImageComposite imageComposite = new ImageComposite( main, SWT.NONE, null );
    imageComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    imageComposite.addImageChangedListener( new IImageChangedListener()
    {
      @Override
      public void imagePropertyChanged( final Properties properties, final int horizontal, final int vertical, final RGB background, final String imageUrl )
      {
        /* Store the properties. */
        m_properties.clear();

        /* Get the properties. */
        final String horizontalProperty = properties.getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION );
        final String verticalProperty = properties.getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION );
        final String backgroundColorProperty = properties.getProperty( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR );
        final String imageUrlProperty = properties.getProperty( ImageUtilities.THEME_PROPERTY_IMAGE_URL );

        /* Set the properties. */
        m_properties.put( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
        m_properties.put( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
        m_properties.put( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, backgroundColorProperty );
        m_properties.put( ImageUtilities.THEME_PROPERTY_IMAGE_URL, imageUrlProperty );

        /* Check if the page can be completed. */
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

    /* Get the URL of the image. */
    final String imageUrl = m_properties.get( ImageUtilities.THEME_PROPERTY_IMAGE_URL );
    if( imageUrl == null || imageUrl.length() == 0 )
    {
      setErrorMessage( Messages.getString("ImageThemeWizardPage_3") ); //$NON-NLS-1$
      setPageComplete( false );
      return;
    }
  }

  /**
   * This function returns the selected properties.
   *
   * @return The selected properties.
   */
  public Map<String, String> getProperties( )
  {
    return m_properties;
  }
}