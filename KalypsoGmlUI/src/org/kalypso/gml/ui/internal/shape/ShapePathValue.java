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

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.kalypso.commons.resources.FileUtilities;

/**
 * @author Gernot Belger
 */
public class ShapePathValue extends AbstractObservableValue
{
  private final ShapeFileNewData m_input;

  public ShapePathValue( final ShapeFileNewData input )
  {
    m_input = input;
  }

  /**
   * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
   */
  @Override
  public Object getValueType( )
  {
    return IPath.class;
  }

  /**
   * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#doSetValue(java.lang.Object)
   */
  @Override
  protected void doSetValue( final Object value )
  {
    final IPath currentValue = doGetValue();
    final IPath path = (IPath) value;

    if( ObjectUtils.equals( path, currentValue ) )
      return;

    final IFile toFile = FileUtilities.toFile( path );
    m_input.setShapeFile( toFile );

    final ValueDiff diff = Diffs.createValueDiff( currentValue, path );
    fireValueChange( diff );
  }

  /**
   * @see org.eclipse.core.databinding.observable.value.AbstractObservableValue#doGetValue()
   */
  @Override
  protected IPath doGetValue( )
  {
    final IFile shpFile = m_input.getShpFile();
    if( shpFile == null )
      return null;

    return shpFile.getFullPath();
  }
}
