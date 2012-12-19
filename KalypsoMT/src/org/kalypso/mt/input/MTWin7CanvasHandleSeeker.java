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
package org.kalypso.mt.input;

import org.mt4j.util.MT4jSettings;

/**
 * @author cybernixadm
 */
public class MTWin7CanvasHandleSeeker
{
// private static final long serialVersionUID = 1L;

  private static final String dllName32 = "Win7TouchHandleSeeker"; //$NON-NLS-1$

  private static final String dllName64 = "Win7TouchHandleSeeker64"; //$NON-NLS-1$

  static
  {
    String dllName = (MT4jSettings.getInstance().getArchitecture() == MT4jSettings.ARCHITECTURE_32_BIT) ? dllName32 : dllName64;
    System.loadLibrary( dllName );
    loaded = true;
  }

  static boolean loaded = false;

// // NATIVE METHODS //
  private native int getHWND( long parentHWND );

  public native void registerTouchWindowByHwnd( long parentHWND );
  // NATIVE METHODS //

  public int findSunAwtCanvasHWND( long parentHWND )
  {
    return getHWND( parentHWND );
  }

}
