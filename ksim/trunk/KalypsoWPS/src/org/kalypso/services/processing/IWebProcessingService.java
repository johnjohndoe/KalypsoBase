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
package org.kalypso.services.processing;

import org.kalypso.services.IOGCWebService;

import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ProcessDescriptions;

/**
 * The specified Web Processing Service (WPS) provides client access to pre-programmed calculations and/or computation
 * models that operate on spatially referenced data. The WPS interface specifies three operations that can be requested
 * by a client and performed by a WPS server, all mandatory implementation by all servers. Those operations are: a)
 * <code>GetCapabilities</code> – This operation allows a client to request and receive back service metadata (or
 * <code>Capabilities</code>) documents that describe the abilities of the specific server implementation. This
 * operation also supports negotiation of the specification version being used for client-server interactions. b)
 * <code>DescribeProcess</code> – This operation allows a client to request and receive back detailed information
 * about one or more process(es) that can be executed by an <code>Execute</code> operation, including the input
 * parameters and formats, and the outputs. c) <code>Execute</code> – This operation allows a client to run a
 * specified process implemented by the WPS, using provided input parameter values and returning the outputs produced.
 * (as specified in OGC Web Processing Service Specification Version 0.4.0 [OGC 05-007r4])
 * 
 * @author skurzbach
 */
public interface IWebProcessingService extends IOGCWebService
{
  /**
   * The mandatory <code>DescribeProcess</code> operation allows WPS clients to request and receive back a full
   * description of one or more processes that can be executed by the <code>Execute</code> operation. The normal
   * response to a valid <code>DescribeProcess</code> operation request shall be a <code>ProcessDescriptions</code>
   * data structure, which contains one or more Process Descriptions for the requested process identifiers. Each Process
   * Description includes the brief information returned in the <code>ProcessOfferings</code> section of the service
   * metadata (<code>Capabilities</code>) document, plus descriptions of the input and output parameters. Each
   * process can have any number of input and output parameters.
   */
  public ProcessDescriptions describeProcess( final DescribeProcess describeProcessRequest );

  /**
   * The mandatory <code>Execute</code> operation allows WPS clients to run a specified process implemented by a
   * server, using input parameter values provided and returning the output value(s) produced. The response to an
   * <code>Execute</code> operation request depends on the value of the <code>store</code> parameter, the number of
   * outputs produced by the process, and the type of output when a single output produced. In the simplest case, when
   * the <code>store</code> parameter is <code>false</code>, process execution is successful, only one output is
   * produced, and that output is a <code>ComplexValueType</code>, then the <code>Execute</code> operation response
   * is that one complex output, from the process directly to the client. In all other cases, the response to a valid
   * <code>Execute</code> operation request is an <code>ExecuteResponseType</code> data structure. The contents of
   * this <code>ExecuteResponseType</code> depend on the value of the <code>store</code> parameter and the forms of
   * the output(s). The <code>ExecuteResponseType</code> can be returned after process execution is completed.
   * Alternately, the <code>ExecuteResponseType</code> can be returned immediately following acceptance by the server
   * of this process execution. In that case, the <code>ExecuteResponseType</code> includes information about the
   * status of the process, which indicates whether or not the process has completed, as well as a status URL. The
   * status URL must return an updated XML-encoded <code>ExecuteResponseType</code>. This status URL allows the
   * client to poll the server if the process takes a substantial amount of time to execute. The updated
   * <code>ExecuteResponseType</code> indicates the completion status of the process and a measure of the amount of
   * processing remaining if the process is not complete.
   */
  public Object execute( final Execute executeRequest );
}
