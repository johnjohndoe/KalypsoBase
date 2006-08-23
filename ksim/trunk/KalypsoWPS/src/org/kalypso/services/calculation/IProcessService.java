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
package org.kalypso.services.calculation;

import net.opengeospatial.ows.GetCapabilitiesType;
import net.opengeospatial.wps.Capabilities;
import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.ProcessDescriptions;

import org.kalypso.services.IKalypsoService;

/**
 * The IProcessService executes requests for calculations.
 * 
 * It provides methods for querying the capabilities of the service and
 * delivering detailed service descriptions.
 * 
 * @author skurzbach
 */
public interface IProcessService extends IKalypsoService
{
	/**
	 * Gives metadata about the service and the processes it can execute. Result
	 * object contains the information specified in the request.
	 */
	public Capabilities getCapabilities(
			final GetCapabilitiesType getCapabilitiesRequest);

	/**
	 * Get detailed description of one or more processes that can be executed.
	 */
	public ProcessDescriptions describeProcess(
			final DescribeProcess describeProcessRequest);

	/**
	 * Executes the process specified in the request and return a response
	 * object that contains information about the status of the process.
	 */
	public ExecuteResponseType execute(final Execute executeRequest);
}
