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
package org.kalypso.contribs.eclipse.core.expressions;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.expressions.PropertyTester;
import org.kalypso.contribs.eclipse.osgi.FrameworkUtilities;

/**
 * A property tester for testing framework or system properties.
 */
public class FrameworkPropertyTester extends PropertyTester
{
  private static final String PROPERTY_PROPERTY = "property"; //$NON-NLS-1$

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( PROPERTY_PROPERTY.equals( property ) )
    {
      final String propertyKey = (String) args[0];
      final String value = FrameworkUtilities.getProperty( propertyKey, null );

      return ObjectUtils.equals( value, expectedValue );
    }

    return false;
  }
}
