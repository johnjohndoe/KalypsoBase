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
package de.openali.odysseus.service.ods.operation;

import java.io.FileNotFoundException;

import org.eclipse.swt.graphics.ImageData;
import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.service.OGCRequest;
import org.kalypso.ogc.core.utils.OWSUtilities;

import de.openali.odysseus.service.ods.util.ImageOutput;
import de.openali.odysseus.service.ods.util.ODSUtils;

/**
 * @author burtscher1
 */
public class GetSymbol extends AbstractODSOperation
{
  /**
   * @see de.openali.odysseus.service.ods.operation.AbstractODSOperation#execute()
   */
  @Override
  public void execute( ) throws OWSException
  {
    // TODO: Validate scene, chart and layer parameter
    final OGCRequest req = getRequest();

    // use default scene if no parameter value has been assigned
    String sceneId = req.getParameterValue( "SCENE" );
    if( sceneId == null || "".equals( sceneId ) )
      sceneId = getEnv().getDefaultSceneId();
    if( sceneId == null )
      throw new OWSException( "Missing parameter 'SCENE'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.MISSING_PARAMETER_VALUE, null );

    if( !getEnv().validateSceneId( sceneId ) )
      throw new OWSException( "Scene '" + sceneId + "' is not available", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

    final String chartId = req.getParameterValue( "CHART" );
    if( chartId == null )
      throw new OWSException( "Missing parameter 'CHART'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.MISSING_PARAMETER_VALUE, null );

    if( !getEnv().validateChartId( sceneId, chartId ) )
      throw new OWSException( "Chart '" + chartId + "' is not available", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

    final String layerId = req.getParameterValue( "LAYER" );
    if( layerId == null )
      throw new OWSException( "Missing parameter 'LAYER'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.MISSING_PARAMETER_VALUE, null );

    if( !getEnv().validateLayerId( sceneId, chartId, layerId ) )
      throw new OWSException( "Layer '" + layerId + "' is not available", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

    final String symbolId = req.getParameterValue( "SYMBOL" );
    if( symbolId == null || "".equals( symbolId.trim() ) )
      throw new OWSException( "Missing parameter 'SYMBOL'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.MISSING_PARAMETER_VALUE, null );

    ImageData id;
    try
    {
      id = ODSUtils.loadSymbol( getEnv().getTmpDir(), sceneId, chartId, layerId, symbolId );
    }
    catch( final FileNotFoundException e )
    {
      throw new OWSException( "Symbol '" + symbolId + "' is not available", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );
    }

    ImageOutput.imageResponse( req, getResponse(), id );
  }
}