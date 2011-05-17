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
package org.kalypso.ui.editor.styleeditor.util;

import org.eclipse.jface.action.Action;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;

/**
 * @author Gernot Belger
 */
public abstract class StyleElementAction<ELEMENT> extends Action implements IUpdateable
{
  private final IStyleInput<ELEMENT> m_input;

  public StyleElementAction( final IStyleInput<ELEMENT> input )
  {
    m_input = input;
  }

  protected abstract boolean checkEnabled( ELEMENT data );

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IUpdateable#update()
   */
  @Override
  public final void update( )
  {
    final ELEMENT element = m_input.getData();
    setEnabled( checkEnabled( element ) );
  }

  @Override
  public final void run( )
  {
    final ELEMENT element = m_input.getData();
    changeElement( element );

    m_input.fireStyleChanged();
  }

  protected abstract void changeElement( ELEMENT data );
}