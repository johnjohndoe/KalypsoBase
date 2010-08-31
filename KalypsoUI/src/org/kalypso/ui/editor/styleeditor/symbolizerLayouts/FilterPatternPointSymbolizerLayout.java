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
 * Created on 26.07.2004
 *
 */
package org.kalypso.ui.editor.styleeditor.symbolizerLayouts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.panels.ColorPalettePanel;
import org.kalypso.ui.editor.styleeditor.panels.ComboPanel;
import org.kalypso.ui.editor.styleeditor.panels.PanelEvent;
import org.kalypso.ui.editor.styleeditor.panels.PanelListener;
import org.kalypso.ui.editor.styleeditor.panels.SliderPanel;
import org.kalypso.ui.editor.styleeditor.panels.WellKnownNameComboPanel;
import org.kalypso.ui.editor.styleeditor.rulePattern.RuleCollection;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.Mark;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author F.Lindemann
 */

public class FilterPatternPointSymbolizerLayout extends AbstractSymbolizerLayout
{
  private final int m_selectionIndex = 0;

  private final RuleCollection m_ruleCollection;

  private final int m_symbolizerIndex;

  ColorPalettePanel m_colorPalettePanel;

  public FilterPatternPointSymbolizerLayout( final Composite composite, final Symbolizer symbolizer, final IKalypsoStyle style, final RuleCollection ruleCollection, final int symbolizerIndex )
  {
    super( composite, symbolizer, style );
    m_ruleCollection = ruleCollection;
    m_symbolizerIndex = symbolizerIndex;
  }

  @Override
  public void draw( ) throws FilterEvaluationException
  {
    final GridLayout compositeLayout = new GridLayout();
    compositeLayout.marginHeight = 2;
    // ***** group
    final Group group = new Group( m_composite, SWT.NULL );
    final GridData groupData = new GridData();
    groupData.widthHint = 210;
    groupData.heightHint = 215;
    group.setLayoutData( groupData );
    group.setLayout( compositeLayout );
    group.layout();

    final PointSymbolizer pointSymbolizer = (PointSymbolizer) m_symbolizer;
    final Graphic graphic = pointSymbolizer.getGraphic();

    final Object objects[] = graphic.getMarksAndExtGraphics();
    final Mark mark = (Mark) objects[0];

    final ComboPanel wellKnownNameComboBox = new WellKnownNameComboPanel( group, MessageBundle.STYLE_EDITOR_TYPE, mark.getWellKnownName() );
    for( int i = 0; i < getRuleCollection().size(); i++ )
    {
      final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
      if( symb instanceof PointSymbolizer )
      {
        final Object[] obj = ((PointSymbolizer) symb).getGraphic().getMarksAndExtGraphics();
        if( obj.length > 0 && obj[0] instanceof Mark )
        {
          ((Mark) obj[0]).setWellKnownName( mark.getWellKnownName() );
        }
      }
    }
    wellKnownNameComboBox.addPanelListener( new PanelListener()
    {
      @Override
      public void valueChanged( final PanelEvent event )
      {
        final int index = ((ComboPanel) event.getSource()).getSelection();
        final String wkn = WellKnownNameComboPanel.getWellKnownNameByIndex( index );
        for( int i = 0; i < getRuleCollection().size(); i++ )
        {
          final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
          if( symb instanceof PointSymbolizer )
          {
            final Object[] obj = ((PointSymbolizer) symb).getGraphic().getMarksAndExtGraphics();
            if( obj.length > 0 && obj[0] instanceof Mark )
            {
              ((Mark) obj[0]).setWellKnownName( wkn );
            }
          }
        }
        m_style.fireStyleChanged();
      }
    } );

    final SliderPanel graphicSizePanel = new SliderPanel( group, MessageBundle.STYLE_EDITOR_SIZE, 1, 15, 1, SliderPanel.INTEGER, graphic.getSize( null ) );
    for( int i = 0; i < getRuleCollection().size(); i++ )
    {
      final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
      if( symb instanceof PointSymbolizer )
      {
        ((PointSymbolizer) symb).getGraphic().setSize( graphic.getSize( null ) );
      }
    }
    graphicSizePanel.addPanelListener( new PanelListener()
    {
      @Override
      public void valueChanged( final PanelEvent event )
      {
        final double size = ((SliderPanel) event.getSource()).getSelection();
        for( int i = 0; i < getRuleCollection().size(); i++ )
        {
          final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
          if( symb instanceof PointSymbolizer )
          {
            ((PointSymbolizer) symb).getGraphic().setSize( size );
          }
        }
        m_style.fireStyleChanged();
      }
    } );

    // get all colors for each rule of the pattern for this specific symbolizer
    final Color[] colors = new Color[getRuleCollection().size()];
    for( int i = 0; i < getRuleCollection().size(); i++ )
    {
      final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
      if( symb instanceof PointSymbolizer )
      {
        final Object[] obj = ((PointSymbolizer) symb).getGraphic().getMarksAndExtGraphics();
        if( obj.length > 0 && obj[0] instanceof Mark )
        {
          final java.awt.Color color = ((Mark) obj[0]).getFill().getFill( null );
          colors[i] = new Color( null, color.getRed(), color.getGreen(), color.getBlue() );
        }
      }
    }

    if( m_colorPalettePanel == null )
    {
      m_colorPalettePanel = new ColorPalettePanel( group, colors, getRuleCollection() );
      m_colorPalettePanel.setType( ColorPalettePanel.CUSTOM_TRANSITION );
      // init colors of PointSymbolizer
      for( int i = 0; i < getRuleCollection().size(); i++ )
      {
        final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
        if( symb instanceof PointSymbolizer )
        {
          final Object[] obj = ((PointSymbolizer) symb).getGraphic().getMarksAndExtGraphics();
          if( obj.length > 0 && obj[0] instanceof Mark )
          {
            ((Mark) obj[0]).getFill().setFill( new java.awt.Color( colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue() ) );
          }
        }
      }

      m_colorPalettePanel.addColorPalettePanelListener( new PanelListener()
      {
        @Override
        public void valueChanged( final PanelEvent event )
        {
          final Color[] colorArray = m_colorPalettePanel.getColorPalette();

          for( int i = 0; i < getRuleCollection().size(); i++ )
          {
            final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
            if( symb instanceof PointSymbolizer )
            {
              ((Mark) ((PointSymbolizer) symb).getGraphic().getMarksAndExtGraphics()[0]).setFill( StyleFactory.createFill( new java.awt.Color( colorArray[i].getRed(), colorArray[i].getGreen(), colorArray[i].getBlue() ) ) );
            }
          }
          m_style.fireStyleChanged();
        }
      } );
    }
    else
      m_colorPalettePanel.draw( m_composite );
  }

  public int getSelectionIndex( )
  {
    return m_selectionIndex;
  }

  public int getSymbolizerIndex( )
  {
    return m_symbolizerIndex;
  }

  public RuleCollection getRuleCollection( )
  {
    return m_ruleCollection;
  }
}