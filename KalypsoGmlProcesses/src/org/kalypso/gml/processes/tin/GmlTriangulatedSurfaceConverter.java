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
package org.kalypso.gml.processes.tin;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.gml.processes.KalypsoGmlProcessesPlugin;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * @author Holger Albert
 */
public class GmlTriangulatedSurfaceConverter extends AbstractTriangulatedSurfaceConverter
{
  private final GMLXPath m_sourcePath;

  public GmlTriangulatedSurfaceConverter( final GMLXPath sourcePath )
  {
    m_sourcePath = sourcePath;
  }

  @Override
  public GM_TriangulatedSurface convert( final URL sourceLocation, IProgressMonitor monitor ) throws CoreException
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( Messages.getString("GmlTriangulatedSurfaceConverter_0"), 100 ); //$NON-NLS-1$

      // REMARK 1: Loads the source tin directly into memory... will bring performance problems...
      final GMLWorkspace sourceWorkspace = GmlSerializer.createGMLWorkspace( sourceLocation, null );
      final Object sourceObject = m_sourcePath == null ? sourceWorkspace : GMLXPathUtilities.query( m_sourcePath, sourceWorkspace );
      final GM_TriangulatedSurface surface = findSurface( sourceObject );
      if( surface == null )
      {
        final String msg = String.format( Messages.getString("GmlTriangulatedSurfaceConverter_1"), m_sourcePath, sourceObject ); //$NON-NLS-1$
        final IStatus status = new Status( IStatus.ERROR, KalypsoGmlProcessesPlugin.PLUGIN_ID, msg );
        throw new CoreException( status );
      }

      // REMARK 2: Cloning the complete tin will result in performance problems...
      final GM_TriangulatedSurface clonedSurface = (GM_TriangulatedSurface)surface.clone();

      /* Monitor. */
      monitor.worked( 100 );

      return clonedSurface;
    }
    catch( final CoreException ex )
    {
      throw ex;
    }
    catch( final Exception ex )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGmlProcessesPlugin.PLUGIN_ID, ex.getLocalizedMessage(), ex ) );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  private GM_TriangulatedSurface findSurface( final Object sourceObject )
  {
    if( sourceObject == null )
      return null;

    if( sourceObject instanceof GM_TriangulatedSurface )
      return (GM_TriangulatedSurface)sourceObject;

    /* Check, if it is a feature or take the root feature if it is a workspace. */
    final Feature feature = getFeature( sourceObject );
    if( feature == null )
      return null;

    /* Get first tin we find as property. */
    final IPropertyType[] properties = feature.getFeatureType().getProperties();
    for( final IPropertyType pt : properties )
    {
      final Object property = feature.getProperty( pt );
      if( property instanceof GM_TriangulatedSurface )
        return (GM_TriangulatedSurface)property;
    }

    return null;
  }

  private Feature getFeature( final Object sourceObject )
  {
    if( sourceObject instanceof Feature )
      return (Feature)sourceObject;

    if( sourceObject instanceof GMLWorkspace )
      return ((GMLWorkspace)sourceObject).getRootFeature();

    return null;
  }
}