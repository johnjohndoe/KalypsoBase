/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author vdoemming
 */
public interface IKalypsoFeatureTheme extends IKalypsoTheme, ICommandTarget
{
  /**
   * (Comma separated) qnames of the properties which determines the qname of the selectable geometry of features in
   * this theme.<br>
   * Should be interpreted by selection widgets.<br>
   * 
   * @see QName#toString()
   */
  public final static String PROPERTY_SELECTABLE_GEOMETRIES = "selectableGeometries"; //$NON-NLS-1$

  /**
   * (Comma separated) qnames of the properties that shall be painted when hovering over a feature in this theme.<br>
   * Should be interpreted by selection widgets.<br>
   * 
   * @see QName#toString()
   */
  public final static String PROPERTY_HOVER_GEOMETRIES = "hoverGeometries"; //$NON-NLS-1$

  public CommandableWorkspace getWorkspace( );

  public ISchedulingRule getSchedulingRule( );

  public IFeatureType getFeatureType( );

  public String getFeaturePath();

  public void addStyle( final IKalypsoUserStyle style );

  public void removeStyle( final IKalypsoUserStyle style );

  public UserStyle[] getStyles( );

  public FeatureList getFeatureList( );

  public FeatureList getFeatureListVisible( final GM_Envelope env );

  public IFeatureSelectionManager getSelectionManager( );

  public void paint( final double scale, final GM_Envelope bbox, final Boolean selected, final IProgressMonitor monitor, final IPaintDelegate delegate ) throws CoreException;
}
