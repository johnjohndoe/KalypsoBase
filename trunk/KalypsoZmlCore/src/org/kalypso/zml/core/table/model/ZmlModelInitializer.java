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
package org.kalypso.zml.core.table.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.zml.core.debug.KalypsoZmlCoreDebug;
import org.kalypso.zml.core.i18n.Messages;
import org.kalypso.zml.core.table.binding.TableTypes;
import org.kalypso.zml.core.table.model.loader.ZmlModelColumnLoader;
import org.kalypso.zml.core.table.model.utils.IClonedColumn;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.DataSourcePropertyType;
import org.kalypso.zml.core.table.schema.DataSourcesType;
import org.kalypso.zml.core.table.schema.ObjectFactory;
import org.kalypso.zml.core.table.schema.ZmlTableType;

/**
 * @author Dirk Kuch
 */
public class ZmlModelInitializer implements ICoreRunnableWithProgress
{

  private final ZmlModel m_model;

  public ZmlModelInitializer( final ZmlModel model )
  {
    m_model = model;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    doLoadColumns();

    return Status.OK_STATUS;
  }

  private void doLoadColumns( )
  {
    final Map<String, Set<DataSourcePropertyType>> map = getSources();
    final Set<Entry<String, Set<DataSourcePropertyType>>> entries = map.entrySet();
    for( final Entry<String, Set<DataSourcePropertyType>> entry : entries )
    {
      synchronized( this )
      {
        final DataSourcePropertyType[] types = entry.getValue().toArray( new DataSourcePropertyType[] {} );
        for( int index = 0; index < types.length; index++ )
        {
          final DataSourcePropertyType source = types[index];
          final AbstractColumnType base = (AbstractColumnType) source.getColumn();

          final String href = source.getHref();
          final ZmlModelColumnLoader loader = m_model.getLoader();
          if( index == 0 )
          {
            final String identifier = base.getId();
            KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( Messages.ZmlModelInitializer_0, identifier, href );

            final ZmlDataSourceElement element = new ZmlDataSourceElement( identifier, href, m_model.getContext(), source.getLabel(), m_model.getMemento() );
            loader.load( element );
          }
          else if( index > 0 )
          {
            final String multipleIdentifier = String.format( IClonedColumn.CLONED_COLUMN_POSTFIX_FORMAT, base.getId(), index );
            final AbstractColumnType clone = TableTypes.cloneColumn( base );
            clone.setId( multipleIdentifier );

            appendColumnType( clone );

            KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( Messages.ZmlModelInitializer_1, multipleIdentifier, href );
            final ZmlDataSourceElement element = new ZmlDataSourceElement( multipleIdentifier, href, m_model.getContext(), source.getLabel(), m_model.getMemento() );
            loader.load( element );
          }
        }
      }
    }
  }

  private void appendColumnType( final AbstractColumnType type )
  {
    final ZmlTableType tableType = m_model.getTableType();
    final ObjectFactory factory = new ObjectFactory();
    final JAXBElement<AbstractColumnType> jaxbElement = factory.createAbstractColumn( type );
    tableType.getColumns().getAbstractColumn().add( jaxbElement );
  }

  private Map<String, Set<DataSourcePropertyType>> getSources( )
  {
    final Map<String, Set<DataSourcePropertyType>> map = new HashMap<>();

    final ZmlTableType tableType = m_model.getTableType();
    if( Objects.isNull( tableType ) )
      return map;

    final DataSourcesType dataSourcesType = tableType.getDataSources();
    if( Objects.isNull( dataSourcesType ) )
      return map;

    final List<DataSourcePropertyType> sources = dataSourcesType.getSource();

    for( final DataSourcePropertyType source : sources )
    {
      final AbstractColumnType columnType = (AbstractColumnType) source.getColumn();
      final String identifier = columnType.getId();

      Set<DataSourcePropertyType> types = map.get( identifier );
      if( Objects.isNull( types ) )
      {
        types = new LinkedHashSet<>();
        map.put( identifier, types );
      }

      types.add( source );
    }

    return map;
  }
}
