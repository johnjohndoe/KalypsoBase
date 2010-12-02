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
package org.kalypso.zml.ui.table.binding;

import jregex.Pattern;
import jregex.RETokenizer;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataBoundary;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.schema.RuleInstruction;
import org.kalypso.zml.ui.table.schema.RuleInstructionOperator;
import org.kalypso.zml.ui.table.schema.StyleReferenceType;
import org.kalypso.zml.ui.table.styles.ZmlStyleResolver;

/**
 * @author Dirk Kuch
 */
public class ZmlRuleInstruction
{
  private final RuleInstruction m_type;

  private CellStyle m_style;

  public ZmlRuleInstruction( final RuleInstruction type )
  {
    m_type = type;
  }

  public MetadataBoundary matches( final IZmlValueReference reference ) throws SensorException
  {
    final MetadataList metadata = reference.getMetadata();
    final IAxis valueAxis = reference.getValueAxis();
    final Number value = (Number) reference.getValue();

    final String[] keys = MetadataBoundary.findBoundaryKeys( metadata, m_type.getMetadataKey(), valueAxis.getType() );

    final MetadataBoundary[] boundaries = MetadataBoundary.getBoundaries( metadata, keys );
    for( final MetadataBoundary boundary : boundaries )
    {
      if( matches( boundary, value ) )
        return boundary;
    }

    return null;
  }

  private boolean matches( final MetadataBoundary metadataBoundary, final Number value )
  {
    final double source = value.doubleValue();
    final double boundary = metadataBoundary.getValue();

    final RuleInstructionOperator operator = m_type.getOperator();
    if( RuleInstructionOperator.SMALLER.equals( operator ) )
    {
      return source < boundary;
    }
    else if( RuleInstructionOperator.SMALLER_EQUAL.equals( operator ) )
    {
      return source <= boundary;
    }
    else if( RuleInstructionOperator.EQUAL.equals( operator ) )
    {
      return source == boundary;
    }
    else if( RuleInstructionOperator.GREATER.equals( operator ) )
    {
      return source > boundary;
    }
    else if( RuleInstructionOperator.GREATER_EQUAL.equals( operator ) )
    {
      return source >= boundary;
    }

    return false;
  }

  public String update( final MetadataBoundary boundary, final String text )
  {
    final RETokenizer tokenizer = new RETokenizer( new Pattern( m_type.getLabelTokenizer() ), boundary.getType() );
    final String value = tokenizer.nextToken();

    final String instruction = String.format( m_type.getLabel(), value );

    return String.format( "%s   %s", instruction, text );
  }

  public CellStyle getStyle( ) throws CoreException
  {
    if( m_style != null )
      return m_style;

    final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();
    final StyleReferenceType styleReference = m_type.getStyleReference();
    if( styleReference != null )
      m_style = resolver.findStyle( styleReference );

    return null;
  }

}
