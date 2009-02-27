package de.openali.diagram.framework.model.mapper.registry;

import java.util.Map;

import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.IAxisConstants;
import de.openali.diagram.framework.model.mapper.IMapper;
import de.openali.diagram.framework.model.mapper.component.IAxisComponent;
import de.openali.diagram.framework.model.mapper.renderer.IAxisRenderer;

/**
 * @author alibu
 *
 * Interface describing a container for the chart axes; used
 * to ensure that only one axis is present for each DataType
 */


public interface IMapperRegistry extends IMapperRegistryEventProvider
{

 
  /**
   * @return Array of all present axes
   */
  public IAxis[] getAxes( );
  
  
  public IAxis getAxis(String id);

  
  public IMapper getMapper(String id);

  
  /**
   * @return Array of all axes at the given position
   */
  public IAxis[] getAxesAt( IAxisConstants.POSITION pos );


  /**
   * @return renderer for the given axis. If first looks up the renderer that were explicitely registered for a given
   *         axis. If no renderer is found for that axis, it looks up the renderer based on the dataClass of the axis.
   *         If still no renderer is found, it tries to find a renderer for a super class of the axis dataClass.
   */
  public IAxisRenderer getRenderer( IAxis<?> axis );

  /**
   * sets the AxisRenderer for a particular dataClass
   */
  public void setRenderer( Class< ? > dataClass, IAxisRenderer renderer );

  /**
   * sets the AxisRenderer for a particular axis, identified by - guess what - the axis' identifier
   */
  public void setRenderer( String identifier, IAxisRenderer renderer );

  /**
   * removes the renderer for a particular dataClass
   */
  public void unsetRenderer( Class< ? > dataClass );

  /**
   * removes the renderer for a particular axis
   */
  public void unsetRenderer( String identifier );

  /**
   * @return the AxisComponent of the given axis or null if there isn't any
   */
  public IAxisComponent getComponent( IAxis axis );

  /**
   * sets the component for a particular axis
   */
  public void setComponent( IAxis axis, IAxisComponent comp );

  /**
   * @return map of Axis-AxisComponent-Pairs
   */
  public Map<IAxis, IAxisComponent> getAxesToComponentsMap( );


  /**
   * removes all IAxis, IAxisRenderer and IAxisComponent entries
   *
   */
  public void clear();
  
  public void addMapper(IMapper mapper);

}
