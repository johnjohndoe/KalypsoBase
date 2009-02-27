package de.openali.diagram.framework.model;

import java.util.List;
import java.util.Map;

import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.layer.ILayerManager;
import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistry;

public interface IDiagramModel 
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