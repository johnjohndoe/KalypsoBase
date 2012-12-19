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
package de.openali.odysseus.service.ods.environment;

import java.io.File;

import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;

/**
 * The ods chart config.
 * 
 * @author Holge Albert
 */
public class ODSChartConfig
{
  /**
   * The chart file.
   */
  private final File m_chartFile;

  /**
   * The chart config doc.
   */
  private final ChartConfigurationDocument m_chartConfigDoc;

  /**
   * The constructor.
   * 
   * @param chartFile
   *          The chart file.
   * @param chartConfigDoc
   *          The chart config doc.
   */
  public ODSChartConfig( final File chartFile, final ChartConfigurationDocument chartConfigDoc )
  {
    m_chartFile = chartFile;
    m_chartConfigDoc = chartConfigDoc;
  }

  /**
   * This function returns the chart file.
   * 
   * @return The chart file.
   */
  public File getChartFile( )
  {
    return m_chartFile;
  }

  /**
   * This function returns the chart config doc.
   * 
   * @return The chart config doc.
   */
  public ChartConfigurationDocument getChartConfigDoc( )
  {
    return m_chartConfigDoc;
  }
}