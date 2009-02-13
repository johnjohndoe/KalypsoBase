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
package org.kalypso.simulation.ui;

/**
 * @author belger
 */
public interface IKalypsoSimulationUIConstants
{
  public static final String PROGNOSE_PERSPECTIVE = "org.kalypso.simulation.ui.startscreen.PrognosePerspective";

  public static final String ID_PROGNOSE_VIEW = "org.kalypso.view.prognose";

  public static final String ID_SIMULATION_ACTIONSET = "org.kalypso.simulation.ui.actionSet";

  /**
   * Config.ini key to configure, if models should be synchronized on start of prognoses.
   * <p>
   * Allowed values are 'true' and 'false'.
   * </p>
   * <p>
   * Default value is: 'true'
   * </p>
   */
  public static final String CONFIG_DO_SYNCHRONIZE_MODELS = "kalypso.prognose.synchronize-models";
}
