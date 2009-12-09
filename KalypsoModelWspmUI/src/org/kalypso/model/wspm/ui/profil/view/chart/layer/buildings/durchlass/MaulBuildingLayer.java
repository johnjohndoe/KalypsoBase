/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.model.wspm.ui.profil.view.chart.layer.buildings.durchlass;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.model.wspm.core.profil.IProfilBuilding;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.ProfilDataException;
import org.kalypso.model.wspm.core.profil.IProfilBuilding.BUILDING_PROPERTY;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.ui.profil.view.chart.ProfilChartView;
import org.kalypso.model.wspm.ui.profil.view.chart.layer.buildings.AbstractBuildingLayer;

import de.belger.swtchart.axis.AxisRange;

/**
 * @author kimwerner
 * 
 */
public class MaulBuildingLayer extends AbstractBuildingLayer
{
  public MaulBuildingLayer( final ProfilChartView pvp,
      final AxisRange domainRange, final AxisRange valueRange, final Color color )
  {
    super( pvp, domainRange, valueRange, color );
  }
 

  /**
   * @see de.belger.swtchart.layer.IChartLayer#getBounds()
   */
  public Rectangle2D getBounds( )
  {
    try
    {
      return createOval();
    }
    catch( Exception e )
    {
      return new Rectangle2D.Double( Double.NaN, Double.NaN, Double.NaN,
          Double.NaN );
    }
  }

  /**
   * @see de.belger.swtchart.layer.IChartLayer#paintLegend(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper)
   */
  @Override
  public void paintLegend( final GCWrapper gc )
  {
    final Rectangle clipping = gc.getClipping();
    
    final Color background = gc.getBackground();
    
    gc.setBackground( getColor() );
    gc.fillOval( clipping.x + 2, clipping.y + 7, clipping.width - 4, clipping.height - 14 );
    gc.drawOval( clipping.x + 2, clipping.y + 7, clipping.width - 4, clipping.height - 14 );
    
    gc.setBackground( background );
  }

  /**
   * @see de.belger.swtchart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper)
   */
  public void paint( final GCWrapper gc )
  {
    try
    {
      final Color background = gc.getBackground();
      gc.setBackground( getColor() );

      final Rectangle2D oval = createOval();

      final Rectangle ovalScreen = logical2screen( oval );
      gc.fillOval( ovalScreen.x, ovalScreen.y, ovalScreen.width,
          ovalScreen.height );
    //  gc.drawOval( ovalScreen.x, ovalScreen.y, ovalScreen.width,
    //      ovalScreen.height );

      gc.setBackground( background );
    }
    catch( final ProfilDataException e )
    {
      // sollte nie passieren
      e.printStackTrace();
      throw new IllegalStateException( e );
    }
  }

  private Rectangle2D createOval( ) throws ProfilDataException
  {
    final IProfilBuilding building = getBuilding();
    final double bezX = (Double)building
        .getValueFor( BUILDING_PROPERTY.BEZUGSPUNKT_X );
    final double bezY = (Double)building
        .getValueFor( BUILDING_PROPERTY.BEZUGSPUNKT_Y );
    final double durchmesser = (Double)building
        .getValueFor( BUILDING_PROPERTY.BREITE );
    final Point2D topLeft = new Point2D.Double( bezX - durchmesser / 2, bezY);
    final double w = durchmesser;
    final double h = (Double)building
    .getValueFor( BUILDING_PROPERTY.HOEHE );
    final Rectangle2D oval = new Rectangle2D.Double( topLeft.getX(), topLeft
        .getY(), w, h );
    return oval;
  }
  @Override
  public String toString( )
  {
    return "Maulprofil";
  }


  /**
   * @see com.bce.profil.ui.view.chart.layer.AbstractProfilChartLayer#editProfil(org.eclipse.swt.graphics.Point, java.lang.Object)
   */
  @Override
  protected void editProfil( Point point, Object data )
  {
    
  }


  /**
   * @see com.bce.eind.core.profil.IProfilListener#onProfilChanged(com.bce.eind.core.profil.changes.ProfilChangeHint, com.bce.eind.core.profil.IProfilChange[])
   */
  @Override
  public void onProfilChanged( ProfilChangeHint hint, IProfilChange[] changes )
  {
    
  }
 
}
