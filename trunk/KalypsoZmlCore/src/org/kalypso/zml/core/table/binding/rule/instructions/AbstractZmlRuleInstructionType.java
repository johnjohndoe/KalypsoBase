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

import org.eclipse.core.runtime.CoreException;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.ZmlStyleResolver;
import org.kalypso.zml.core.table.schema.AbstractRuleInstructionType;
import org.kalypso.zml.core.table.schema.StyleReferenceType;

/**
 * @author Dirk Kuch
 */
public class AbstractZmlRuleInstructionType
{
  private final AbstractRuleInstructionType m_type;

  private CellStyle m_style;

  public AbstractZmlRuleInstructionType( final AbstractRuleInstructionType type )
  {
    m_type = type;
  }

  public AbstractRuleInstructionType getType( )
  {
    return m_type;
  }

  public CellStyle getStyle( ) throws CoreException
  {
    if( m_style != null )
      return m_style;

    final ZmlStyleResolver resolver = ZmlStyleResolver.getInstance();
    final StyleReferenceType styleReference = m_type.getStyleReference();

    m_style = resolver.findStyle( styleReference );
    return m_style;
  }

}
