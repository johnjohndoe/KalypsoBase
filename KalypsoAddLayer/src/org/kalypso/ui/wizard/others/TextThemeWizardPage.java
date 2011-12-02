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
package org.kalypso.ui.wizard.others;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.util.themes.ThemeUtilities;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypso.util.themes.text.TextUtilities;
import org.kalypso.util.themes.text.controls.TextComposite;
import org.kalypso.util.themes.text.listener.ITextChangedListener;

/**
 * A wizard page for entering properties of for a text theme.
 * 
 * @author Holger Albert
 */
public class TextThemeWizardPage extends WizardPage
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
  public TextThemeWizardPage( String pageName )
  {
    super( pageName );

    m_properties = new HashMap<String, String>();

    setTitle( "Texteigenschaften" );
    setDescription( "W�hlen Sie die Eigenschaften des Textes." );
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
  public TextThemeWizardPage( String pageName, String title, ImageDescriptor titleImage )
  {
    super( pageName, title, titleImage );

    m_properties = new HashMap<String, String>();

    setDescription( "W�hlen Sie die Eigenschaften des Textes." );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( Composite parent )
  {
    /* Create the main composite. */
    Composite main = new Composite( parent, SWT.NONE );
    main.setLayout( new GridLayout( 1, false ) );

    /* Create the text composite. */
    TextComposite textComposite = new TextComposite( main, SWT.NONE, null );
    textComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    textComposite.addTextChangedListener( new ITextChangedListener()
    {
      /**
       * @see org.kalypso.util.themes.text.listener.ITextChangedListener#textPropertyChanged(java.util.Properties, int,
       *      int, org.eclipse.swt.graphics.Color, java.lang.String, int, boolean)
       */
      @Override
      public void textPropertyChanged( Properties properties, int horizontal, int vertical, org.eclipse.swt.graphics.Color backgroundColor, String text, int fontSize, boolean transparency )
      {
        /* Store the properties. */
        m_properties.clear();

        /* Get the properties. */
        String horizontalProperty = properties.getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION );
        String verticalProperty = properties.getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION );
        String backgroundColorProperty = properties.getProperty( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR );
        String textProperty = properties.getProperty( TextUtilities.THEME_PROPERTY_TEXT );
        String fontSizeProperty = properties.getProperty( TextUtilities.THEME_PROPERTY_FONT_SIZE );
        String transparencyProperty = properties.getProperty( TextUtilities.THEME_PROPERTY_TRANSPARENCY );

        /* Set the properties. */
        m_properties.put( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
        m_properties.put( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
        m_properties.put( ThemeUtilities.THEME_PROPERTY_BACKGROUND_COLOR, backgroundColorProperty );
        m_properties.put( TextUtilities.THEME_PROPERTY_TEXT, textProperty );
        m_properties.put( TextUtilities.THEME_PROPERTY_FONT_SIZE, fontSizeProperty );
        m_properties.put( TextUtilities.THEME_PROPERTY_TRANSPARENCY, transparencyProperty );

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

    /* Get the text. */
    String text = m_properties.get( TextUtilities.THEME_PROPERTY_TEXT );
    if( text == null || text.length() == 0 )
    {
      setErrorMessage( "Bitte geben Sie den anzuzeigenden Text an..." );
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