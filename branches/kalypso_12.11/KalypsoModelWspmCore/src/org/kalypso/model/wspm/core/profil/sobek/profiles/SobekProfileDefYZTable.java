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
package org.kalypso.model.wspm.core.profil.sobek.profiles;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * @author Gernot Belger
 */
public class SobekProfileDefYZTable implements ISobekProfileDefData
{
  private final Collection<SobekYZPoint> m_points = new ArrayList<>();

  private final int m_st;

  private final BigDecimal m_sw;

  public SobekProfileDefYZTable( final int st, final BigDecimal sw )
  {
    m_st = st;
    m_sw = sw;
  }

  @Override
  public int getType( )
  {
    return 10;
  }

  @Override
  public void writeContent( final PrintWriter writer )
  {
    writer.format( "st %s lt sw %s 0 gl 0 gu 0 lt yz%n", m_st, m_sw ); //$NON-NLS-1$

    writer.println( "TBLE" ); //$NON-NLS-1$

    for( final SobekYZPoint point : m_points )
    {
      final BigDecimal y = point.getY();
      final BigDecimal z = point.getZ();
      writer.format( Locale.US, "%s %s <%n", y, z ); //$NON-NLS-1$
    }

    writer.println( "tble" ); //$NON-NLS-1$
  }

  public void addPoint( final SobekYZPoint point )
  {
    m_points.add( point );
  }

  public SobekYZPoint[] getPoints( )
  {
    return m_points.toArray( new SobekYZPoint[m_points.size()] );
  }
}