package org.kalypso.chart.framework.model;

import java.util.List;
import java.util.Map;

import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;


public interface IChartModel 
{

	public IMapperRegistry getAxisRegistry();

	public ILayerManager getLayerManager();

	public void clear();

	public Map<IAxis<?>, List<IChartLayer>> getAxis2Layers();

	/**
	 * @param b
	 *          if true, axes in the AxisRegistry which are not used by any layer are hidden; if false, all axes are shown
	 */
	public void setHideUnusedAxes(boolean b);

}