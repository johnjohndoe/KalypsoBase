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
package org.kalypso.gml.ui.internal.shape;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Gernot Belger
 *
 */
public class PathButtonValue implements ISWTObservableValue
{

  /**
   * @see org.eclipse.jface.databinding.swt.ISWTObservable#getWidget()
   */
  @Override
  public Widget getWidget( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#getRealm()
   */
  @Override
  public Realm getRealm( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#addChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
   */
  @Override
  public void addChangeListener( IChangeListener listener )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#removeChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
   */
  @Override
  public void removeChangeListener( IChangeListener listener )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#addStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
   */
  @Override
  public void addStaleListener( IStaleListener listener )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#removeStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
   */
  @Override
  public void removeStaleListener( IStaleListener listener )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#isStale()
   */
  @Override
  public boolean isStale( )
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#addDisposeListener(org.eclipse.core.databinding.observable.IDisposeListener)
   */
  @Override
  public void addDisposeListener( IDisposeListener listener )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#removeDisposeListener(org.eclipse.core.databinding.observable.IDisposeListener)
   */
  @Override
  public void removeDisposeListener( IDisposeListener listener )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#isDisposed()
   */
  @Override
  public boolean isDisposed( )
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.eclipse.core.databinding.observable.IObservable#dispose()
   */
  @Override
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
   */
  @Override
  public Object getValueType( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValue()
   */
  @Override
  public Object getValue( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.eclipse.core.databinding.observable.value.IObservableValue#setValue(java.lang.Object)
   */
  @Override
  public void setValue( Object value )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.value.IObservableValue#addValueChangeListener(org.eclipse.core.databinding.observable.value.IValueChangeListener)
   */
  @Override
  public void addValueChangeListener( IValueChangeListener listener )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.core.databinding.observable.value.IObservableValue#removeValueChangeListener(org.eclipse.core.databinding.observable.value.IValueChangeListener)
   */
  @Override
  public void removeValueChangeListener( IValueChangeListener listener )
  {
    // TODO Auto-generated method stub

  }

}
