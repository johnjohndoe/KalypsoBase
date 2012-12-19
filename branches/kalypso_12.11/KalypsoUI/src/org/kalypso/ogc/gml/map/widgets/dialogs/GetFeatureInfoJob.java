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
package org.kalypso.ogc.gml.map.widgets.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * @author Holger Albert
 */
public class GetFeatureInfoJob extends Job
{
  /**
   * The WMS theme.
   */
  private final KalypsoWMSTheme m_wmsTheme;

  /**
   * The x coordinate.
   */
  private final double m_x;

  /**
   * The y coordinate.
   */
  private final double m_y;

  /**
   * The feature info. May be null.
   */
  private String m_featureInfo;

  /**
   * The constructor.
   * 
   * @param wmsTheme
   *          The WMS theme.
   * @param x
   *          The x coordinate.
   * @param y
   *          The y coordinate.
   */
  public GetFeatureInfoJob( final KalypsoWMSTheme wmsTheme, final double x, final double y )
  {
    super( "GetFeatureInfoJob" ); //$NON-NLS-1$

    m_wmsTheme = wmsTheme;
    m_x = x;
    m_y = y;
    m_featureInfo = null;
  }

  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    try
    {
      m_featureInfo = m_wmsTheme.getFeatureInfo( m_x, m_y );

      return new Status( IStatus.OK, KalypsoGisPlugin.PLUGIN_ID, org.kalypso.ui.internal.i18n.Messages.getString( "GetFeatureInfoJob_1" ) ); //$NON-NLS-1$
    }
    catch( final Exception ex )
    {
      m_featureInfo = null;

      return new Status( IStatus.ERROR, KalypsoGisPlugin.PLUGIN_ID, ex.getLocalizedMessage(), ex );
    }
  }

  /**
   * This function returns the feature info. May be null.
   * 
   * @return The feature info. May be null.
   */
  public String getFeatureInfo( )
  {
    return m_featureInfo;
  }
}