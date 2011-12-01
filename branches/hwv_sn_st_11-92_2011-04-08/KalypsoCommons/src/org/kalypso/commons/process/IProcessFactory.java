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
package org.kalypso.commons.process;

import java.io.IOException;

/**
 * Factory for creating new {@link IProcess} objects.
 * 
 * @author Gernot Belger
 */
public interface IProcessFactory
{
  /**
   * Id of the default (local) process factory implementation. <br>
   * This id should be used as fall-back if no other factory is specified.
   */
  final String DEFAULT_PROCESS_FACTORY_ID = "org.kalypso.commons.process.defaultProcessFactory"; //$NON-NLS-1$

  /**
   * Creates a new {@link IProcess}.
   * 
   * @param tempDirName
   *          The name of the working directory of the process. The process will create a sandbox directory in a place
   *          it finds suitable using this name. All necessary files should be copied into this sandbox including the
   *          executable file if needed.
   * @param executeable
   *          The location of the executable file relative to the sandbox.
   * @param commandlineArgs
   *          Command line arguments passed to the executable.
   */
  IProcess newProcess( final String tempDirName, final String executeable, final String... commandlineArgs ) throws IOException;

}
