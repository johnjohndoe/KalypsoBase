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
package org.kalypso.ogc.gml.featureview.dialog;

import java.util.Collection;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.transformation.transformer.GeoTransformerFactory;
import org.kalypso.transformation.transformer.IGeoTransformer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * This class opens a point editing dialog.
 * 
 * @author Holger Albert
 */
public class PointFeatureDialog implements IFeatureDialog
{
  private FeatureChange m_change = null;

  private final Feature m_feature;

  private final IValuePropertyType m_ftp;

  public PointFeatureDialog( final Feature feature, final IValuePropertyType ftp )
  {
    m_feature = feature;
    m_ftp = ftp;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#open(org.eclipse.swt.widgets.Shell)
   */
  @Override
  public int open( final Shell shell )
  {
    final GM_Point point = (GM_Point) m_feature.getProperty( m_ftp );

    final String kalypsoCrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    final String dlgCrs = point == null ? kalypsoCrs : point.getCoordinateSystem();
    final PointDialog dialog = new PointDialog( shell, point.getAsArray(), dlgCrs );

    final int open = dialog.open();

    if( open == Window.OK )
    {
      final double[] values = dialog.getValues();
      final GM_Position newPos = GeometryFactory.createGM_Position( values );
      final String newCrs = dialog.getCS_CoordinateSystem();

      final IGeoTransformer geoTransformer = GeoTransformerFactory.getGeoTransformer( kalypsoCrs );
      try
      {
        // REMARK: enw geometries MUST always be created in the current Kalypso coordinate system
        final GM_Position newPosTransformed = geoTransformer.transform( newPos, newCrs );
        final GM_Point newPoint = GeometryFactory.createGM_Point( newPosTransformed, kalypsoCrs );

        m_change = new FeatureChange( m_feature, m_ftp, newPoint );
      }
      catch( final Exception e )
      {
        e.printStackTrace();
        final String titel = Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.PointDialog.data" ); //$NON-NLS-1$
        final String message = e.toString();
        ErrorDialog.openError( shell, titel, message, null );
      }
    }

    return open;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#collectChanges(java.util.Collection)
   */
  @Override
  public void collectChanges( final Collection<FeatureChange> c )
  {
    if( c != null && m_change != null )
      c.add( m_change );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.IFeatureDialog#getLabel()
   */
  @Override
  public String getLabel( )
  {
    return Messages.getString( "org.kalypso.ogc.gml.featureview.dialog.PointFeatureDialog.values" ); //$NON-NLS-1$
  }
}