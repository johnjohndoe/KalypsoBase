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
package org.kalypso.gml.ui.internal.coverage.imports;

import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.java.io.FilePattern;
import org.kalypso.gml.processes.tin.GmlTriangulatedSurfaceConverter;
import org.kalypso.gml.processes.tin.TriangulatedSurfaceFeature;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * Imports a .gml file as coverage.
 * 
 * @author Holger Albert
 */
public class GmlCoverageImporter extends AbstractTriangulatedSurfaceCoverageImporter
{
  @Override
  public FilePattern getFilePattern( )
  {
    return new FilePattern( "*.gml", Messages.getString("GmlCoverageImporter_0") ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  protected GM_TriangulatedSurface readInputData( final File dataFile, final String crs, final IProgressMonitor monitor ) throws CoreException, MalformedURLException
  {
    final GMLXPath gmlxPath = new GMLXPath( TriangulatedSurfaceFeature.FEATURE_TRIANGULATED_SURFACE );
    final GMLXPath sourcePath = new GMLXPath( gmlxPath, TriangulatedSurfaceFeature.MEMBER_TRIANGULATED_SURFACE );

    final GmlTriangulatedSurfaceConverter converter = new GmlTriangulatedSurfaceConverter( sourcePath );
    final GM_TriangulatedSurface gmSurface = converter.convert( dataFile.toURI().toURL(), monitor );

    return gmSurface;
  }
}