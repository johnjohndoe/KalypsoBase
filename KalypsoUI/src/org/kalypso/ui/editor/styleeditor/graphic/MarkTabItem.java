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
package org.kalypso.ui.editor.styleeditor.graphic;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypsodeegree.graphics.sld.Mark;

/**
 * @author Gernot Belger
 */
public class MarkTabItem implements IGraphicElementItem
{
  private final IStyleInput<Mark> m_input;

  private MarkComposite m_markComposite;

  public MarkTabItem( final IStyleInput<Mark> input )
  {
    m_input = input;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabItem#getItemLabel()
   */
  @Override
  public String getItemLabel( )
  {
    return "Mark";
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabItem#getItemImage()
   */
  @Override
  public Image getItemImage( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabItem#createItemControl(org.eclipse.ui.forms.widgets.FormToolkit, org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control createItemControl( final FormToolkit toolkit, final Composite parent )
  {
    m_markComposite = new MarkComposite( toolkit, parent, m_input );
    return m_markComposite;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabItem#updateItemControl()
   */
  @Override
  public void updateItemControl( )
  {
    m_markComposite.updateControl();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.symbolizer.IGraphicElementItem#getElement()
   */
  @Override
  public Object getElement( )
  {
    return m_input.getData();
  }
}