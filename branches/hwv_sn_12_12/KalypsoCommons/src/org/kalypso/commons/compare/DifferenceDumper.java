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

import junit.framework.Assert;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.compare.structuremergeviewer.IDiffElement;

/**
 * Utilities for <code>org.eclipse.compare</code> stuff.
 * 
 * @author Gernot Belger
 */
public final class DifferenceDumper
{
  private final Object m_differences;

  private final IElementDumper m_elementDumper;

  /**
   * @param differences
   *          Should be the result of
   *          {@link org.eclipse.compare.structuremergeviewer.Differencer#findDifferences(boolean, org.eclipse.core.runtime.IProgressMonitor, Object, Object, Object, Object)}
   *          .
   * @param elementDumper
   *          Used to dump elements that have changes. May be <code>null</code>.
   */
  public DifferenceDumper( final Object differences, final IElementDumper elementDumper )
  {
    m_differences = differences;
    m_elementDumper = elementDumper;
  }

  public boolean hasDifferences( )
  {
    return m_differences != null;
  }

  /**
   * @return <code>true</code> If differences exist.
   */
  public void dumpDifferences( )
  {
    if( !hasDifferences() )
      return;

    if( !(m_differences instanceof IDiffElement) )
      Assert.fail( "Unknown differencer result: " + ObjectUtils.toString( m_differences ) );

    final IDiffElement element = (IDiffElement) m_differences;
    CompareUtils.dumpDiffElement( element, 0, m_elementDumper );
  }

}
