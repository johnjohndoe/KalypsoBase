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

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.kalypso.model.wspm.core.i18n.Messages;

/**
 * Sobek cross section data corresponding to type "0. Tabulated cross section"
 * 
 * @author Gernot Belger
 */
public class SobekProfileDefTabulatedCrossSection implements ISobekProfileDefData
{
  /**
   * The width of the main channel.
   */
  private final BigDecimal m_wm;

  /**
   * The width of the floodplain 1 (used in River profile only, else value = 0).
   */
  private final BigDecimal m_w1;

  /**
   * The width of the floodplain 2 (used in River profile only, else value = 0).
   */
  private final BigDecimal m_w2;

  /**
   * The sediment transport width (not in SOBEK Urban/Rural). Default 0. Only important for module sediment/morfology.
   */
  private final BigDecimal m_sw;

  /**
   * The data for the heights of a tabulated sobek profile.
   */
  private final List<SobekProfileHeight> m_profileHeights = new ArrayList<>();

  /**
   * Summer dike (1 = active, 0 = not active) (in River profile only).
   */
  private final int m_dk;

  /**
   * The dike crest level (in River profile only).
   */
  private final BigDecimal m_dc;

  /**
   * The floodplain base level behind the dike (in River profile only).
   */
  private final BigDecimal m_db;

  /**
   * The flow area behind the dike (in River profile only).
   */
  private final BigDecimal m_df;

  /**
   * The total area behind the dike (in River profile only).
   */
  private final BigDecimal m_dt;

  /**
   * The ground layer depth (meter relative to bed level).
   */
  private final BigDecimal m_gl;

  /**
   * The ground layer to be used within hydraulics calculation (1) or not (0).
   */
  private final int m_gu;

  public SobekProfileDefTabulatedCrossSection( final BigDecimal wm, final BigDecimal w1, final BigDecimal w2, final BigDecimal sw, final SobekProfileHeight[] profileHeights, final BigDecimal gl, final int gu )
  {
    // FIXME: when writing, consider case where no dike is defined
    this( wm, w1, w2, sw, profileHeights, 0, null, null, null, null, gl, gu );
  }

  /**
   * @param wm
   *          The width of the main channel.
   * @param w1
   *          The width of the floodplain 1 (used in River profile only, else value = 0).
   * @param w2
   *          The width of the floodplain 2 (used in River profile only, else value = 0).
   * @param sw
   *          The sediment transport width (not in SOBEK Urban/Rural). Default 0. Only important for module
   *          sediment/morfology.
   * @param profileHeights
   *          The height steps for this profile. One height step contains the height, the full width and the flow width.
   * @param dk
   *          Summer dike (1 = active, 0 = not active) (in River profile only).
   * @param dc
   *          The dike crest level (in River profile only).
   * @param db
   *          The floodplain base level behind the dike (in River profile only).
   * @param df
   *          The flow area behind the dike (in River profile only).
   * @param dt
   *          The total area behind the dike (in River profile only).
   * @param gl
   *          The ground layer depth (meter relative to bed level).
   * @param gu
   *          The ground layer to be used within hydraulics calculation (1) or not (0).
   */
  public SobekProfileDefTabulatedCrossSection( final BigDecimal wm, final BigDecimal w1, final BigDecimal w2, final BigDecimal sw, final SobekProfileHeight[] profileHeights, final int dk, final BigDecimal dc, final BigDecimal db, final BigDecimal df, final BigDecimal dt, final BigDecimal gl, final int gu )
  {
    m_wm = wm;
    m_w1 = w1;
    m_w2 = w2;
    m_sw = sw;
    m_profileHeights.addAll( Arrays.asList( profileHeights ) );
    m_dk = dk;
    m_dc = dc;
    m_db = db;
    m_df = df;
    m_dt = dt;
    m_gl = gl;
    m_gu = gu;
  }

  @Override
  public int getType( )
  {
    return 0;
  }

  /**
   * This function returns the width of the main channel.
   * 
   * @return The width of the main channel.
   */
  public BigDecimal getWm( )
  {
    return m_wm;
  }

  /**
   * This function returns the width of the floodplain 1 (used in River profile only, else value = 0).
   * 
   * @return The width of the floodplain 1 (used in River profile only, else value = 0).
   */
  public BigDecimal getW1( )
  {
    return m_w1;
  }

  /**
   * This function returns the width of the floodplain 2 (used in River profile only, else value = 0).
   * 
   * @return The width of the floodplain 2 (used in River profile only, else value = 0).
   */
  public BigDecimal getW2( )
  {
    return m_w2;
  }

  /**
   * This function returns the sediment transport width (not in SOBEK Urban/Rural). Default 0. Only important for module
   * sediment/morfology.
   * 
   * @return The sediment transport width (not in SOBEK Urban/Rural). Default 0. Only important for module
   *         sediment/morfology.
   */
  public BigDecimal getSw( )
  {
    return m_sw;
  }

  /**
   * This function returns the data for the heights of a tabulated sobek profile.
   * 
   * @return The data for the heights of a tabulated sobek profile.
   */
  public SobekProfileHeight[] getProfileHeights( )
  {
    return m_profileHeights.toArray( new SobekProfileHeight[m_profileHeights.size()] );
  }

  /**
   * This function returns summer dike (1 = active, 0 = not active) (in River profile only).
   * 
   * @return Summer dike (1 = active, 0 = not active) (in River profile only).
   */
  public int getDk( )
  {
    return m_dk;
  }

  /**
   * This function returns the dike crest level (in River profile only).
   * 
   * @return The dike crest level (in River profile only).
   */
  public BigDecimal getDc( )
  {
    return m_dc;
  }

  /**
   * This function returns the dike crest level (in River profile only).
   * 
   * @return The dike crest level (in River profile only).
   */
  public BigDecimal getDb( )
  {
    return m_db;
  }

  /**
   * This function returns the flow area behind the dike (in River profile only).
   * 
   * @return The flow area behind the dike (in River profile only).
   */
  public BigDecimal getDf( )
  {
    return m_df;
  }

  /**
   * This function returns the total area behind the dike (in River profile only).
   * 
   * @return The total area behind the dike (in River profile only).
   */
  public BigDecimal getDt( )
  {
    return m_dt;
  }

  /**
   * This function returns the ground layer depth (meter relative to bed level).
   * 
   * @return The ground layer depth (meter relative to bed level).
   */
  public BigDecimal getGl( )
  {
    return m_gl;
  }

  /**
   * This function returns the ground layer to be used within hydraulics calculation (1) or not (0).
   * 
   * @return The ground layer to be used within hydraulics calculation (1) or not (0).
   */
  public int getGu( )
  {
    return m_gu;
  }

  @Override
  public void writeContent( final Writer writer ) throws IOException
  {
    /* Create a string builder. */
    final StringBuilder line = new StringBuilder();

    /* Create warnings for each sobek profile height. */
    final List<SobekProfileWarning> profileWarnings = new ArrayList<>();

    line.append( serializeCrdsStart( profileWarnings ) );
    line.append( serializeTable( m_profileHeights, profileWarnings ) );
    line.append( serializeDikes( m_dk, m_dc, m_db, m_df, m_dt ) );

    writer.write( line.toString() );
  }

  private String serializeCrdsStart( final List<SobekProfileWarning> profileWarnings )
  {
    final BigDecimal w2 = calculateW2( profileWarnings );

    return String.format( Locale.PRC, "wm %.2f w1 %.2f w2 %.2f sw %.2f gl %.2f gu %d lt lw%n", m_wm, m_w1, w2, m_sw, m_gl, m_gu ); //$NON-NLS-1$
  }

  private BigDecimal calculateW2( final List<SobekProfileWarning> profileWarnings )
  {

    /* Check all heights, full widths and flow widths. */
    double lastHeight = -1.0;
    double lastFullWidth = -1.0;
    double lastFlowWidth = -1.0;
    for( int i = 0; i < m_profileHeights.size(); i++ )
    {
      /* Get the sobek profile height. */
      final SobekProfileHeight profileHeight = m_profileHeights.get( i );

      /* Create a sobek profile warning. */
      /* It will be empty. */
      final SobekProfileWarning profileWarning = new SobekProfileWarning();

      /* Get the values. */
      final double height = profileHeight.getHeight().doubleValue();
      final double fullWidth = profileHeight.getFullWidth().doubleValue();
      final double flowWidth = profileHeight.getFlowWidth().doubleValue();

      /* Create a warning. */
      if( height < lastHeight || fullWidth < lastFullWidth || flowWidth < lastFlowWidth )
      {
        profileWarning.addWarning( String.format( Locale.PRC, Messages.getString( "SobekProfileDef_0" ), height, lastHeight, fullWidth, lastFullWidth, flowWidth, lastFlowWidth ) ); //$NON-NLS-1$
      }

      /* Create a warning. */
      if( fullWidth < flowWidth )
      {
        profileWarning.addWarning( String.format( Locale.PRC, Messages.getString( "SobekProfileDef_4" ), flowWidth, fullWidth ) ); //$NON-NLS-1$
      }

      /* Store the warning. */
      profileWarnings.add( profileWarning );

      /* The current values will be the used for comparison in the next loop. */
      lastHeight = height;
      lastFullWidth = fullWidth;
      lastFlowWidth = flowWidth;
    }

    BigDecimal w2 = m_w2;
    final double width = m_wm.doubleValue() + m_w1.doubleValue() + m_w2.doubleValue();
    final double diff = width - lastFlowWidth;
    if( Math.abs( diff ) > 0.0001 )
    {
      /* Adjust the width of the flood plain 2. */
      w2 = new BigDecimal( lastFlowWidth - (m_wm.doubleValue() + m_w1.doubleValue()) );

      /* Add a warning in the last sobek profile warning. */
      final SobekProfileWarning profileWarning = profileWarnings.get( profileWarnings.size() - 1 );
      profileWarning.addWarning( String.format( Locale.PRC, Messages.getString( "SobekProfileDef_5" ), w2, width, lastFlowWidth ) ); //$NON-NLS-1$
    }

    return w2;
  }

  private String serializeTable( final List<SobekProfileHeight> profileHeights, final List<SobekProfileWarning> profileWarnings )
  {
    /* Create a string builder. */
    final StringBuilder line = new StringBuilder();

    /* Build the table. */
    line.append( String.format( Locale.PRC, "TBLE%n" ) ); //$NON-NLS-1$

    /* Iterate through the sobek profile heights. */
    for( int i = 0; i < profileHeights.size(); i++ )
    {
      /* Get the elements. */
      final SobekProfileHeight profileHeight = profileHeights.get( i );
      final SobekProfileWarning profileWarning = profileWarnings.get( i );

      /* Get the values. */
      final BigDecimal height = profileHeight.getHeight();
      final BigDecimal fullWidth = profileHeight.getFullWidth();
      final BigDecimal flowWidth = profileHeight.getFlowWidth();
      final String warning = profileWarning.serialize();

      /* Add the line. */
      line.append( String.format( Locale.PRC, "%.2f %.2f %.2f <%s%n", height, fullWidth, flowWidth, warning ) ); //$NON-NLS-1$
    }

    /* Finish the table. */
    line.append( String.format( Locale.PRC, "tble%n" ) ); //$NON-NLS-1$

    return line.toString();
  }

  private String serializeDikes( final int dk, final BigDecimal dc, final BigDecimal db, final BigDecimal df, final BigDecimal dt )
  {
    return String.format( Locale.PRC, "dk %d dc %.2f db %.2f df %.2f dt %.2f%n", dk, dc, db, df, dt ); //$NON-NLS-1$
  }
}