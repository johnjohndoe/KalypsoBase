/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.wizards.calculation;

import java.net.URL;
import java.util.ArrayList;

import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.util.PropertiesHelper;
import org.kalypso.ogc.sensor.template.ObsView;
import org.kalypso.ogc.sensor.timeseries.TimeserieFeatureProps;
import org.kalypso.simulation.ui.wizards.calculation.modelpages.ObservationMapTableDiagWizardPage;

/**
 * Provides some convenience methods for dealing with the stuff in Kalypso Wizards.
 * 
 * @author Schlienger
 */
public class CalcWizardHelper
{
  private CalcWizardHelper( )
  {
    // not to be instantiated
  }

  /**
   * Parses the properties by fetching all timeserieX elements out (X being whatever, as long as different from element
   * to element). A TimeserieFeatureProps is created for each of these elements.
   * 
   * <pre>
   *  &lt;arg name=&quot;timeserie1&quot; value=&quot;type=...#typeName=...#nameColumn=...#linkColumn=...&quot;/&gt;
   *  &lt;arg name=&quot;timeserie2&quot; value=&quot;type=...#typeName=...#nameColumn=...#linkColumn=...&quot;/&gt;
   * </pre>
   * 
   * @return array of TimeserieFeatureProps
   */
  public static TimeserieFeatureProps[] parseTimeserieFeatureProps( final Arguments props )
  {
    final ArrayList<TimeserieFeatureProps> l = new ArrayList<TimeserieFeatureProps>();

    for( final String pName : props.keySet() )
    {
      if( pName.startsWith( ObservationMapTableDiagWizardPage.PROP_TIMEPROPNAME ) )
        l.add( new TimeserieFeatureProps( PropertiesHelper.parseFromString( (String) props.get( pName ), '#' ) ) );
    }

    return l.toArray( new TimeserieFeatureProps[0] );
  }

  /**
   * Updates the diagram template for the given TimeserieFeatureProps and features
   */
  public static void updateZMLView( final ObsView view, final TSLinkWithName[] links, final URL context, final boolean ignoreExceptions, final String[] ignoreTypes )
  {
    view.removeAllItems();

    view.setIgnoreTypes( ignoreTypes );

    for( final TSLinkWithName link : links )
      view.loadObservation( context, link.href, ignoreExceptions, link.name, new ObsView.ItemData( true, link.color, link.stroke ) );
  }
}