/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.commons.databinding.conversion;

import java.util.Map;

/**
 * A {@link org.eclipse.core.databinding.conversion.IConverter} implementation based on a given mapping.
 *
 * @author Gernot Belger
 */
public class MapConverter<FROM, TO> extends TypedConverter<FROM, TO>
{
  private final Map<FROM, TO> m_mapping;

  private final ITypedConverter<FROM, TO> m_missingElementConverter;

  /**
   * @param missingElementConverter
   *          If the fromObject is not contained in mapping, this converter is used to convert it to the to type. If <code>null</code>, instead an {@link IllegalArgumentException} will be thrown.
   */
  public MapConverter( final Class<FROM> fromType, final Class<TO> toType, final Map<FROM, TO> mapping, final ITypedConverter<FROM, TO> missingElementConverter )
  {
    super( fromType, toType );

    m_mapping = mapping;
    m_missingElementConverter = missingElementConverter;
  }

  @Override
  public TO convertTyped( final FROM fromObject )
  {
    if( m_mapping.containsKey( fromObject ) )
      return m_mapping.get( fromObject );

    if( m_missingElementConverter == null )
      throw new IllegalArgumentException( "fromObject missing in mapping" ); //$NON-NLS-1$

    return m_missingElementConverter.convert( fromObject );
  }
}