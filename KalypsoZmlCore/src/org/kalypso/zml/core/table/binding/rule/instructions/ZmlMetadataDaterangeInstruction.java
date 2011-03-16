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
package org.kalypso.zml.core.table.binding.rule.instructions;

import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.references.ZmlDataValueReference;
import org.kalypso.zml.core.table.model.references.ZmlIndexValueReference;
import org.kalypso.zml.core.table.schema.AbstractRuleInstructionType;
import org.kalypso.zml.core.table.schema.MetadataDateRangeInstructionType;

/**
 * @author Dirk kuch
 */
public class ZmlMetadataDaterangeInstruction extends AbstractZmlRuleInstructionType
{

  public ZmlMetadataDaterangeInstruction( final AbstractRuleInstructionType type )
  {
    super( type );
  }

  @Override
  public MetadataDateRangeInstructionType getType( )
  {
    return (MetadataDateRangeInstructionType) super.getType();
  }

  /**
   * @see org.kalypso.zml.core.table.binding.rule.instructions.AbstractZmlRuleInstructionType#matches(org.kalypso.zml.core.table.model.references.IZmlValueReference)
   */
  @Override
  public boolean matches( final IZmlValueReference reference ) throws SensorException
  {
    final MetadataList metadata = resolveMetadata( reference );
    final MetadataDateRangeInstructionType type = getType();

    final Date from = getDate( metadata, type.getPropertyFrom() );
    final Date to = getDate( metadata, type.getPropertyTo() );
    if( from == null || to == null )
      return false;

    final Object objDate = reference.getIndexValue();
    if( !(objDate instanceof Date) )
      return false;

    final Date referenceDate = (Date) objDate;

    final long diffFrom = from.getTime() - referenceDate.getTime();
    if( diffFrom > 0 )
      return false;
    else if( !type.isPropertyFromInclusive() && diffFrom == 0 )
      return false;

    final long diffTo = to.getTime() - referenceDate.getTime();

    if( diffTo < 0 )
      return false;
    else if( !type.isPropertyToInclusive() && diffTo == 0 )
      return false;

    return true;
  }

  private MetadataList resolveMetadata( final IZmlValueReference reference )
  {
    if( reference instanceof ZmlDataValueReference )
    {
      final IZmlModelColumn column = reference.getColumn();

      return column.getMetadata();
    }
    else if( reference instanceof ZmlIndexValueReference )
    {
      final IZmlModel model = reference.getModel();
      final IZmlModelColumn[] columns = model.getColumns();
      for( final IZmlModelColumn column : columns )
      {
        if( column.isMetadataSource() )
          return column.getMetadata();
      }

      // fallback
      if( ArrayUtils.isNotEmpty( columns ) )
        return columns[0].getMetadata();
    }

    throw new NotImplementedException();
  }

  private Date getDate( final MetadataList metadata, final String property )
  {
    if( metadata == null || property == null )
      return null;

    final String value = metadata.getProperty( property );
    if( value == null )
      return null;

    return DateUtilities.parseDateTime( value );
  }

}
