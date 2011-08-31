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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.pages;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.ILanduseShapeDataProvider;
import org.kalypso.shape.IShapeFileVisitor;
import org.kalypso.shape.ShapeFile;

/**
 * @author Dirk Kuch
 */
public class LanduseTableMappingHandler implements IRunnableWithProgress
{

  private final ILanduseShapeDataProvider m_shape;

  private final ImportLanduseDataModel m_model;

  public LanduseTableMappingHandler( final ILanduseShapeDataProvider shape, final ImportLanduseDataModel model )
  {
    m_shape = shape;
    m_model = model;
  }

  /**
   * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void run( final IProgressMonitor monitor )
  {
    try
    {
      final ShapeFile shapeFile = m_shape.getShapeFile();
      final Set<Object> shapeValues = new LinkedHashSet<Object>();
      final int column = ArrayUtils.indexOf( shapeFile.getFields(), m_model.getShapeColumn() );

      shapeFile.accept( new IShapeFileVisitor()
      {
        @Override
        public void visit( final Object[] row )
        {
          shapeValues.add( row[column] );
        }
      } );

      final Properties properties = m_model.getMapping();
      for( final Object value : shapeValues )
      {
        properties.put( value, "" );
      }
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }

  }
}
