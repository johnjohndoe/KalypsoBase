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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.halo.HaloComposite;
import org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypsodeegree.graphics.sld.Halo;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;

/**
 * The section controls a label placement inside a {@link org.kalypsodeegree.graphics.sld.TextSymbolizer} and allows to
 * add remove it.
 * 
 * @author Gernot Belger
 */
public class HaloSection extends AbstractStyleElementSection<TextSymbolizer, Halo, HaloComposite>
{
  protected HaloSection( final FormToolkit toolkit, final Composite parent, final IStyleInput<TextSymbolizer> input )
  {
    super( toolkit, parent, input );
  }

  @Override
  protected StyleElementAction<TextSymbolizer>[] createActions( final IStyleInput<TextSymbolizer> input )
  {
    @SuppressWarnings("unchecked")
    final StyleElementAction<TextSymbolizer>[] actions = new StyleElementAction[2];
    actions[0] = new HaloAddAction( input );
    actions[1] = new HaloRemoveAction( input );
    return actions;
  }

  @Override
  protected String getTitle( )
  {
    return "Halo";
  }

  @Override
  protected Halo getItem( final TextSymbolizer data )
  {
    return data.getHalo();
  }

  @Override
  protected HaloComposite createItemControl( final Composite parent, final Halo item )
  {
    final FormToolkit toolkit = getToolkit();
    final IStyleInput<Halo> input = new StyleInput<Halo>( item, getInput() );
    return new HaloComposite( toolkit, parent, input );
  }

  @Override
  protected void updateItemControl( final HaloComposite itemControl )
  {
    itemControl.updateControl();
  }
}