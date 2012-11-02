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
package org.kalypso.ui.editor.styleeditor.fill;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.graphic.GraphicComposite;
import org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.GraphicFill;

/**
 * @author Gernot Belger
 */
public class GraphicFillSection extends AbstractStyleElementSection<Fill, Graphic, GraphicComposite>
{
  public GraphicFillSection( final FormToolkit toolkit, final Composite parent, final IStyleInput<Fill> input )
  {
    super( toolkit, parent, input );
  }

  @Override
  protected String getTitle( )
  {
    return "Graphic"; //$NON-NLS-1$
  }

  @Override
  protected StyleElementAction<Fill>[] createActions( final IStyleInput<Fill> input )
  {
    @SuppressWarnings( "unchecked" ) final StyleElementAction<Fill>[] actions = new StyleElementAction[2];
    actions[0] = new GraphicFillAddAction( input );
    actions[1] = new GraphicFillRemoveAction( input );
    return actions;
  }

  @Override
  protected Graphic getItem( final Fill data )
  {
    final GraphicFill graphicFill = data.getGraphicFill();
    if( graphicFill == null )
      return null;

    return graphicFill.getGraphic();
  }

  @Override
  protected GraphicComposite createItemControl( final Composite parent, final Graphic item )
  {
    final FormToolkit toolkit = getToolkit();
    final IStyleInput<Graphic> input = new StyleInput<>( item, getInput() );
    return new GraphicComposite( toolkit, parent, input );
  }

  @Override
  protected void updateItemControl( final GraphicComposite itemControl )
  {
    itemControl.updateControl();
  }
}