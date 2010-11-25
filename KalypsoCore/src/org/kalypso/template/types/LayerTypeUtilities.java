/*--------------- Kalypso-Header ------------------------------------------

This file is part of kalypso.
Copyright (C) 2004, 2005 by:

Technical University Hamburg-Harburg (TUHH)
Institute of River and coastal engineering
Denickestr. 22
21073 Hamburg, Germany
http://www.tuhh.de/wb

and

Bjoernsen Consulting Engineers (BCE)
Maria Trost 3
56070 Koblenz, Germany
http://www.bjoernsen.de

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Contact:

E-Mail:
belger@bjoernsen.de
schlienger@bjoernsen.de
v.doemming@tuhh.de

--------------------------------------------------------------------------*/

package org.kalypso.template.types;

import org.eclipse.core.internal.resources.PlatformURLResourceConnection;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;

/**
 * Utilities for the (xml-binding-)class {@link LayerType}.
 * 
 * @author bce
 */
@SuppressWarnings("restriction")//$NON-NLS-1$
public final class LayerTypeUtilities
{
  private static final String TYPE_SHAPE = "shape"; //$NON-NLS-1$

  private static final String TYPE_GML = "gml"; //$NON-NLS-1$

  private static final String EXT_SHP = "shp"; //$NON-NLS-1$

  private static final String EXT_GMLZ = "gmlz"; //$NON-NLS-1$

  private static final String EXT_GML = "gml"; //$NON-NLS-1$


  private LayerTypeUtilities( )
  {
  }

  public static void initLayerType( final LayerType layer, final IFile file ) throws CoreException
  {
    final IPath projectRelativePath = file.getProjectRelativePath();

    final String fileext = projectRelativePath.getFileExtension().toLowerCase();
    final String contentType;

    final String projectURL = PlatformURLResourceConnection.RESOURCE_URL_STRING + "/" + file.getProject().getName() + "/"; //$NON-NLS-1$ //$NON-NLS-2$

    final String href;
    if( EXT_GML.equals( fileext ) || EXT_GMLZ.equals( fileext ) )
    {
      href = projectURL + projectRelativePath;
      contentType = TYPE_GML;
    }
    else if( EXT_SHP.equalsIgnoreCase( fileext ) )
    {
      contentType = TYPE_SHAPE;
      href = projectURL + projectRelativePath.removeFileExtension();
    }
    else
    {
      final String msg = Messages.getString( "org.kalypso.template.types.LayerTypeUtilities.8" ) + fileext; //$NON-NLS-1$
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), msg ) );
    }

    layer.setId( file.getName() );
    layer.setFeaturePath( "" ); //$NON-NLS-1$
    layer.setHref( href );
    layer.setLinktype( contentType );
    layer.setType( "simple" ); //$NON-NLS-1$
  }
}
