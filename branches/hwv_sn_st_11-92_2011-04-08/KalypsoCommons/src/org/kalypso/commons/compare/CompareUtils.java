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
package org.kalypso.commons.compare;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;

/**
 * Utilities for <code>org.eclipse.compare</code> stuff.
 * 
 * @author Gernot Belger
 */
public final class CompareUtils
{
  private CompareUtils( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  public static void dumpDiffElement( final IDiffElement element, final int intendation, final IElementDumper elementDumper )
  {
    for( int i = 0; i < intendation; i++ )
      System.out.print( " " );

    final String directionLabel = getDirectionLabel( element );
    final String changeLabel = getChangeLabel( element );
    final String diffDump = String.format( "%s %s: %s", directionLabel, changeLabel, element.getName() );
    System.out.println( diffDump );

    if( element instanceof ICompareInput && elementDumper != null )
    {
      if( (element.getKind() & Differencer.CHANGE_TYPE_MASK) == Differencer.CHANGE )
        elementDumper.dumpElement( (ICompareInput) element );
    }

    if( element instanceof IDiffContainer )
    {
      final IDiffContainer container = (IDiffContainer) element;
      final IDiffElement[] children = container.getChildren();
      for( final IDiffElement child : children )
        dumpDiffElement( child, intendation + 1, elementDumper );
    }
  }

  public static String getDirectionLabel( final IDiffElement element )
  {
    switch( element.getKind() & Differencer.DIRECTION_MASK )
    {
      case Differencer.LEFT:
        return ">"; //$NON-NLS-1$
      case Differencer.RIGHT:
        return "<"; //$NON-NLS-1$
      case Differencer.CONFLICTING:
        return "!"; //$NON-NLS-1$
    }
    return " "; //$NON-NLS-1$
  }

  public static String getChangeLabel( final IDiffElement element )
  {
    switch( element.getKind() & Differencer.CHANGE_TYPE_MASK )
    {
      case Differencer.ADDITION:
        return "+"; //$NON-NLS-1$
      case Differencer.DELETION:
        return "-"; //$NON-NLS-1$
      case Differencer.CHANGE:
        return "#"; //$NON-NLS-1$
    }
    return "="; //$NON-NLS-1$
  }

}
