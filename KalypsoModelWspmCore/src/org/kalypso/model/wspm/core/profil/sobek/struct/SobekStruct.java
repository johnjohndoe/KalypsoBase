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
package org.kalypso.model.wspm.core.profil.sobek.struct;

import java.io.PrintWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDef;
import org.kalypsodeegree.model.geometry.GM_Point;

/**
 * Represents a structure of the sobek file format.
 * 
 * @author Gernot Belger
 */
public class SobekStruct
{
  private final SobekStructDat m_structDat;

  private final SobekStructDef m_structDef;

  private final GM_Point m_location;

  public SobekStruct( final SobekStructDat structDat, final SobekStructDef structDef, final GM_Point location )
  {
    m_structDat = structDat;
    m_structDef = structDef;
    m_location = location;
  }

  public IStatus validate( )
  {
    return Status.OK_STATUS;
  }

  public SobekStructDat getDat( )
  {
    return m_structDat;
  }

  public GM_Point getLocation( )
  {
    return m_location;
  }

  public void serializeStructDat( final PrintWriter datWriter )
  {
    final String line = m_structDat.serialize();
    datWriter.println( line );
  }

  public void serializeStructDef( final PrintWriter datWriter, final PrintWriter profileDefWriter )
  {
    final String line = m_structDef.serialize();
    datWriter.println( line );

    final SobekProfileDef profileDef = m_structDef.getProfileDef();
    if( profileDef != null )
    {
      profileDef.serialize( profileDefWriter );
      profileDefWriter.println();
    }
  }

}