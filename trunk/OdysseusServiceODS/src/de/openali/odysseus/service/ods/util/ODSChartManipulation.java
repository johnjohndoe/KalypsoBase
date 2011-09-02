package de.openali.odysseus.service.ods.util;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.service.OGCRequest;
import org.kalypso.ogc.core.utils.OWSUtilities;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;

/**
 * Helper class for chart stuff concerning request parameters.
 * 
 * @author Alexander Burtscher
 */
public class ODSChartManipulation
{
  public static void manipulateChart( IChartModel model, OGCRequest request ) throws OWSException
  {
    setLayerVisibility( model.getLayerManager(), request );
    setAxesRanges( model.getMapperRegistry(), request );
  }

  /**
   * sets the layers visibility and position from the request parameter LAYERS - if the parameter is not empty, all
   * those layers which are not mentioned in the comma separated list are set invisible
   */
  private static void setLayerVisibility( ILayerManager manager, OGCRequest request )
  {
    String reqLayerString = request.getParameterValue( "LAYERS" );
    String[] reqLayers = null;
    if( (reqLayerString != null) && !reqLayerString.trim().equals( "" ) )
      reqLayers = reqLayerString.trim().split( "," );

    /* Herausfinden, welche von den angeforderten Layers wirklich existieren. */
    List<String> realReqLayers = new ArrayList<String>();
    if( reqLayers != null )
      for( String layerId : reqLayers )
        if( manager.findLayer( layerId ) != null )
          realReqLayers.add( layerId );
    if( (realReqLayers != null) && (realReqLayers.size() > 0) )
    {
      for( int i = 0; i < realReqLayers.size(); i++ )
      {
        IChartLayer layer = manager.findLayer( realReqLayers.get( i ) );
        if( layer != null )
        {
          manager.moveLayerToPosition( layer, i );
          layer.setVisible( true );
        }
      }
      IChartLayer[] layers = manager.getLayers();
      for( int i = realReqLayers.size(); i < layers.length; i++ )
      {
        IChartLayer layer = layers[i];
        layer.setVisible( false );
      }

    }

  }

  private static void setAxesRanges( IMapperRegistry ar, OGCRequest request ) throws OWSException
  {
    setAxesRanges( ar, request.getParameterValue( "AXES" ), request.getParameterValue( "AXES_MIN" ), request.getParameterValue( "AXES_MAX" ) );
  }

  private static void setAxesRanges( IMapperRegistry mr, String axesString, String minsString, String maxsString ) throws OWSException
  {
    /* Falls der AXES-Parameter nicht angegeben ist, dann wird einfach abgebrochen. */
    if( axesString == null )
      return;

    /* Zunächst AXISMIN- und AXISMAX-Parameter parsen und zu Min/MaxMap hinzufügen. */
    String[] axesStrArray = axesString.split( "," );
    String[] minStrArray = minsString.split( "," );
    String[] maxStrArray = maxsString.split( "," );

    if( (axesStrArray.length != minStrArray.length) || (axesStrArray.length != maxStrArray.length) )
      throw new OWSException( "AXES, AXES_MIN and AXES_MAX must contain equal number of values", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

    for( int i = 0; i < axesStrArray.length; i++ )
    {
      IAxis iaxis = mr.getAxis( axesStrArray[i] );
      if( iaxis != null )
      {
        String maxString = maxStrArray[i];
        String minString = minStrArray[i];

        setAxisRange2( iaxis, iaxis.getDataClass(), maxString, minString );
      }
    }
  }

  /**
   * sets range for an individual axis; set to public so an axis can be re-ranged widthout the need for a complete
   * chartmodel (e.g. by GetAxesInfo, GetAxis)
   * 
   * @param iaxis
   *          the axis whose range to set
   * @param clazz
   *          describes the class the range shall be mapped to
   * @param minString
   *          axis min value as string
   * @param maxString
   *          axis max value as string
   */
  public static <T> void setAxisRange2( IAxis iaxis, Class<T> clazz, String maxString, String minString ) throws OWSException
  {
    IDataOperator<T> da = iaxis.getDataOperator( clazz );
    Number min = null;
    Number max = null;

    try
    {
      min = da.logicalToNumeric( da.stringToLogical( minString ) );
    }
    catch( MalformedValueException e )
    {
      throw new OWSException( "Value '" + minString + "' is not appropriate for Axis '" + iaxis.getIdentifier() + "'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );
    }

    try
    {
      max = da.logicalToNumeric( da.stringToLogical( maxString ) );
    }
    catch( MalformedValueException e )
    {
      throw new OWSException( "Value '" + maxString + "' is not appropriate for Axis '" + iaxis.getIdentifier() + "'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );
    }

    DataRange<Number> dr = new DataRange<Number>( min, max );
    iaxis.setNumericRange( dr );
  }
}