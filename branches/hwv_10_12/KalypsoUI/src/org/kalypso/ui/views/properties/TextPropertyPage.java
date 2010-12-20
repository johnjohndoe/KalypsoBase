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
package org.kalypso.ui.views.properties;

import java.util.Properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.themes.KalypsoTextTheme;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.util.themes.position.PositionUtilities;
import org.kalypso.util.themes.text.TextUtilities;
import org.kalypso.util.themes.text.controls.TextComposite;
import org.kalypso.util.themes.text.listener.ITextChangedListener;

/**
 * @author Holger Albert
 */
public class TextPropertyPage extends PropertyPage implements IWorkbenchPropertyPage
{
  /**
   * The theme.
   */
  private IKalypsoTheme m_theme;

  /**
   * A up to date properties object, containing all serialized text properties.
   */
  protected Properties m_properties;

  /**
   * The parent composite.
   */
  private Composite m_parent;

  /**
   * The main composite.
   */
  private Composite m_main;

  /**
   * The constructor.
   */
  public TextPropertyPage( )
  {
    m_theme = null;
    m_properties = null;
    m_parent = null;
    m_main = null;
  }

  /**
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents( Composite parent )
  {
    /* The content. */
    Composite content = new Composite( parent, SWT.NONE );
    GridLayout contentLayout = new GridLayout( 1, false );
    contentLayout.marginHeight = 0;
    contentLayout.marginWidth = 0;
    content.setLayout( contentLayout );
    content.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Initialize. */
    init();

    /* Create the contents. */
    createContentsInternal( content );

    return content;
  }

  /**
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  @Override
  protected void performDefaults( )
  {
    if( m_theme == null )
    {
      super.performDefaults();
      return;
    }

    if( !(m_theme instanceof KalypsoTextTheme) )
    {
      super.performDefaults();
      return;
    }

    /* Get the default properties. */
    m_properties = TextUtilities.getDefaultProperties();

    /* Update the GUI. */
    update();

    super.performDefaults();
  }

  /**
   * @see org.eclipse.jface.preference.PreferencePage#performOk()
   */
  @Override
  public boolean performOk( )
  {
    if( m_theme == null || m_properties == null )
      return super.performOk();

    if( !(m_theme instanceof KalypsoTextTheme) )
      return super.performOk();

    /* Get the properties. */
    String horizontalProperty = m_properties.getProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION );
    String verticalProperty = m_properties.getProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION );
    String textProperty = m_properties.getProperty( TextUtilities.THEME_PROPERTY_TEXT );

    /* Set the properties. */
    if( horizontalProperty != null && horizontalProperty.length() > 0 )
      m_theme.setProperty( PositionUtilities.THEME_PROPERTY_HORIZONTAL_POSITION, horizontalProperty );
    if( verticalProperty != null && verticalProperty.length() > 0 )
      m_theme.setProperty( PositionUtilities.THEME_PROPERTY_VERTICAL_POSITION, verticalProperty );
    if( textProperty != null && textProperty.length() > 0 )
      m_theme.setProperty( TextUtilities.THEME_PROPERTY_TEXT, textProperty );

    return super.performOk();
  }

  /**
   * This function creates the contents.
   * 
   * @param parent
   *          The parent composite.
   */
  private void createContentsInternal( Composite parent )
  {
    /* Store the parent. */
    m_parent = parent;

    /* Create the main composite. */
    m_main = new Composite( parent, SWT.NONE );
    m_main.setLayout( new GridLayout( 1, false ) );
    m_main.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the text composite. */
    TextComposite textComposite = new TextComposite( m_main, SWT.NONE, m_properties );
    textComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    textComposite.addTextChangedListener( new ITextChangedListener()
    {
      /**
       * @see org.kalypso.util.themes.text.listener.ITextChangedListener#textPropertyChanged(java.util.Properties, int,
       *      int, java.lang.String)
       */
      @Override
      public void textPropertyChanged( Properties properties, int horizontal, int vertical, String text )
      {
        /* Update the properties object. */
        m_properties = properties;
      }
    } );
  }

  /**
   * This function initializes the property page.
   */
  private void init( )
  {
    /* Get the element. */
    IAdaptable element = getElement();

    /* Get the node. */
    IThemeNode node = (IThemeNode) (element instanceof IThemeNode ? element : element.getAdapter( IThemeNode.class ));

    /* Get the theme. */
    Object nodeElement = node.getElement();
    IKalypsoTheme theme = (nodeElement instanceof IKalypsoTheme) ? (IKalypsoTheme) nodeElement : null;

    /* Store the members. */
    m_theme = theme;
    m_properties = null;

    if( m_theme instanceof KalypsoTextTheme )
    {
      /* Cast. */
      KalypsoTextTheme textTheme = (KalypsoTextTheme) m_theme;

      /* Get the properties of the text theme. */
      Properties textProperties = new Properties();
      String[] propertyNames = textTheme.getPropertyNames();
      for( String propertyName : propertyNames )
        textProperties.put( propertyName, textTheme.getProperty( propertyName, null ) );

      /* Store the member. */
      m_properties = textProperties;
    }
  }

  /**
   * This function updates the composite.
   */
  protected void update( )
  {
    if( m_parent == null || m_main == null )
      return;

    /* Dispose the content of the composite. */
    if( !m_main.isDisposed() )
      m_main.dispose();

    /* Redraw the content of the composite. */
    createContentsInternal( m_parent );

    /* Do a reflow. */
    m_parent.layout( true, true );
  }
}