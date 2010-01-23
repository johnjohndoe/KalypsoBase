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
package org.kalypso.simulation.ui.ant;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.simulation.core.ant.DateFeature2ZmlMapping;
import org.kalypso.simulation.core.ant.Feature2ZmlMapping;
import org.kalypso.simulation.core.ant.MapFeature2ZmlMetaVisitor;
import org.kalypsodeegree.model.feature.FeatureVisitor;
/**
 * Reads data from a zml (linked into the visited features) and puts it as property into the same feature.
 * 
 * @see org.kalypso.ogc.util.MapZmlMeta2FeatureVisitor
 * 
 * @author belger
 */
public class MapFeature2ZmlMetaTask extends AbstractFeatureVisitorTask
{
  /** FeatureProperty which holds the Zml-Link */
  private String m_zmlLink;
  
  /** List of mappings to perform */
  private final List<Feature2ZmlMapping> m_mappings = new ArrayList<Feature2ZmlMapping>( 5 );

  public MapFeature2ZmlMetaTask( )
  {
    super( false );
  }
  
  public final void setZmlLink( final String zmlLink )
  {
    m_zmlLink = zmlLink;
  }
  
  public final void addConfiguredMapping( final Feature2ZmlMapping mapping )
  {
    m_mappings.add( mapping ); 
  }

  public final void addConfiguredDateMapping( final DateFeature2ZmlMapping mapping )
  {
    m_mappings.add( mapping );
  }
  
  /**
   * @see org.kalypso.ant.AbstractFeatureVisitorTask#createVisitor(java.net.URL, org.kalypso.contribs.java.net.IUrlResolver, org.kalypso.contribs.java.util.logging.ILogger, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public final FeatureVisitor createVisitor( final URL context, final ILogger logger )
  {
    return new MapFeature2ZmlMetaVisitor( context, m_zmlLink, m_mappings.toArray( new Feature2ZmlMapping[m_mappings.size()] ) );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.IErrorHandler#handleError(org.eclipse.swt.widgets.Shell,
   *      org.eclipse.core.runtime.IStatus)
   */
  public void handleError( final Shell shell, final IStatus status )
  {
    ErrorDialog.openError( shell, "MapFeature2ZmlMeta", "Fehler beim Erzeugen der ZML-Metadaten.", status );
  }
}
