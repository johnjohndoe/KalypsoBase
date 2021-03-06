/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.ui.editor.diagrameditor.actions;

import java.util.Arrays;

import org.kalypso.ui.internal.i18n.Messages;

/**
 * Represents dash arrays for awt-strokes.
 * 
 * @author Gernot Belger
 */
public class DashType
{
  public static DashType NONE = new DashType( Messages.getString( "org.kalypso.ui.editor.diagrameditor.actions.DashType.0" ), "---------", new float[] {} ); //$NON-NLS-1$ //$NON-NLS-2$

  public static DashType SIMPLE_1 = new DashType( Messages.getString( "org.kalypso.ui.editor.diagrameditor.actions.DashType.2" ), "- - - - -", new float[] { 10f, 10f } ); //$NON-NLS-1$ //$NON-NLS-2$

  public static DashType SIMPLE_2 = new DashType( Messages.getString( "org.kalypso.ui.editor.diagrameditor.actions.DashType.4" ), ". . . . .", new float[] { 5f, 5f } ); //$NON-NLS-1$ //$NON-NLS-2$

  public static DashType SIMPLE_3 = new DashType( Messages.getString( "org.kalypso.ui.editor.diagrameditor.actions.DashType.6" ), "-- -- -- ", new float[] { 10f, 2f } ); //$NON-NLS-1$ //$NON-NLS-2$

  public static DashType SIMPLE_4 = new DashType( Messages.getString( "org.kalypso.ui.editor.diagrameditor.actions.DashType.8" ), ".........", new float[] { 2f, 1f } ); //$NON-NLS-1$ //$NON-NLS-2$

  public static DashType[] KNOWN_DASHS = new DashType[] { NONE, SIMPLE_1, SIMPLE_2, SIMPLE_3, SIMPLE_4 };

  private final String m_label;

  private final float[] m_dashs;

  private final String m_comboLabel;

  public DashType( final String label, final String comboLabel, final float[] dashs )
  {
    m_label = label;
    m_comboLabel = comboLabel;
    m_dashs = dashs;
  }

  public float[] getDashs( )
  {
    return m_dashs;
  }

  public String getComboLabel( )
  {
    return m_comboLabel;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_label;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof DashType )
    {
      final DashType other = (DashType)obj;
      return Arrays.equals( m_dashs, other.m_dashs );
    }

    return false;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    // Not correct, but works
    return m_dashs.hashCode();
  }

  public static boolean isKnownDash( final DashType dash )
  {
    for( final DashType element : KNOWN_DASHS )
    {
      if( element.equals( dash ) )
        return true;
    }

    return false;
  }

  public static DashType findKnownDash( final DashType dash )
  {
    for( final DashType element : KNOWN_DASHS )
    {
      if( element.equals( dash ) )
        return element;
    }

    return null;
  }

}
