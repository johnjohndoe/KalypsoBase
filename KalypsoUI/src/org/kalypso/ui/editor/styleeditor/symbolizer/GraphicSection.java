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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.i18n.Messages;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.graphic.GraphicComposite;
import org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;

/**
 * @author Gernot Belger
 */
public class GraphicSection extends AbstractStyleElementSection<PointSymbolizer, Graphic, GraphicComposite>
{
  protected GraphicSection( final FormToolkit toolkit, final Composite parent, final IStyleInput<PointSymbolizer> input )
  {
    super( toolkit, parent, input );
  }

  @Override
  protected String getTitle( )
  {
    return "Graphic"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection#getDescription()
   */
  @Override
  protected String getDescription( )
  {
    return Messages.getString("GraphicSection_1"); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection#createActions(org.kalypso.ui.editor.styleeditor.forms.IInputWithContext)
   */
  @Override
  protected StyleElementAction<PointSymbolizer>[] createActions( final IStyleInput<PointSymbolizer> input )
  {
    @SuppressWarnings("unchecked")
    final StyleElementAction<PointSymbolizer>[] actions = new StyleElementAction[2];
    actions[0] = new GraphicAddAction( input );
    actions[1] = new GraphicRemoveAction( input );
    return actions;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection#getItem(java.lang.Object)
   */
  @Override
  protected Graphic getItem( final PointSymbolizer data )
  {
    return data.getGraphic();
  }

  @Override
  protected GraphicComposite createItemControl( final Composite parent, final Graphic item )
  {
    final FormToolkit toolkit = getToolkit();
    final IStyleInput<Graphic> input = new StyleInput<Graphic>( item, getInput() );
    return new GraphicComposite( toolkit, parent, input );
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.AbstractStyleElementSection#updateItemControl(java.lang.Object)
   */
  @Override
  protected void updateItemControl( final GraphicComposite itemControl )
  {
    itemControl.updateControl();
  }
}