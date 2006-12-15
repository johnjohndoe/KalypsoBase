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
package org.kalypso.services.ods.helper;

import javax.xml.bind.JAXBException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.configuration.ChartLoader;
import org.kalypso.swtchart.configuration.ConfigurationLoader;
import org.kalypso.swtchart.exception.ConfigChartNotFoundException;
import org.kalypso.swtchart.logging.Logger;

/**
 * @author burtscher1
 *
 * this class contains all objects (e.g. shell) needed to keep a chart in the cache
 */
public class CachedChart
{

    private Shell m_shell;
    private Chart m_chart;
    private boolean m_isDisposed=false;
    private String m_id;


    public CachedChart(String id)
    {
      Display d= DisplayHelper.getInstance().getDisplay();
      m_shell=new Shell(d);
      m_chart=new Chart( m_shell, SWT.NONE );

      try
      {
        String path=System.getProperty( IODSConstants.ODS_CONFIG_PATH_KEY, IODSConstants.ODS_CONFIG_PATH_DEFAULT)+IODSConstants.ODS_CONFIG_NAME;
        ConfigurationLoader cl=new ConfigurationLoader(path);
        Logger.trace("Config-Pfad: "+path);
        ChartLoader.createChart( m_chart, cl.getConfiguration(), id, null );
      }
      catch( ConfigChartNotFoundException e )
      {
        Logger.trace(e.getMessage());
        e.printStackTrace();
      }
      catch( JAXBException e )
      {
        e.printStackTrace();
      }

    }



    public CachedChart(String id, Chart chart, Shell shell)
    {
      m_id=id;
      m_shell=shell;
      m_chart=chart;
    }


    public void dispose()
    {
      if (m_shell!=null && !m_shell.isDisposed())
        m_shell.dispose();
      if (m_chart!=null && !m_chart.isDisposed())
        m_chart.dispose();
      m_isDisposed=true;
    }

    public boolean isDisposed()
    {
      return m_isDisposed;
    }

    public Chart getChart()
    {
      return m_chart;
    }

}
