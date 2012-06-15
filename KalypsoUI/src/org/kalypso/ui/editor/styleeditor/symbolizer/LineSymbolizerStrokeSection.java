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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.stroke.StrokeComposite;
import org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;
import org.kalypsodeegree.graphics.sld.Stroke;

/**
 * @author Gernot Belger
 */
class LineSymbolizerStrokeSection extends AbstractStyleElementSection<LineSymbolizer, Stroke, StrokeComposite>
{
  LineSymbolizerStrokeSection( final FormToolkit toolkit, final Composite parent, final IStyleInput<LineSymbolizer> input )
  {
    super( toolkit, parent, input );
  }

  @Override
  protected String getTitle( )
  {
    return "Line";
  }

  @Override
  protected StyleElementAction<LineSymbolizer>[] createActions( final IStyleInput<LineSymbolizer> input )
  {
    @SuppressWarnings("unchecked")
    final StyleElementAction<LineSymbolizer>[] actions = new StyleElementAction[2];
    actions[0] = new LineSymbolizerStrokeAddAction( input );
    actions[1] = new LineSymbolizerStrokeRemoveAction( input );
    return actions;
  }

  @Override
  protected Stroke getItem( final LineSymbolizer data )
  {
    return data.getStroke();
  }

  @Override
  protected StrokeComposite createItemControl( final Composite parent, final Stroke item )
  {
    final FormToolkit toolkit = getToolkit();

    final IStyleInput<LineSymbolizer> input = getInput();

    final IStyleInput<Stroke> strokeInput = new StyleInput<Stroke>( item, input );

    final int sldStyle = findSldStyle( input );

    return new StrokeComposite( toolkit, parent, strokeInput, sldStyle );
  }

  private int findSldStyle( final IStyleInput<LineSymbolizer> input )
  {
    int style = SWT.NONE;

    if( !input.getConfig().isLineSymbolizerStrokeLineDetails() )
      style |= StrokeComposite.HIDE_LINE_DETAILS;

    if( !input.getConfig().isLineSymbolizerShowGraphic() )
      style |= StrokeComposite.HIDE_GRAPHIC;

    return style;
  }

  @Override
  protected void updateItemControl( final StrokeComposite itemControl )
  {
    itemControl.updateControl();
  }
}