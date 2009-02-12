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
package org.kalypso.swtchart.configuration;

import java.net.URL;

import org.kalypso.swtchart.exception.ConfigTableNotFoundException;
import org.kalypso.swtchart.table.ITableProvider;
import org.kalypso.swtchart.table.KalypsoTableExtensions;
import org.ksp.chart.configuration.ObjectFactory;
import org.ksp.chart.configuration.TableType;


/**
 * @author burtscher
 *
 * reads an observation and transforms its values into an html table
 */
public class TableLoader
{
  public final static ObjectFactory OF = new ObjectFactory();

  /**
   * checks if a tableType-object named tableName is found in the ConfigurationType. If so, it
   * tries to load the data, creates an html table and returns it as string. Otherwise, it throws a
   * ConfigTableNotFound Exception
   */
  public static String createTable( ConfigLoader cl, String configTableName, final URL context ) throws ConfigTableNotFoundException
//  public static String createTable( ConfigurationType config, String tableName ) throws ConfigTableNotFoundException
  {
    TableType configTable = null;
    String tableString="";

    // ChartConfig auslesen
    if( cl != null )
    {
      configTable = cl.getTables().get( configTableName );
    }
    if( configTable == null )
    {
      throw new ConfigTableNotFoundException( configTableName );
    }
    else
    {
        String tableProviderId=configTable.getProvider();
        try
        {
          String htmltable="";
          ITableProvider provider = KalypsoTableExtensions.createProvider( tableProviderId );
          if (provider!=null)
          {
            provider.init( configTable, context );
            htmltable = provider.getTable().showTable();
          }
          return htmltable;
        }
        catch( Exception e )
        {
          e.printStackTrace();
        }
      }
    return tableString;
  }



}
