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
package org.kalypso.simulation.core.refactoring;

import java.net.URL;

import org.kalypso.simulation.core.refactoring.local.LocalSimulationRunner;
import org.kalypso.simulation.core.simspec.Modeldata;


/**
 * @author kuch
 *
 */
public class SimulationRunnerFactory
{
  
  /**
   * FIXME refactoring
   * 
   * <pre>
   * 
   * final ISimulationRunner runner = SimulationRunnerFacotry.createRunner( typeID );
   * runner.getSpec();
   * 
   * final String typeID = modeldata.getTypeID();
   * 
   * // ‹bersetzung modeldata -&gt; hashmap
   * // - Ableich modelspec/modeldata
   * 
   * // modelspec -&gt; Map&lt;String, Object&gt;
   * // - Literal: String, Double, Integer
   * // - ComplexValueType: Feature/Image
   * // - ComplexReferenceType: URL/URI
   * 
   * final IStatus status = runner.run( Map &lt; String, Object &gt; inputs, List &lt; String &gt; outputs, progress );
   * 
   * </pre>
   */

  public static ISimulationRunner createRunner( final String calculationTypeId, final Modeldata modeldata, final URL inputDir )
  {
    // FIXME atm only local simulation runner will be returned...
    return new LocalSimulationRunner( calculationTypeId, modeldata, inputDir );
  }

 
}
