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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.model;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.dbf.IDBFField;

/**
 * @author Dirk Kuch
 */
public class LanduseMappingUpdater implements IRunnableWithProgress
{
  private final ShapeFile m_file;

  private final LanduseProperties m_properties;

  private final IDBFField m_field;

  public LanduseMappingUpdater( final ShapeFile file, final IDBFField field, final LanduseProperties properties )
  {
    m_file = file;
    m_field = field;
    m_properties = properties;
  }

  @Override
  public void run( final IProgressMonitor monitor )
  {
    try
    {
      final int column = ArrayUtils.indexOf( m_file.getFields(), m_field );

      final int numRecords = m_file.getNumRecords();
      for( int index = 0; index < numRecords; index++ )
      {
        final Object[] row = m_file.getRow( index );
        final Object value = row[column];

        // only add empty mappings
        if( Objects.isNull( m_properties.getProperty( value.toString() ) ) )
          m_properties.put( value, "" ); //$NON-NLS-1$
      }
    }
    catch( final Exception ex )
    {
      // FIXME: error handling!
      ex.printStackTrace();
    }
  }
}