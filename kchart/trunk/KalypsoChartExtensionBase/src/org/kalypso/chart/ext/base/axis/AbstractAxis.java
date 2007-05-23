package org.kalypso.chart.ext.base.axis;

import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.DataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.POSITION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.PROPERTY;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.renderer.IAxisRenderer;
import org.kalypso.chart.framework.view.AxisCanvas;


/**
 * @author burtscher
 *
 * Abstract implementation of IAxis - implements some
 * methods which are equal for all concrete IAxis-classes
 */
public abstract class AbstractAxis<T> implements IAxis<T>
{
  private IMapperRegistry m_registry = null;

  private final String m_id;

  private final String m_label;

  private final PROPERTY m_prop;

  private final POSITION m_pos;

  private final DIRECTION m_dir;

  private final Comparator<T> m_dataComparator;

  private IDataRange<T> m_dataRange;

  private final Class< ? > m_dataClass;

  /**
   * Uses a ComparableComparator as dataComparator
   */
  @SuppressWarnings("unchecked")
  public AbstractAxis( final String id, final String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final Class< ? > dataClass )
  {
    this( id, label, prop, pos, dir, new ComparableComparator(), dataClass );
  }

  public AbstractAxis( final String id, final String label, final PROPERTY prop, final POSITION pos, final DIRECTION dir, final Comparator<T> dataComparator, final Class< ? > dataClass )
  {
    m_id = id;
    m_label = label;
    m_prop = prop;
    m_pos = pos;
    m_dir = dir;
    m_dataComparator = dataComparator;
    m_dataClass = dataClass;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getDataClass()
   */
  public Class< ? > getDataClass( )
  {
    return m_dataClass;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#setRegistry(org.kalypso.chart.framework.axis.registry.IMapperRegistry)
   */
  public void setRegistry( final IMapperRegistry mapperRegistry )
  {
    m_registry = mapperRegistry;
  }
  


  public IMapperRegistry  getRegistry( )
  {
    return m_registry;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getRenderer()
   */
  @SuppressWarnings("unchecked")
  public IAxisRenderer<T> getRenderer( )
  {
    if( m_registry == null )
      throw new IllegalStateException( "Registry is null" );

    return m_registry.getRenderer( this );
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getIdentifier()
   */
  public String getIdentifier( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getLabel()
   */
  public String getLabel( )
  {
    return m_label;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getProperty()
   */
  public PROPERTY getProperty( )
  {
    return m_prop;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getPosition()
   */
  public POSITION getPosition( )
  {
    return m_pos;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#getDirection()
   */
  public DIRECTION getDirection( )
  {
    return m_dir;
  }

  /**
   * @see org.kalypso.chart.framework.axis.IAxis#isInverted()
   */
  public boolean isInverted( )
  {
    return getDirection() == DIRECTION.NEGATIVE;
  }


  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return m_label + " " + "{" + m_id + " " + m_pos + " " + m_dir + "}";
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxis#autorange(org.kalypso.swtchart.layer.IDataRange<T>[])
   */
  public void autorange( final IDataRange<T>[] ranges )
  {
    T min = null;
    T max = null;

    for( int i = 0; i < ranges.length; i++ )
    {
      if( min == null || m_dataComparator.compare( min, ranges[i].getMin() ) > 0 )
        min = ranges[i].getMin();
      if( max == null || m_dataComparator.compare( max, ranges[i].getMax() ) < 0 )
        max = ranges[i].getMax();
    }

    DataRange<T> range=null;
    try
	{
		range= new DataRange<T>(min, max);
	} catch (ZeroSizeDataRangeException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    setDataRange(range);
  }
  
  public IDataRange<T> getDataRange()
  {
	  return m_dataRange;
  }
  
  public void setDataRange(IDataRange<T> dataRange)
  {
	  m_dataRange=dataRange;
  }
}
