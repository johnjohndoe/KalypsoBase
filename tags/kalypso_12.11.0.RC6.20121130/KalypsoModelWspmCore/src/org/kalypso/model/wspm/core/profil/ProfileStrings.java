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
package org.kalypso.model.wspm.core.profil;

import org.kalypso.model.wspm.core.i18n.Messages;

/**
 * Some common string constants
 * 
 * @author Gernot Belger
 */
public interface ProfileStrings
{
  String PROFILE_OBJECT_RECORD_WIDTH_LABEL = Messages.getString( "ProfileStrings_0" ); //$NON-NLS-1$

  String PROFILE_OBJECT_RECORD_WIDTH_UNIT = "m"; //$NON-NLS-1$

  String PROFILE_OBJECT_RECORD_HEIGHT_LABEL = Messages.getString( "ProfileStrings_2" ); //$NON-NLS-1$

  String PROFILE_OBJECT_RECORD_HEIGHT_UNIT = "mNN"; //$NON-NLS-1$

  String PROFILE_OBJECT_RECORD_CODE_LABEL = Messages.getString( "ProfileStrings_4" ); //$NON-NLS-1$
}