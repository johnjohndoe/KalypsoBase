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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;

/**
 * This class containes data of a tabulated sobek profile, which will be stored in the file 'profile.def'.
 * 
 * @author Holger Albert
 */
public class SobekProfileDef
{
  /**
   * The id of the cross section definition.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * This id is referenced from a line in the file 'profile.dat'.
   */
  private String m_id;

  /**
   * The name of the cross section definition.
   */
  private String m_nm;

  /**
   * The type of the cross section (0=table).
   */
  private int m_ty;

  /**
   * The width of the main channel.
   */
  private BigDecimal m_wm;

  /**
   * The width of the floodplain 1 (used in River profile only, else value = 0).
   */
  private BigDecimal m_w1;

  /**
   * The width of the floodplain 2 (used in River profile only, else value = 0).
   */
  private BigDecimal m_w2;

  /**
   * The sediment transport width (not in SOBEK Urban/Rural). Default 0. Only important for module sediment/morfology.
   */
  private BigDecimal m_sw;

  /**
   * The data for the heights of a tabulated sobek profile.
   */
  private List<SobekProfileHeight> m_profileHeights;

  /**
   * Summer dike (1 = active, 0 = not active) (in River profile only).
   */
  private int m_dk;

  /**
   * The dike crest level (in River profile only).
   */
  private BigDecimal m_dc;

  /**
   * The floodplain base level behind the dike (in River profile only).
   */
  private BigDecimal m_db;

  /**
   * The flow area behind the dike (in River profile only).
   */
  private BigDecimal m_df;

  /**
   * The total area behind the dike (in River profile only).
   */
  private BigDecimal m_dt;

  /**
   * The ground layer depth (meter relative to bed level).
   */
  private BigDecimal m_gl;

  /**
   * The ground layer to be used within hydraulics calculation (1) or not (0).
   */
  private int m_gu;

  /**
   * The constructor.
   * 
   * @param id
   *          The id of the cross section definition. <strong>NOTE:</strong> This id is referenced from a line in the
   *          file profile.dat.
   * @param nm
   *          The name of the cross section definition.
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
  public SobekProfileDef( String id, String nm, BigDecimal wm, BigDecimal w1, BigDecimal w2, BigDecimal sw, List<SobekProfileHeight> profileHeights, int dk, BigDecimal dc, BigDecimal db, BigDecimal df, BigDecimal dt, BigDecimal gl, int gu )
  {
    m_id = id;
    m_nm = nm;
    m_ty = 0;
    m_wm = wm;
    m_w1 = w1;
    m_w2 = w2;
    m_sw = sw;
    m_profileHeights = profileHeights;
    m_dk = dk;
    m_dc = dc;
    m_db = db;
    m_df = df;
    m_dt = dt;
    m_gl = gl;
    m_gu = gu;
  }

  /**
   * This function returns the id of the cross section definition.<br>
   * <br>
   * <strong>NOTE:</strong><br>
   * This id is referenced from a line in the file profile.dat.
   * 
   * @return The id of the cross section definition.
   */
  public String getId( )
  {
    return m_id;
  }

  /**
   * This function returns the name of the cross section definition.
   * 
   * @return The name of the cross section definition.
   */
  public String getNm( )
  {
    return m_nm;
  }

  /**
   * This function returns the type of the cross section (0=table).
   * 
   * @return The type of the cross section (0=table).
   */
  public int getTy( )
  {
    return m_ty;
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
  public List<SobekProfileHeight> getProfileHeights( )
  {
    return m_profileHeights;
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

  /**
   * This function validates the contained data.
   * 
   * @return A status.
   */
  public IStatus validate( )
  {
    if( m_id == null || m_id.length() == 0 )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "The id of the cross section definition is mandatory..." );

    if( m_nm == null || m_nm.length() == 0 )
      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), "The name of the cross section definition is mandatory..." );

    return new Status( IStatus.OK, KalypsoModelWspmCorePlugin.getID(), "OK" );
  }

  /**
   * This function serializes the data for the file 'profile.def'.
   * 
   * @return The data for the file 'profile.def'.
   */
  public String serialize( )
  {
    /* Create warnings for each sobek profile height. */
    List<SobekProfileWarning> profileWarnings = new ArrayList<SobekProfileWarning>();

    /* Check all heights, full widths and flow widths. */
    double lastHeight = -1.0;
    double lastFullWidth = -1.0;
    double lastFlowWidth = -1.0;
    for( int i = 0; i < m_profileHeights.size(); i++ )
    {
      /* Get the sobek profile height. */
      SobekProfileHeight profileHeight = m_profileHeights.get( i );

      /* Create a sobek profile warning. */
      /* It will be empty. */
      SobekProfileWarning profileWarning = new SobekProfileWarning();

      /* Get the values. */
      double height = profileHeight.getHeight().doubleValue();
      double fullWidth = profileHeight.getFullWidth().doubleValue();
      double flowWidth = profileHeight.getFlowWidth().doubleValue();

      /* Create a warning. */
      if( height < lastHeight || fullWidth < lastFullWidth || flowWidth < lastFlowWidth )
        profileWarning.addWarning( String.format( Locale.PRC, "Check current line with previous line: %f>=%f %f>=%f %f>=%f", height, lastHeight, fullWidth, lastFullWidth, flowWidth, lastFlowWidth ) );

      /* Create a warning. */
      if( fullWidth < flowWidth )
        profileWarning.addWarning( String.format( Locale.PRC, "Flow width (%f) is larger than full width (%f)", flowWidth, fullWidth ) );

      /* Store the warning. */
      profileWarnings.add( profileWarning );

      /* The current values will be the used for comparison in the next loop. */
      lastHeight = height;
      lastFullWidth = fullWidth;
      lastFlowWidth = flowWidth;
    }

    BigDecimal w2 = m_w2;
    double width = m_wm.doubleValue() + m_w1.doubleValue() + m_w2.doubleValue();
    double diff = width - lastFlowWidth;
    if( Math.abs( diff ) > 0.0001 )
    {
      /* Adjust the width of the flood plain 2. */
      w2 = new BigDecimal( lastFlowWidth - (m_wm.doubleValue() + m_w1.doubleValue()) );

      /* Add a warning in the last sobek profile warning. */
      SobekProfileWarning profileWarning = profileWarnings.get( profileWarnings.size() - 1 );
      profileWarning.addWarning( String.format( Locale.PRC, "Adjusted w2 (now %f) because: wm + w1 + w2 (%f) != flow width (%f)", w2, width, lastFlowWidth ) );
    }

    /* Create a string builder. */
    StringBuilder line = new StringBuilder();

    /* Serialize. */
    line.append( serializeCrdsStart( m_id, m_nm, m_ty, m_wm, m_w1, w2, m_sw, m_gl, m_gu ) );
    line.append( serializeTable( m_profileHeights, profileWarnings ) );
    line.append( serializeDikes( m_dk, m_dc, m_db, m_df, m_dt ) );
    line.append( serializeCrdsEnd() );

    return line.toString();
  }

  private String serializeCrdsStart( String id, String nm, int ty, BigDecimal wm, BigDecimal w1, BigDecimal w2, BigDecimal sw, BigDecimal gl, int gu )
  {
    return String.format( Locale.PRC, "CRDS id '%s' nm '%s' ty %d wm %.2f w1 %.2f w2 %.2f sw %.2f gl %.2f gu %d lt lw%n", id, nm, ty, wm, w1, w2, sw, gl, gu );
  }

  private String serializeTable( List<SobekProfileHeight> profileHeights, List<SobekProfileWarning> profileWarnings )
  {
    /* Create a string builder. */
    StringBuilder line = new StringBuilder();

    /* Build the table. */
    line.append( String.format( Locale.PRC, "TBLE%n" ) );

    /* Iterate through the sobek profile heights. */
    for( int i = 0; i < profileHeights.size(); i++ )
    {
      /* Get the elements. */
      SobekProfileHeight profileHeight = profileHeights.get( i );
      SobekProfileWarning profileWarning = profileWarnings.get( i );

      /* Get the values. */
      BigDecimal height = profileHeight.getHeight();
      BigDecimal fullWidth = profileHeight.getFullWidth();
      BigDecimal flowWidth = profileHeight.getFlowWidth();
      String warning = profileWarning.serialize();

      /* Add the line. */
      line.append( String.format( Locale.PRC, "%.2f %.2f %.2f <%s%n", height, fullWidth, flowWidth, warning ) );
    }

    /* Finish the table. */
    line.append( String.format( Locale.PRC, "tble%n" ) );

    return line.toString();
  }

  private String serializeDikes( int dk, BigDecimal dc, BigDecimal db, BigDecimal df, BigDecimal dt )
  {
    return String.format( Locale.PRC, "dk %d dc %.2f db %.2f df %.2f dt %.2f%n", dk, dc, db, df, dt );
  }

  private String serializeCrdsEnd( )
  {
    return String.format( Locale.PRC, "crds" );
  }
}