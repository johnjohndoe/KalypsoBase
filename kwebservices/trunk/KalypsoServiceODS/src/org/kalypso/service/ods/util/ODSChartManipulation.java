package org.kalypso.service.ods.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kalypso.chart.framework.exception.MalformedValueException;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.IStringDataConverter;
import org.kalypso.chart.framework.model.data.impl.DataRange;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.service.ogc.RequestBean;

/**
 * @author alibu
 *
 * Helper class for chart stuff concerning request parameters
 */
public class ODSChartManipulation
{
  
 
  public static void manipulateChart(IChartModel model, RequestBean requestBean)
  {
	  setLayerVisibility(model.getLayerManager(), requestBean);
	  setAxesRange(model.getMapperRegistry(), requestBean);
  }
  
  
  /**
   * sets the layers visibility and position from the request parameter LAYERS - if the parameter
   * is not empty, all those layers which are not mentioned in the comma separated list
   * are set invisible
   */
  public static void setLayerVisibility(ILayerManager manager, RequestBean requestBean)
  {
    String reqLayerString=requestBean.getParameterValue( "LAYERS" );
    String[] reqLayers=null;
    if (reqLayerString!=null && !reqLayerString.trim().equals(""))
    {
      reqLayers=reqLayerString.trim().split( "," );
    }
    //herausfinden, welche von den angeforderten Layers wirklich existieren
    
    List<String> realReqLayers=new ArrayList<String>();
    if (reqLayers!=null)
    {
	    for (String layerId : reqLayers)
		{
			if (manager.getLayerById(layerId)!=null)
				realReqLayers.add(layerId);
		}
    }
    if (realReqLayers!=null && realReqLayers.size()>0)
        {
        	for (int i=0;i<realReqLayers.size();i++)
        	{
        		IChartLayer<?,?> layer=manager.getLayerById(realReqLayers.get(i));
        		if (layer!=null)
        		{
        			manager.moveLayerToPosition(layer, i);
        		}
        	}
        	IChartLayer<?,?>[] layers = manager.getLayers();
        	for (int i=realReqLayers.size();i<layers.length;i++)
        	{
        		IChartLayer<?,?> layer=layers[i];
        		layer.setVisible(false);
        	}
        	
        }
    
    
  }
  
  /**
   * sets the charts axes values as given by the request parameters AXIS_MIN and AXIS_MAX
   */
  public static void setAxesRange(IMapperRegistry ar, RequestBean requestBean )
  {
    String minstring=null;
    String maxstring=null;
    if (requestBean!=null)
    {
      minstring=requestBean.getParameterValue( "AXISMIN" );
      maxstring=requestBean.getParameterValue( "AXISMAX" );
    } 
    
    //RangeMaps erzeugen
    Map<IAxis<?>, String> minMap=getAxisRangeMap(minstring, ar);
    Map<IAxis<?>, String> maxMap=getAxisRangeMap(maxstring, ar);
    
    /*
     * Jetzt beide Maps durchlaufen und AxisRange erzeugen;
     * falls für eine AxisRange nur ein Wert (min oder max)
     * anwendbar ist, wird der andere Wert aus der aktuellen 
     * Range verwendet
     */
     
    for (IAxis<?> iaxis:  minMap.keySet())
    {
    	setAxisRange(iaxis, minMap.get(iaxis), maxMap.get(iaxis));
    }
    
    /*
     * Beim Durchlauf der maxMap muss gesichert sein, dass die Keys (=Achse)
     * NICHT in der minMap vorkommen, da man die DataRange sonst doppelt erzeugt
     */
    for (IAxis<?> iaxis:  maxMap.keySet())
    {
    	if (!minMap.containsKey(iaxis))
    	{
    		setAxisRange(iaxis, null, maxMap.get(iaxis));
    	}
    }
  }
  
  /**
   * sets range for an individual axis; set to public so an axis can be re-ranged widthout
   * the need for a complete chartmodel (e.g. by GetAxesInfo, GetAxis)
   * 
   * @param axis
   * @param requestBean
   */
  public static <T> void setAxisRange(IAxis<T> axis, String minVal, String maxVal)
  {
	  IStringDataConverter<T> sdc = axis.getDataOperator();
	  IDataRange<T> dataRange = axis.getDataRange();
	  try {
		  T min=null;
		  T max=null;
		  /*
		   * wenn min- oder maxString null sind, wird der entsprechende Wert der DataRange verwendet
		   */
		  if (minVal!=null)
			  min=sdc.stringToLogical(minVal);
		  else
			  min=dataRange.getMin();
		  if (maxVal!=null)
			  max=sdc.stringToLogical(maxVal);
		  else
			  max=dataRange.getMax();
		  axis.setDataRange(new DataRange<T>(min, max));
	} catch (MalformedValueException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ZeroSizeDataRangeException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
  public static Map<IAxis<?>, String> getAxisRangeMap(String requestString, IMapperRegistry mr)
  {
	  Map<IAxis<?>, String> rangeMap=new HashMap<IAxis<?>, String>();
	    
	    /*
	     * Zunächst AXISMIN- und AXISMAX-Parameter parsen und zu Min/MaxMap hinzufügen 
	     */
	    
	    
	    if (requestString!=null && !"".equals(requestString.trim()))
	    {
	      String[] axesStrArray = requestString.split( "," );
	      
	      
	      for( String axisStr : axesStrArray )
	      {
	        String[] axinfo = axisStr.split( "\\|" );
	        IAxis<?> iaxis = mr.getAxis( axinfo[0] );
	        rangeMap.put(iaxis, axinfo[1]);
	      }
	    }
	    return rangeMap;
  }


  
}
