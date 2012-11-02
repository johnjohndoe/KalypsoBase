/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.ui.view.chart.handler;

import org.eclipse.core.expressions.PropertyTester;
import org.kalypso.model.wspm.ui.dialog.compare.ProfileChartComposite;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Holger Albert
 */
public class SwitchProfilePropertyTester extends PropertyTester
{
  public SwitchProfilePropertyTester( )
  {
  }

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( !(receiver instanceof ProfileChartComposite) )
      return false;

    if( !"direction".equals( property ) ) //$NON-NLS-1$
      return false;

    if( "previous".equals( expectedValue ) ) //$NON-NLS-1$
      return isEnabled( (ProfileChartComposite)receiver, -1 );

    if( "next".equals( expectedValue ) ) //$NON-NLS-1$
      return isEnabled( (ProfileChartComposite)receiver, 1 );

    return false;
  }

  private boolean isEnabled( final ProfileChartComposite chart, final int differencePositions )
  {
    /* Get the feature of the chart. */
    final Feature chartFeature = SwitchProfileHandler.getChartFeature( chart );
    if( chartFeature == null )
      return false;

    /* Get the workspace of the feature of the chart. */
    /* No workspace means, the feature of the chart is not selected in the global selection manager. */
    final CommandableWorkspace chartWorkspace = SwitchProfileHandler.getChartWorkspace( chartFeature );
    if( chartWorkspace == null )
      return false;

    /* Get the new feature to switch to. */
    final Feature newFeature = SwitchProfileHandler.getNewFeature( chartFeature, differencePositions );
    if( newFeature == null )
      return false;

    return true;
  }
}