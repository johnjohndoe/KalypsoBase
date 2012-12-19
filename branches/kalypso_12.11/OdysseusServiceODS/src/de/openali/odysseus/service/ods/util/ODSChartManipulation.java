package de.openali.odysseus.service.ods.util;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.service.OGCRequest;
import org.kalypso.ogc.core.utils.OWSUtilities;

import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisRegistry;

/**
 * Helper class for chart stuff concerning request parameters.
 * 
 * @author Alexander Burtscher
 */
public class ODSChartManipulation
{
  public static void manipulateChart( final IChartModel model, final OGCRequest request ) throws OWSException
  {
    setLayerVisibility( model.getLayerManager(), request );
    setAxesRanges( model.getAxisRegistry(), request );
  }

  /**
   * sets the layers visibility and position from the request parameter LAYERS - if the parameter is not empty, all
   * those layers which are not mentioned in the comma separated list are set invisible
   */
  private static void setLayerVisibility( final ILayerManager manager, final OGCRequest request )
  {
    final String reqLayerString = request.getParameterValue( "LAYERS" );
    String[] reqLayers = null;
    if( reqLayerString != null && !reqLayerString.trim().equals( "" ) )
      reqLayers = reqLayerString.trim().split( "," );

    /* Herausfinden, welche von den angeforderten Layers wirklich existieren. */
    final List<String> realReqLayers = new ArrayList<>();

    if( reqLayers != null )
    {
      for( final String layerId : reqLayers )
      {
        if( manager.findLayer( layerId ) != null )
          realReqLayers.add( layerId );
      }
    }

    if( realReqLayers.size() > 0 )
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

  public static void setAxesRanges( final IAxisRegistry ar, final OGCRequest request ) throws OWSException
  {
    setAxesRanges( ar, request.getParameterValue( "AXES" ), request.getParameterValue( "AXES_MIN" ), request.getParameterValue( "AXES_MAX" ) );
  }

  private static void setAxesRanges( final IAxisRegistry mr, final String axesString, final String minsString, final String maxsString ) throws OWSException
  {
    /* Falls der AXES-Parameter nicht angegeben ist, dann wird einfach abgebrochen. */
    if( axesString == null )
      return;

    /* Zunächst AXISMIN- und AXISMAX-Parameter parsen und zu Min/MaxMap hinzufügen. */
    final String[] axesStrArray = axesString.split( "," );
    final String[] minStrArray = minsString.split( "," );
    final String[] maxStrArray = maxsString.split( "," );

    if( axesStrArray.length != minStrArray.length || axesStrArray.length != maxStrArray.length )
      throw new OWSException( "AXES, AXES_MIN and AXES_MAX must contain equal number of values", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

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
  public static <T> void setAxisRange2( final IAxis<T> iaxis, final Class<T> clazz, final String maxString, final String minString ) throws OWSException
  {
    // final IDataOperator<T> da = iaxis.getDataOperator( clazz );
    T min = null;
    T max = null;

    try
    {
      min = iaxis.xmlStringToLogical( minString );
    }
    catch( final MalformedValueException e )
    {
      throw new OWSException( "Value '" + minString + "' is not appropriate for Axis '" + iaxis.getIdentifier() + "'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );
    }

    try
    {
      max = iaxis.xmlStringToLogical( maxString );
    }
    catch( final MalformedValueException e )
    {
      throw new OWSException( "Value '" + maxString + "' is not appropriate for Axis '" + iaxis.getIdentifier() + "'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );
    }

    final IDataRange<T> dr = new DataRange<>( min, max );
    iaxis.setLogicalRange( dr );
  }
}