/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
/*
 * Created on 15.07.2004
 *  
 */
package org.kalypso.ui.editor.styleeditor.panels;

import javax.swing.event.EventListenerList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.style.RuleCollection;

/**
 * @author Administrator
 */
public class ColorPalettePanel
{

  private Composite composite = null;

  private final EventListenerList listenerList = new EventListenerList();

  public final static int CUSTOM_TRANSITION = -1;

  public final static int RED_GREEN_TRANSITION = 0;

  public final static int BLUE_GREEN_TRANSITION = 1;

  public final static int RED_BLUE_TRANSITION = 2;

  private ColorPaletteComboBox comboBox = null;

  private int numberOfColors = 0;

  public static final int COLOR_SIZE = 10;

  public static final int COLOR_BORDER = 2;

  public int type = RED_GREEN_TRANSITION;

  private Color[] customColor = null;

  private Color[] colorArray = null;

  private int colorPaletteSelection = 0;

  private RuleCollection ruleCollection = null;

  public ColorPalettePanel( final Composite parent, final Color[] colors, final RuleCollection m_ruleCollection )
  {
    type = RED_GREEN_TRANSITION;
    setRuleCollection( m_ruleCollection );
    composite = new Composite( parent, SWT.NULL );
    final FormLayout compositeLayout = new FormLayout();
    final GridData compositeData = new GridData();
    compositeData.widthHint = 180;
    composite.setLayoutData( compositeData );
    composite.setLayout( compositeLayout );
    compositeLayout.marginWidth = 0;
    compositeLayout.marginHeight = 0;
    compositeLayout.spacing = 0;
    composite.layout();
    if( colors == null )
      initializeColors( type, m_ruleCollection.size() );
    else
      setColorArray( colors );
    setNumberOfColors( getColorArray().length );
    init();
    customColor = getColorArray();
  }

  public void addColorPalettePanelListener( final PanelListener pl )
  {
    listenerList.add( PanelListener.class, pl );
  }

  public void draw( final Composite parent )
  {
    composite = new Composite( parent, SWT.NULL );
    final FormLayout compositeLayout = new FormLayout();
    final GridData compositeData = new GridData();
    compositeData.widthHint = 180;
    composite.setLayoutData( compositeData );
    composite.setLayout( compositeLayout );
    compositeLayout.marginWidth = 0;
    compositeLayout.marginHeight = 0;
    compositeLayout.spacing = 0;
    composite.layout();
    init();
  }

  private void init( )
  {
    final Composite palleteParentComposite = new Composite( composite, SWT.NULL );
    palleteParentComposite.setLayout( new GridLayout() );
    final FormData palleteParentCompositeData = new FormData();
    palleteParentCompositeData.left = new FormAttachment( 340, 1000, 0 );
    palleteParentCompositeData.top = new FormAttachment( 0, 1000, 0 );
    palleteParentComposite.setLayoutData( palleteParentCompositeData );

    comboBox = new ColorPaletteComboBox( palleteParentComposite );
    comboBox.setSelection( colorPaletteSelection );
    final ColorPalette colorPallete = new ColorPalette( palleteParentComposite, getColorArray(), COLOR_SIZE, COLOR_BORDER, getRuleCollection() );

    comboBox.addPanelListener( new PanelListener()
    {
      @Override
      public void valueChanged( final PanelEvent event )
      {
        switch( getComboBox().getSelection() )
        {
          case 0:
          {
            initializeColors( RED_GREEN_TRANSITION, getNumberOfColors() );
            setColorPaletteSelection( 0 );
            setType( RED_GREEN_TRANSITION );
            colorPallete.setColors( getColorArray() );
            break;
          }
          case 1:
          {
            initializeColors( BLUE_GREEN_TRANSITION, getNumberOfColors() );
            setColorPaletteSelection( 1 );
            setType( BLUE_GREEN_TRANSITION );
            colorPallete.setColors( getColorArray() );
            break;
          }
          case 2:
          {
            initializeColors( RED_BLUE_TRANSITION, getNumberOfColors() );
            setColorPaletteSelection( 2 );
            setType( RED_BLUE_TRANSITION );
            colorPallete.setColors( getColorArray() );
            break;
          }
          case 3:
          {
            initializeColors( CUSTOM_TRANSITION, getNumberOfColors() );
            setColorPaletteSelection( 3 );
            setType( CUSTOM_TRANSITION );
            colorPallete.setColors( getCustomColor() );
            break;
          }
          default:
          {
            initializeColors( RED_GREEN_TRANSITION, getNumberOfColors() );
            setColorPaletteSelection( 1 );
            setType( BLUE_GREEN_TRANSITION );
            colorPallete.setColors( getColorArray() );
          }
        }
        colorPallete.setColors( getColorArray() );
        setColorPaletteSelection( getComboBox().getSelection() );
        fire();
      }
    } );

    colorPallete.addColorPaletterListener( new PanelListener()
    {
      @Override
      public void valueChanged( final PanelEvent event )
      {
        // changes allowed only to customize selection
        setColorPaletteSelection( 3 );
        setType( CUSTOM_TRANSITION );
        setCustomColor( colorPallete.getColors() );
        setColorArray( colorPallete.getColors() );
        fire();
      }
    } );

    final Label fillColorLabel = new Label( composite, SWT.NULL );
    final FormData fillColorLabelLData = new FormData();
    fillColorLabelLData.height = 15;
    fillColorLabelLData.width = 242;
    fillColorLabelLData.left = new FormAttachment( 0, 1000, 0 );
    fillColorLabelLData.top = new FormAttachment( 100, 1000, 0 );
    fillColorLabel.setLayoutData( fillColorLabelLData );
    fillColorLabel.setText( MessageBundle.STYLE_EDITOR_COLOR_PATTERN );
  }

  // public static Color[] initializeColors(int type,int numberOfColors)
  public void initializeColors( final int m_type, final int m_numberOfColors )
  {
    type = m_type;
    if( type == CUSTOM_TRANSITION )
    {
      initializeCustomColors( m_numberOfColors );
      return;
    }
    final Color[] colors = new Color[m_numberOfColors];
    int step = 255;
    if( m_numberOfColors > 1 )
      step = 255 / (m_numberOfColors - 1);
    for( int i = 0; i < m_numberOfColors; i++ )
    {
      if( type == BLUE_GREEN_TRANSITION )
      {
        colors[i] = new Color( null, 0, i * step, 255 - i * step );
      }
      else if( type == RED_BLUE_TRANSITION )
      {
        colors[i] = new Color( null, 255 - i * step, 0, i * step );
      }
      else if( type == RED_GREEN_TRANSITION )
      {
        colors[i] = new Color( null, 255 - i * step, i * step, 0 );
      }
    }
    setColorArray( colors );
  }

  private void initializeCustomColors( final int m_numberOfColors )
  {
    // check whether size is appropriate
    if( customColor.length < m_numberOfColors )
    {
      final Color[] tmpColor = new Color[m_numberOfColors];
      int i = 0;
      for( ; i < customColor.length; i++ )
        tmpColor[i] = customColor[i];
      for( ; i < m_numberOfColors; i++ )
        tmpColor[i] = new Color( null, 0, 0, 0 );
      customColor = tmpColor;
    }
    else if( customColor.length > m_numberOfColors )
    {
      final Color[] tmpColor = new Color[m_numberOfColors];
      for( int i = 0; i < m_numberOfColors; i++ )
        tmpColor[i] = customColor[i];
      customColor = tmpColor;
    }
    setColorArray( customColor );
  }

  public void setColorPalette( final Color[] colors )
  {
    setColorArray( colors );
  }

  public Color[] getColorPalette( )
  {
    return getColorArray();
  }

  protected void fire( )
  {
    final Object[] listeners = listenerList.getListenerList();
    for( int i = listeners.length - 2; i >= 0; i -= 2 )
    {
      if( listeners[i] == PanelListener.class )
      {
        final PanelEvent event = new PanelEvent( this );
        ((PanelListener)listeners[i + 1]).valueChanged( event );
      }
    }
  }

  public Color[] getColorArray( )
  {
    return colorArray;
  }

  public void setColorArray( final Color[] m_colorArray )
  {
    numberOfColors = m_colorArray.length;
    colorArray = m_colorArray;
  }

  public int getType( )
  {
    return type;
  }

  public void setType( final int m_type )
  {
    type = m_type;
    switch( type )
    {
      case RED_GREEN_TRANSITION:
      {
        setColorPaletteSelection( 0 );
        break;
      }
      case BLUE_GREEN_TRANSITION:
      {
        setColorPaletteSelection( 1 );
        break;
      }
      case RED_BLUE_TRANSITION:
      {
        setColorPaletteSelection( 2 );
        break;
      }
      default:
      {
        setColorPaletteSelection( 3 );
      }
    }
  }

  public int getNumberOfColors( )
  {
    return numberOfColors;
  }

  public void setNumberOfColors( final int m_numberOfColors )
  {
    numberOfColors = m_numberOfColors;
  }

  public int getColorPaletteSelection( )
  {
    return colorPaletteSelection;
  }

  public void setColorPaletteSelection( final int m_colorPaletteSelection )
  {
    colorPaletteSelection = m_colorPaletteSelection;
    getComboBox().setSelection( m_colorPaletteSelection );
  }

  public Color[] getCustomColor( )
  {
    return customColor;
  }

  public void setCustomColor( final Color[] m_customColor )
  {
    customColor = m_customColor;
  }

  public ColorPaletteComboBox getComboBox( )
  {
    return comboBox;
  }

  public void setComboBox( final ColorPaletteComboBox m_comboBox )
  {
    comboBox = m_comboBox;
  }

  public RuleCollection getRuleCollection( )
  {
    return ruleCollection;
  }

  public void setRuleCollection( final RuleCollection m_ruleCollection )
  {
    ruleCollection = m_ruleCollection;
  }
}