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
import java.util.Locale;

/**
 * @author Gernot Belger
 */
public class SobekFrictionDat
{
  public static enum FrictionType
  {
    Chezy,
    Manning,
    Strickler_Kn,
    Strickler_Ks, // = WSPM kst
    White_Colebrook, // WSPM ks
    unknown1,
    unknown2,
    De_Bos_and_Bijkerk;
  }

  private final String m_id;

  private final String m_name;

  private final String m_csID;

  private final SobekFrictionDatCRFRSection[] m_sections;

  public SobekFrictionDat( final String id, final String name, final String csID, final SobekFrictionDatCRFRSection[] sections )
  {
    m_id = id;
    m_name = name;
    m_csID = csID;
    m_sections = sections;
  }

  public String getCsID( )
  {
    return m_csID;
  }

  public SobekFrictionDatCRFRSection[] getSections( )
  {
    return m_sections;
  }

  public void serialize( final PrintWriter writer )
  {
    writer.format( "CRFR id '%s' nm '%s' cs '%s'%n", m_id, m_name, m_csID ); //$NON-NLS-1$

    writeZoneTable( writer );

    writePositiveTable( writer );
    writeNegativeTable( writer );

    writer.print( "crfr" ); //$NON-NLS-1$
  }

  private void writeZoneTable( final PrintWriter writer )
  {
    writer.format( "lt ys%n" ); //$NON-NLS-1$
    writer.format( "TBLE%n" ); //$NON-NLS-1$

    for( final SobekFrictionDatCRFRSection zone : m_sections )
      writer.format( "%s %s <%n", zone.getStart(), zone.getEnd() ); //$NON-NLS-1$

    writer.format( "tble%n" ); //$NON-NLS-1$
  }

  private void writePositiveTable( final PrintWriter writer )
  {
    writer.println( "ft ys" ); //$NON-NLS-1$
    writer.format( "TBLE%n" ); //$NON-NLS-1$

    for( final SobekFrictionDatCRFRSection zone : m_sections )
    {
      final FrictionType type = zone.getPositiveType();
      final BigDecimal value = zone.getPositiveValue();
      final int scale = SobekFrictionDatCRFRSection.getTypeScale( type );

      final BigDecimal scaledValue = value.setScale( scale, BigDecimal.ROUND_HALF_UP );

      writer.format( Locale.US, "%d %s <%n", type.ordinal(), scaledValue ); //$NON-NLS-1$
    }

    writer.format( "tble%n" ); //$NON-NLS-1$
  }

  private void writeNegativeTable( final PrintWriter writer )
  {
    writer.println( "fr ys" ); //$NON-NLS-1$
    writer.format( "TBLE%n" ); //$NON-NLS-1$

    for( final SobekFrictionDatCRFRSection zone : m_sections )
    {
      final FrictionType type = zone.getNegativeType();
      final BigDecimal value = zone.getNegativeValue();
      final int scale = SobekFrictionDatCRFRSection.getTypeScale( type );

      final BigDecimal scaledValue = value.setScale( scale, BigDecimal.ROUND_HALF_UP );

      writer.format( Locale.US, "%d %s <%n", type.ordinal(), scaledValue ); //$NON-NLS-1$
    }

    writer.format( "tble%n" ); //$NON-NLS-1$
  }
}