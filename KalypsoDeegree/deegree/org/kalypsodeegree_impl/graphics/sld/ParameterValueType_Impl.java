/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.graphics.sld;

import java.util.ArrayList;
import java.util.List;

import org.kalypsodeegree.filterencoding.Expression;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.Marshallable;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 */
public class ParameterValueType_Impl implements ParameterValueType, Marshallable
{
  private final List<Object> m_components = new ArrayList<>();

  /**
   * Constructs a new <tt>ParameterValueType_Impl</tt>.
   * <p>
   *
   * @param components
   *          <tt>String</tt>s/<tt>Expression</tt> s that make up the contents of the <tt>ParameterValueType_Impl</tt>
   */
  public ParameterValueType_Impl( final Object[] components )
  {
    setComponents( components );
  }

  /**
   * Returns the contents (mix of <tt>String</tt>/<tt>Expression</tt> -objects) of this <tt>ParameterValueType</tt>.
   * <p>
   *
   * @return mix of <tt>String</tt>/<tt>Expression</tt> -objects
   */
  @Override
  public Object[] getComponents( )
  {
    return m_components.toArray( new Object[m_components.size()] );
  }

  /**
   * Sets the contents (mix of <tt>String</tt>/<tt>Expression</tt> -objects) of this <tt>ParameterValueType</tt>.
   * <p>
   *
   * @param components
   *          mix of <tt>String</tt> and <tt>Expression</tt> -objects
   */
  @Override
  public void setComponents( final Object[] components )
  {
    this.m_components.clear();

    if( components != null )
    {
      for( final Object component : components )
      {
        this.m_components.add( component );
      }
    }
  }

  /**
   * Concatenates a component (a<tt>String</tt> or an <tt>Expression</tt> -object) to this <tt>ParameterValueType</tt>.
   * <p>
   *
   * @param component
   *          either a <tt>String</tt> or an <tt>Expression</tt> -object
   */
  @Override
  public void addComponent( final Object component )
  {
    m_components.add( component );
  }

  /**
   * Removes a component (a<tt>String</tt> or an <tt>Expression</tt> -object) from this <tt>ParameterValueType</tt>.
   * <p>
   *
   * @param component
   *          either a <tt>String</tt> or an <tt>Expression</tt> -object
   */
  @Override
  public void removeComponent( final Object component )
  {
    m_components.remove( m_components.indexOf( component ) );
  }

  /**
   * Returns the actual <tt>String</tt> value of this object. Expressions are evaluated according to the given
   * <tt>Feature</tt> -instance.
   * <p>
   *
   * @param feature
   *          used for the evaluation of the underlying 'wfs:Expression'-elements
   * @return the (evaluated) String value
   * @throws FilterEvaluationException
   *           if the evaluation fails
   */
  @Override
  public String evaluate( final Feature feature ) throws FilterEvaluationException
  {
    final StringBuilder sb = new StringBuilder();

    for( int i = 0; i < m_components.size(); i++ )
    {
      final Object component = m_components.get( i );
      if( component instanceof Expression )
      {
        final Object expr = ((Expression) component).evaluate( feature );
        if( expr instanceof List< ? > )
        {
          final List< ? > list = (List< ? >) expr;
          if( list.size() == 1 )
          {
            final Object object = list.get( 0 );
            if( object == null )
              sb.append( expr );
            else
              sb.append( object );
          }
        }
        else if( expr != null )
          sb.append( expr );
      }
      else if( component != null && component instanceof String )
      {
        sb.append( ((String) component).trim() );
      }
      else
      {
        sb.append( component );
      }
    }

    return sb.toString();
  }

  /**
   * exports the content of the ParameterValueType as XML formated String
   *
   * @return xml representation of the ParameterValueType
   */
  @Override
  public String exportAsXML( )
  {
    final StringBuilder sb = new StringBuilder();
    for( int i = 0; i < m_components.size(); i++ )
    {
      final Object component = m_components.get( i );
      if( component instanceof Expression )
      {
        sb.append( ((Expression) component).toXML() );
      }
      else if( component != null && component instanceof String )
      {
        sb.append( ((String) component).trim() );
      }
      else
      {
        sb.append( component );
      }
    }

    return sb.toString();
  }
}