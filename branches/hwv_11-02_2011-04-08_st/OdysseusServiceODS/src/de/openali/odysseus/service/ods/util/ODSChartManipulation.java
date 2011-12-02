package de.openali.odysseus.service.ods.util;

import java.util.ArrayList;
import java.util.List;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.service.ows.exception.OWSException;
import de.openali.odysseus.service.ows.exception.OWSException.ExceptionCode;
import de.openali.odysseus.service.ows.request.RequestBean;

/**
 * @author alibu Helper class for chart stuff concerning request parameters
 */
public class ODSChartManipulation
{

  public static void manipulateChart( final IChartModel model, final RequestBean request ) throws OWSException
  {
    setLayerVisibility( model.getLayerManager(), request );
    setAxesRanges( model.getMapperRegistry(), request );
  }

  /**
   * sets the layers visibility and position from the request parameter LAYERS - if the parameter is not empty, all
   * those layers which are not mentioned in the comma separated list are set invisible
   */
  private static void setLayerVisibility( final ILayerManager manager, final RequestBean request )
  {
    final String reqLayerString = request.getParameterValue( "LAYERS" );
    String[] reqLayers = null;
    if( (reqLayerString != null) && !reqLayerString.trim().equals( "" ) )
      reqLayers = reqLayerString.trim().split( "," );
    // herausfinden, welche von den angeforderten Layers wirklich existieren

    final List<String> realReqLayers = new ArrayList<String>();
    if( reqLayers != null )
      for( final String layerId : reqLayers )
        if( manager.findLayer( layerId ) != null )
          realReqLayers.add( layerId );
    if( (realReqLayers != null) && (realReqLayers.size() > 0) )
    {
      for( int i = 0; i < realReqLayers.size(); i++ )
      {
        final IChartLayer layer = manager.findLayer( realReqLayers.get( i ) );
        if( layer != null )
        {
          manager.moveLayerToPosition( layer, i );
          layer.setVisible( true );
        }
      }
      final IChartLayer[] layers = manager.getLayers();
      for( int i = realReqLayers.size(); i < layers.length; i++ )
      {
        final IChartLayer layer = layers[i];
        layer.setVisible( false );
      }

    }

  }

  private static void setAxesRanges( final IMapperRegistry ar, final RequestBean request ) throws OWSException
  {
    setAxesRanges( ar, request.getParameterValue( "AXES" ), request.getParameterValue( "AXES_MIN" ), request.getParameterValue( "AXES_MAX" ) );
  }

  private static void setAxesRanges( final IMapperRegistry mr, final String axesString, final String minsString, final String maxsString ) throws OWSException
  {
    // falls der AXES-Parameter nicht angegeben ist, dann wird einfach
    // abgebrochen
    if( axesString == null )
      return;

    /*
     * Zunächst AXISMIN- und AXISMAX-Parameter parsen und zu Min/MaxMap hinzufügen
     */

    final String[] axesStrArray = axesString.split( "," );
    final String[] minStrArray = minsString.split( "," );
    final String[] maxStrArray = maxsString.split( "," );

    if( (axesStrArray.length != minStrArray.length) || (axesStrArray.length != maxStrArray.length) )
      throw new OWSException( ExceptionCode.INVALID_PARAMETER_VALUE, "AXES, AXES_MIN and AXES_MAX must contain equal number of values", null );

    for( int i = 0; i < axesStrArray.length; i++ )
    {
      final IAxis iaxis = mr.getAxis( axesStrArray[i] );
      if( iaxis != null )
      {
        final String maxString = maxStrArray[i];
        final String minString = minStrArray[i];

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
  public static <T> void setAxisRange2( final IAxis iaxis, final Class<T> clazz, final String maxString, final String minString ) throws OWSException
  {
    final IDataOperator<T> da = iaxis.getDataOperator( clazz );
    Number min = null;
    Number max = null;
    try
    {
      min = da.logicalToNumeric( da.stringToLogical( minString ) );
    }
    catch( final MalformedValueException e )
    {
      throw new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "Value '" + minString + "' is not appropriate for Axis '" + iaxis.getIdentifier() + "'", null );
    }
    try
    {
      max = da.logicalToNumeric( da.stringToLogical( maxString ) );
    }
    catch( final MalformedValueException e )
    {
      throw new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "Value '" + maxString + "' is not appropriate for Axis '" + iaxis.getIdentifier() + "'", null );
    }
    final DataRange<Number> dr = new DataRange<Number>( min, max );
    iaxis.setNumericRange( dr );
  }

}
