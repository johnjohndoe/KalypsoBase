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
import org.kalypso.ui.editor.styleeditor.placement.LabelPlacementComposite;
import org.kalypso.ui.editor.styleeditor.placement.LinePlacementAction;
import org.kalypso.ui.editor.styleeditor.placement.PointPlacementAction;
import org.kalypso.ui.editor.styleeditor.placement.RemovePlacementAction;
import org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.LabelPlacement;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;

/**
 * The section controls a label placement inside a {@link org.kalypsodeegree.graphics.sld.TextSymbolizer} and allows to
 * add remove it.
 * 
 * @author Gernot Belger
 */
public class LabelPlacementSection extends AbstractStyleElementSection<TextSymbolizer, LabelPlacement, LabelPlacementComposite>
{
  protected LabelPlacementSection( final FormToolkit toolkit, final Composite parent, final IStyleInput<TextSymbolizer> input )
  {
    super( toolkit, parent, input );
  }

  @Override
  protected String getTitle( )
  {
    return Messages.getString( "LabelPlacementSection_0" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection#createActions(org.kalypso.ui.editor.styleeditor.forms.IInputWithContext)
   */
  @Override
  protected StyleElementAction<TextSymbolizer>[] createActions( final IStyleInput<TextSymbolizer> input )
  {
    @SuppressWarnings( "unchecked" ) final StyleElementAction<TextSymbolizer>[] actions = new StyleElementAction[3];
    actions[0] = new PointPlacementAction( input );
    actions[1] = new LinePlacementAction( input );
    actions[2] = new RemovePlacementAction( input );
    return actions;
  }

  @Override
  protected LabelPlacement getItem( final TextSymbolizer data )
  {
    return data.getLabelPlacement();
  }

  @Override
  protected LabelPlacementComposite createItemControl( final Composite parent, final LabelPlacement item )
  {
    final IStyleInput<LabelPlacement> input = new StyleInput<>( item, getInput() );
    return new LabelPlacementComposite( getToolkit(), parent, input );
  }

  @Override
  protected void updateItemControl( final LabelPlacementComposite itemControl )
  {
    itemControl.updateControl();
  }
}