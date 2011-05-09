/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.ui.editor.styleeditor.binding;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoStyleListener;
import org.kalypso.ui.editor.styleeditor.IStyleContext;

/**
 * @author Gernot Belger
 */
// FIXME: remove all these one-constructor implementation by one generic implementation
public class StyleInput<DATA> implements IStyleInput<DATA>
{
  private final IKalypsoStyleListener m_styleListener = new IKalypsoStyleListener()
  {
    @Override
    public void styleChanged( )
    {
      handleStyleChanged();
    }
  };

  private final Collection<IKalypsoStyleListener> m_listener = new HashSet<IKalypsoStyleListener>();

  private final IStyleContext m_context;

  private final DATA m_data;

  public StyleInput( final DATA data, final IStyleContext context )
  {
    m_data = data;
    m_context = context;
    final IKalypsoStyle style = m_context == null ? null : m_context.getKalypsoStyle();
    if( style != null )
      style.addStyleListener( m_styleListener );
  }

  /**
   * @see org.kalypso.commons.field.IFieldInput#getInput()
   */
  @Override
  public final DATA getData( )
  {
    return m_data;
  }

  protected void handleStyleChanged( )
  {
    final IKalypsoStyleListener[] listener = m_listener.toArray( new IKalypsoStyleListener[m_listener.size()] );
    for( final IKalypsoStyleListener l : listener )
      l.styleChanged();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.forms.IFormInputWithContext#dispose()
   */
  @Override
  public void dispose( )
  {
    final IKalypsoStyle style = m_context.getKalypsoStyle();
    if( style != null )
      style.removeStyleListener( m_styleListener );
  }

  /**
   * @see org.kalypso.commons.field.IFieldInput#fireInputChanged()
   */
  @Override
  public final void fireInputChanged( )
  {
    if( m_context != null )
      m_context.fireStyleChanged();
  }

  @Override
  public final IStyleContext getContext( )
  {
    return m_context;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.forms.IFormInputWithContext#addStyleListener(org.kalypso.ogc.gml.IKalypsoStyleListener)
   */
  @Override
  public void addStyleListener( final IKalypsoStyleListener listener )
  {
    m_listener.add( listener );
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.forms.IFormInputWithContext#removeStyleListener(org.kalypso.ogc.gml.IKalypsoStyleListener)
   */
  @Override
  public void removeStyleListener( final IKalypsoStyleListener listener )
  {
    m_listener.remove( listener );
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return new HashCodeBuilder().append( getData() ).append( getContext() ).toHashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj == null )
      return false;
    if( obj == this )
      return true;
    if( obj.getClass() != getClass() )
      return false;

    final StyleInput< ? > other = (StyleInput< ? >) obj;

    return new EqualsBuilder().append( getData(), other.getData() ).append( getContext(), other.getContext() ).isEquals();
  }
}
