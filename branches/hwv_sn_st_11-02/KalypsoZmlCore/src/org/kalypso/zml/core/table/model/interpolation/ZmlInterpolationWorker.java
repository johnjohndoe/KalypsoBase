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
package org.kalypso.zml.core.table.model.interpolation;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;

/**
 * @author Dirk Kuch
 */
public class ZmlInterpolationWorker implements ICoreRunnableWithProgress
{

  private final IZmlModelColumn m_column;

  public ZmlInterpolationWorker( final IZmlModelColumn column )
  {
    m_column = column;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      final FindStuetzstellenVisitor visitor = new FindStuetzstellenVisitor();
      m_column.accept( visitor );

      final IZmlValueReference[] stuetzstellen = visitor.getStuetzstellen();
      if( ArrayUtils.isEmpty( stuetzstellen ) )
      {
        ZmlInterpolation.setNull( m_column, 0, m_column.size() );
        return Status.OK_STATUS;
      }

      // set all values 0 before first stuetzstelle
      if( stuetzstellen[0].getModelIndex() > 0 )
        ZmlInterpolation.setNull( m_column, 0, stuetzstellen[0].getModelIndex() );

      for( int index = 0; index < stuetzstellen.length - 2; index++ )
      {
        final IZmlValueReference stuetzstelle1 = stuetzstellen[index];
        final IZmlValueReference stuetzstelle2 = stuetzstellen[index + 1];
        ZmlInterpolation.interpolate( m_column, stuetzstelle1, stuetzstelle2 );
      }

      // set all values 0 after last stuetzstelle
      final IZmlValueReference last = stuetzstellen[stuetzstellen.length - 1];
      if( last.getModelIndex() != m_column.size() - 1 )
        ZmlInterpolation.setNull( m_column, last.getModelIndex() + 1, m_column.size() );

      return Status.OK_STATUS;
    }
    catch( final SensorException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.createExceptionalErrorStatus( "(Re)Interpolating values failed", e ) );
    }
  }

}
