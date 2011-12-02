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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.fill.FillComposite;
import org.kalypso.ui.editor.styleeditor.stroke.StrokeComposite;
import org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;

/**
 * @author Gernot Belger
 */
class FillSection extends AbstractStyleElementSection<PolygonSymbolizer, Fill, FillComposite>
{
  protected FillSection( final FormToolkit toolkit, final Composite parent, final IStyleInput<PolygonSymbolizer> input )
  {
    super( toolkit, parent, input );
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection#getTitle()
   */
  @Override
  protected String getTitle( )
  {
    return MessageBundle.STYLE_EDITOR_FILL;
  }

  @Override
  protected StyleElementAction<PolygonSymbolizer>[] createActions( final IStyleInput<PolygonSymbolizer> input )
  {
    @SuppressWarnings("unchecked")
    final StyleElementAction<PolygonSymbolizer>[] actions = new StyleElementAction[2];
    actions[0] = new FillAddAction( input );
    actions[1] = new FillRemoveAction( input );
    return actions;
  }

  @Override
  protected Fill getItem( final PolygonSymbolizer data )
  {
    return data.getFill();
  }

  @Override
  protected FillComposite createItemControl( final Composite parent, final Fill item )
  {
    final FormToolkit toolkit = getToolkit();
    final IStyleInput<Fill> input = new StyleInput<Fill>( item, getInput() );

    final boolean showGraphics = input.getConfig().isPolygonSymbolizerShowGraphic();
    final int sldStyle = showGraphics ? SWT.NONE : StrokeComposite.HIDE_GRAPHIC;

    return new FillComposite( toolkit, parent, input, sldStyle );
  }

  @Override
  protected void updateItemControl( final FillComposite itemControl )
  {
    itemControl.updateControl();
  }
}