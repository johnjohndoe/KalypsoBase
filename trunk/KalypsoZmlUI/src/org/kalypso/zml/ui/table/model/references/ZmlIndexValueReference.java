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
package org.kalypso.zml.ui.table.model.references;

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.model.IZmlDataModel;
import org.kalypso.zml.ui.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.model.IZmlModelRow;

/**
 * @author Dirk Kuch
 */
public class ZmlIndexValueReference implements IZmlValueReference
{
  private final BaseColumn m_column;

  private final IZmlModelRow m_row;

  public ZmlIndexValueReference( final IZmlModelRow row, final BaseColumn column )
  {
    m_row = row;
    m_column = column;
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlValueReference#getValue()
   */
  @Override
  public Object getValue( )
  {
    return m_row.getIndexValue();
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlValueReference#getStatus()
   */
  @Override
  public Integer getStatus( )
  {
    return KalypsoStati.BIT_OK;
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlValueReference#getMetadata()
   */
  @Override
  public MetadataList getMetadata( )
  {
    final IZmlValueReference[] references = m_row.getReferences();

    for( final IZmlValueReference reference : references )
    {
      try
      {
        if( reference.isMetadataSource() && reference.getValue() != null )
          return reference.getMetadata();
      }
      catch( final SensorException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return new MetadataList();
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlValueReference#getAxis()
   */
  @Override
  public IAxis getValueAxis( )
  {
    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlValueReference#update(java.lang.Object)
   */
  @Override
  public void update( final Object targetValue )
  {
    throw new NotImplementedException();
  }

  /**
   * @see org.kalypso.zml.ui.table.provider.IZmlValueReference#isMetadataSource()
   */
  @Override
  public boolean isMetadataSource( )
  {
    return true;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getColumn()
   */
  @Override
  public IZmlModelColumn getColumn( )
  {
    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getBaseColumn()
   */
  @Override
  public BaseColumn getBaseColumn( )
  {
    return m_column;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getRow()
   */
  @Override
  public IZmlModelRow getRow( )
  {
    return m_row;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getTupleModelIndex()
   */
  @Override
  public Integer getTupleModelIndex( )
  {
    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.model.references.IZmlValueReference#getModel()
   */
  @Override
  public IZmlDataModel getModel( )
  {
    return m_row.getModel();
  }
}
