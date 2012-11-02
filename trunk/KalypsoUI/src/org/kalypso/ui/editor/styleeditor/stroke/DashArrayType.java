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
package org.kalypso.ui.editor.styleeditor.stroke;

import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public enum DashArrayType
{
  continuous( Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.3" ) ), //$NON-NLS-1$
  dot( Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.4" ), 2f, 2f ), //$NON-NLS-1$
  dash( Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.5" ), 10f, 5f ), //$NON-NLS-1$
  dash_dot( Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.6" ), 10f, 5f, 2f, 2f ); //$NON-NLS-1$

  private final String m_label;

  private final float[] m_dashes;

  private DashArrayType( final String label, final float... dashes )
  {
    m_label = label;
    m_dashes = dashes;
  }

  public float[] getDashes( )
  {
    return m_dashes;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString( )
  {
    return m_label;
  }
}
