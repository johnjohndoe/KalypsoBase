/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.swtchart.chart.axis.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.kalypso.swtchart.chart.axis.IAxis;
import org.kalypso.swtchart.chart.axis.IAxisConstants.POSITION;
import org.kalypso.swtchart.chart.axis.component.IAxisComponent;
import org.kalypso.swtchart.chart.axis.renderer.IAxisRenderer;

/**
 * @author schlienger
 */
public class AxisRegistry implements IAxisRegistry
{
  private final AxisRegistryEventHandler m_handler = new AxisRegistryEventHandler();

  /** axis-identifier --> axis */
  private final Map<String, IAxis> m_axes = new HashMap<String, IAxis>();

  /** axis-identifier --> renderer */
  private final Map<String, IAxisRenderer> m_id2renderers = new HashMap<String, IAxisRenderer>();

  private final Map<Class< ? >, IAxisRenderer> m_class2renderers = new HashMap<Class< ? >, IAxisRenderer>();

  /** axis --> component */
  private final Map<IAxis, IAxisComponent> m_components = new HashMap<IAxis, IAxisComponent>();

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistry#hasAxis(java.lang.String)
   */
  public boolean hasAxis( String identifier )
  {
    return m_axes.containsKey( identifier );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistry#getAxis(java.lang.String)
   */
  public IAxis getAxis( String identifier )
  {
    return m_axes.get( identifier );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistry#addAxis(org.kalypso.swtchart.axis.IAxis)
   */
  public void addAxis( IAxis axis )
  {
    // TODO: Exception wieder rein und else raus
    if( m_axes.containsKey( axis.getIdentifier() ) )
      System.out.println( "Axis already present in registry: " + axis.getIdentifier() + " - " + axis.getLabel() );
    // throw new IllegalArgumentException( "Axis already present in registry: " + axis.getIdentifier() + " - " +
    // axis.getLabel() );
    else
    {
      axis.setRegistry( this );

      m_axes.put( axis.getIdentifier(), axis );

      m_handler.fireAxisAdded( axis );
    }
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistry#removeAxis(org.kalypso.swtchart.axis.IAxis)
   */
  public void removeAxis( IAxis axis )
  {
    m_axes.remove( axis.getIdentifier() );

    IAxisComponent component = m_components.get( axis );
    if( component != null )
    {
      component.dispose();
      m_components.remove( axis );
    }

    m_handler.fireAxisRemoved( axis );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistry#clear()
   */
  public void clear( )
  {
    for( final IAxisComponent comp : m_components.values() )
      comp.dispose();
    m_components.clear();

    for( final IAxis axis : m_axes.values() )
      m_handler.fireAxisRemoved( axis );
    m_axes.clear();

    m_id2renderers.clear();
    m_class2renderers.clear();
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistryEventProvider#addAxisRegistryEventListener(org.kalypso.swtchart.axis.IAxisRegistryEventListener)
   */
  public void addAxisRegistryEventListener( IAxisRegistryEventListener l )
  {
    m_handler.addAxisRegistryEventListener( l );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistryEventProvider#removeAxisRegistryEventListener(org.kalypso.swtchart.axis.IAxisRegistryEventListener)
   */
  public void removeAxisRegistryEventListener( IAxisRegistryEventListener l )
  {
    m_handler.removeAxisRegistryEventListener( l );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistry#apply(org.kalypso.swtchart.axis.IAxisVisitor)
   */
  public void apply( IAxisVisitor axisVisitor )
  {
    for( final IAxis axis : m_axes.values() )
      axisVisitor.visitAxis( axis );
  }

  /**
   * @see org.kalypso.swtchart.axis.IAxisRegistry#getAxesAt(org.kalypso.swtchart.axis.IAxisConstants.POSITION)
   */
  public IAxis[] getAxesAt( POSITION pos )
  {
    final AxisPositionVisitor v = new AxisPositionVisitor( pos );
    apply( v );

    return v.getAxes();
  }

  /**
   * @see org.kalypso.swtchart.axis.registry.IAxisRegistry#getRenderer(org.kalypso.swtchart.axis.IAxis)
   */
  public IAxisRenderer getRenderer( final IAxis axis )
  {
    IAxisRenderer renderer = m_id2renderers.get( axis.getIdentifier() );
    if( renderer != null )
      return renderer;

    renderer = m_class2renderers.get( axis.getDataClass() );
    if( renderer != null )
      return renderer;

    for( final Entry<Class< ? >, IAxisRenderer> entry : m_class2renderers.entrySet() )
    {
      final Class< ? > dataClass = entry.getKey();
      renderer = entry.getValue();

      if( dataClass.isAssignableFrom( axis.getDataClass() ) )
        return renderer;
    }

    return null;
  }

  /**
   * @see org.kalypso.swtchart.axis.registry.IAxisRegistry#getComponent(org.kalypso.swtchart.axis.IAxis)
   */
  public IAxisComponent getComponent( final IAxis axis )
  {
    return m_components.get( axis );
  }

  /**
   * @see org.kalypso.swtchart.axis.registry.IAxisRegistry#setRenderer(org.kalypso.swtchart.axis.IAxis,
   *      org.kalypso.swtchart.axis.renderer.IAxisRenderer)
   */
  public void setRenderer( String identifier, IAxisRenderer renderer )
  {
    m_id2renderers.put( identifier, renderer );
  }

  /**
   * @see org.kalypso.swtchart.axis.registry.IAxisRegistry#unsetRenderer(org.kalypso.swtchart.axis.IAxis)
   */
  public void unsetRenderer( final String axisIdentifier )
  {
    m_id2renderers.remove( axisIdentifier );
  }

  /**
   * @see org.kalypso.swtchart.axis.registry.IAxisRegistry#setComponent(org.kalypso.swtchart.axis.IAxis,
   *      org.kalypso.swtchart.axis.component.IAxisComponent)
   */
  public void setComponent( final IAxis axis, final IAxisComponent comp )
  {
    m_components.put( axis, comp );
  }

  /**
   * @see org.kalypso.swtchart.axis.registry.IAxisRegistry#setRenderer(java.lang.Class,
   *      org.kalypso.swtchart.axis.renderer.IAxisRenderer)
   */
  public void setRenderer( final Class< ? > dataClass, final IAxisRenderer renderer )
  {
    m_class2renderers.put( dataClass, renderer );
  }

  /**
   * @see org.kalypso.swtchart.axis.registry.IAxisRegistry#unsetRenderer(java.lang.Class)
   */
  public void unsetRenderer( final Class< ? > dataClass )
  {
    m_class2renderers.remove( dataClass );
  }

  /**
   * @see org.kalypso.swtchart.axis.registry.IAxisRegistry#getAxes()
   */
  public IAxis[] getAxes( )
  {
    final Collection<IAxis> axes = m_axes.values();
    return axes.toArray( new IAxis[axes.size()] );
  }

  public Map<IAxis, IAxisComponent> getComponents( )
  {
    return m_components;
  }
}
