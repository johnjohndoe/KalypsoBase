/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.swtchart.chart.legend;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.configuration.ChartLoader;
import org.kalypso.swtchart.exception.ConfigChartNotFoundException;
import org.ksp.chart.viewerconfiguration.ConfigurationType;

/**
 * @author alibu
 */
public class LegendImageContainer
{

  Image m_img = null;

  /**
   * Erzeugt eine Legende zu einem ChartNamen und einer Config-Datei Im momement geschieht das Erzeugen etwas
   * umständlich: Chart erzeugen, dann Layer geben lassen und in Legende füllen; ohne Chart kommt man aber nicht an die
   * Layer ran: LayerProvider verlangt in der Init-Methode nach einem Chart Das Objekt muss nach der Verwendung diposed
   * werden
   */
  public LegendImageContainer( ConfigurationType config, String chartname, Device dev ) throws ConfigChartNotFoundException
  {
    Shell shell = new Shell();
    shell.setLayout( new FillLayout() );
    Legend legend = new Legend( shell, SWT.NONE );
    Chart chart = new Chart( shell, SWT.NONE );
    // Chart füllen
    ChartLoader.createChart( chart, config, chartname, null );
    // Layer umfüllen
    legend.addLayers( chart.getLayers() );
    // Größe berechnen lassen
    Point size = legend.computeSize( 0, 0 );
    int height = size.x;
    int width = size.y;
    if( width > 0 && height > 0 )
    {
      m_img = new Image( dev, size.x, size.y );
      // Legende zeichnen
      legend.drawImage( m_img, dev );
    }
    legend.dispose();
    shell.dispose();
  }

  /**
   * Wenn man schon ein fertiges Chart hat, kann man auch leichter konstruieren
   */
  public LegendImageContainer( Chart chart, Device dev )
  {
    Shell shell = new Shell();
    shell.setLayout( new FillLayout() );
    Legend legend = new Legend( shell, SWT.NONE );
    // Layer umfüllen
    legend.addLayers( chart.getLayers() );
    // Größe berechnen lassen
    Point size = legend.computeSize( 0, 0 );
    m_img = new Image( dev, size.x, size.y );
    // Legende zeichnen
    legend.drawImage( m_img, Display.getDefault() );
    legend.dispose();
    shell.dispose();
  }

  /**
   * Gibt das erzeugte Bild zurück
   */
  public Image getImage( )
  {
    return m_img;
  }

  public void dispose( )
  {
    if( m_img != null )
      m_img.dispose();
    m_img = null;
  }
}
