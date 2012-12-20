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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.panels.ColorPalettePanel;
import org.kalypso.ui.editor.styleeditor.panels.PanelEvent;
import org.kalypso.ui.editor.styleeditor.panels.PanelListener;
import org.kalypso.ui.editor.styleeditor.preview.SymbolizerPreview;
import org.kalypso.ui.editor.styleeditor.style.RuleCollection;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Symbolizer;

/**
 * @author F.Lindemann
 */
public class FilterPatternPolygonSymbolizerLayout extends AbstractSymbolizerComposite<PolygonSymbolizer>
{
  private int m_selectionIndex = 0;

  private RuleCollection m_ruleCollection;

  private int m_symbolizerIndex = -1;

  ColorPalettePanel colorPalettePanel = null;

  public FilterPatternPolygonSymbolizerLayout( final FormToolkit toolkit, final Composite parent, final IStyleInput<PolygonSymbolizer> input, final RuleCollection ruleCollection, final int symbolizerIndex )
  {
    super( toolkit, parent, input );

    m_ruleCollection = ruleCollection;
    m_symbolizerIndex = symbolizerIndex;
  }

  @Override
  protected Control createContent( final FormToolkit toolkit, final Composite parent )
  {
    final Composite panel = toolkit.createComposite( parent );
    GridLayoutFactory.fillDefaults().applyTo( panel );

    try
    {
      // get all colors for each rule of the pattern for this specific symbolizer
      final Color[] colors = new Color[getRuleCollection().size()];
      for( int i = 0; i < getRuleCollection().size(); i++ )
      {
        final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
        if( symb instanceof PolygonSymbolizer )
        {
          final java.awt.Color color = ((PolygonSymbolizer)symb).getFill().getFill( null );
          colors[i] = new Color( null, color.getRed(), color.getGreen(), color.getBlue() );
        }
      }

      if( colorPalettePanel == null )
      {
        colorPalettePanel = new ColorPalettePanel( panel, colors, getRuleCollection() );
        colorPalettePanel.setType( ColorPalettePanel.CUSTOM_TRANSITION );
        // init colors of PolygonSymbolizer
        for( int i = 0; i < getRuleCollection().size(); i++ )
        {
          final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
          if( symb instanceof PolygonSymbolizer )
          {
            ((PolygonSymbolizer)symb).getFill().setFill( new java.awt.Color( colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue() ) );
          }
        }

        colorPalettePanel.addColorPalettePanelListener( new PanelListener()
        {
          @Override
          public void valueChanged( final PanelEvent event )
          {
            final Color[] colorArray = colorPalettePanel.getColorPalette();

            for( int i = 0; i < getRuleCollection().size(); i++ )
            {
              final Symbolizer symb = getRuleCollection().get( i ).getSymbolizers()[getSymbolizerIndex()];
              if( symb instanceof PolygonSymbolizer )
              {
                ((PolygonSymbolizer)symb).getFill().setFill( new java.awt.Color( colorArray[i].getRed(), colorArray[i].getGreen(), colorArray[i].getBlue() ) );
              }
            }
            fireStyleChanged();
          }
        } );
      }
      else
        colorPalettePanel.draw( panel );
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }

    return panel;
  }

  @Override
  protected SymbolizerPreview<PolygonSymbolizer> createPreview( final Composite parent, final Point size, final IStyleInput<PolygonSymbolizer> input )
  {
    return null;
  }

  @Override
  protected void doUpdateControl( )
  {
  }

  public int getSelectionIndex( )
  {
    return m_selectionIndex;
  }

  public void setSelectionIndex( final int selectionIndex )
  {
    m_selectionIndex = selectionIndex;
  }

  public int getSymbolizerIndex( )
  {
    return m_symbolizerIndex;
  }

  public void setSymbolizerIndex( final int symbolizerIndex )
  {
    m_symbolizerIndex = symbolizerIndex;
  }

  public RuleCollection getRuleCollection( )
  {
    return m_ruleCollection;
  }

  public void setRuleCollection( final RuleCollection ruleCollection )
  {
    m_ruleCollection = ruleCollection;
  }
}