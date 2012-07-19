/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.contribs.eclipse.jface.dialog;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.internal.EclipseRCPContributionsPlugin;

/**
 * @author Dirk Kuch
 */
public class EnhancedTrayDialog extends TrayDialog
{
  public EnhancedTrayDialog( final Shell shell )
  {
    super( shell );
  }

  protected Point getScreenSize( final String parameter )
  {
    final IDialogSettings settings = EclipseRCPContributionsPlugin.getDefault().getDialogSettings();
    final String sizeString = settings.get( parameter );
    if( sizeString == null || sizeString.trim().isEmpty() )
      return new Point( 640, 480 );

    final String[] parts = sizeString.split( "," ); //$NON-NLS-1$

    return new Point( Integer.valueOf( parts[0] ), Integer.valueOf( parts[1] ) );
  }

  public void setScreenSize( final String parameter, final Point size )
  {
    final IDialogSettings settings = EclipseRCPContributionsPlugin.getDefault().getDialogSettings();
    settings.put( parameter, String.format( "%d,%d", size.x, size.y ) );
  }
}