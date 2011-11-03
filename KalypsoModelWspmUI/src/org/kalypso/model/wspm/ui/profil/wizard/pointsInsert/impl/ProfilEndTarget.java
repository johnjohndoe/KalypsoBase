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
package org.kalypso.model.wspm.ui.profil.wizard.pointsInsert.impl;

import java.util.List;

import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.changes.PointAdd;
import org.kalypso.model.wspm.ui.profil.operation.ProfilOperation;
import org.kalypso.model.wspm.ui.profil.operation.ProfilOperationJob;
import org.kalypso.model.wspm.ui.profil.wizard.pointsInsert.AbstractPointsTarget;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

/**
 * @author kimwerner
 */
public class ProfilEndTarget extends AbstractPointsTarget
{

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.pointsInsert.IPointsTarget#insertPoints(org.kalypso.model.wspm.core.profil.impl.ProfilEventManager,
   *      IProfilPoints)
   */
  @Override
  public void insertPoints( final IProfil profile, final List<IRecord> points )
  {
    if( points != null )
      insertPointsInternal( profile, points );
    else
      addPointInternal( profile );
  }

  private final void addPointInternal( final IProfil profile )
  {
    final TupleResult result = profile.getResult();
    if( result.isEmpty() )
      return;
    final IRecord endPoint = result.get( result.size() - 1 );
    final IRecord point = result.createRecord();

    final IComponent[] properties = profile.getPointProperties();
    for( final IComponent property : properties )
    {
      final int iProp = profile.indexOfProperty( property );
      if( IWspmConstants.POINT_PROPERTY_BREITE.equals( property.getId() ) )
        point.setValue( iProp, (Double) endPoint.getValue( iProp ) + 20 );
      else if( !profile.isPointMarker( property.getId() ) )
        point.setValue( iProp, endPoint.getValue( iProp ) );
    }
    result.add( point );
  }

  public void insertPointsInternal( final IProfil profile, final List<IRecord> points )
  {
    final int pointsCount = points.size();

    final int iBreite = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_BREITE );
    final int iHoehe = profile.indexOfProperty( IWspmConstants.POINT_PROPERTY_HOEHE );

    final TupleResult owner = points.get( 0 ).getOwner();
    final int iPointsBreite = owner.indexOfComponent( IWspmConstants.POINT_PROPERTY_BREITE );
    final int iPointsHoehe = owner.indexOfComponent( IWspmConstants.POINT_PROPERTY_HOEHE );

    final IProfilChange[] changes = new IProfilChange[pointsCount];
    try
    {
      final IRecord[] existingPoints = profile.getPoints();
      final IRecord targetPkt = existingPoints.length == 0 ? profile.createProfilPoint() : existingPoints[existingPoints.length - 1];

      final double deltaX = existingPoints.length == 0 ? 0.0 : (Double) points.get( 0 ).getValue( iPointsBreite ) - (Double) targetPkt.getValue( iBreite );
      final double deltaY = existingPoints.length == 0 ? 0.0 : (Double) points.get( 0 ).getValue( iPointsHoehe ) - (Double) targetPkt.getValue( iHoehe );

      int i = changes.length - 1;
      for( final IRecord point : points )
      {
        final IRecord newPoint = targetPkt.cloneRecord();
        newPoint.setValue( iBreite, (Double) point.getValue( iPointsBreite ) - deltaX );
        newPoint.setValue( iHoehe, (Double) point.getValue( iPointsHoehe ) - deltaY );
        for( final IComponent prop : owner.getComponents() )
          if( !(IWspmConstants.POINT_PROPERTY_BREITE.equals( prop.getId() ) || IWspmConstants.POINT_PROPERTY_HOEHE.equals( prop.getId() )) )
          {

            final int index = profile.indexOfProperty( prop.getId() );
            if( index > -1 )
              newPoint.setValue( index, point.getValue( owner.indexOfComponent( prop ) ) );
          }
        changes[i--] = new PointAdd( profile, existingPoints.length == 0 ? null : targetPkt, newPoint );
      }
    }
    catch( final Exception e )
    {
      // should never happen, stops operation and raise NullPointerException in ProfilOperation.doChange
      changes[0] = null;
    }
    final ProfilOperation operation = new ProfilOperation( org.kalypso.model.wspm.ui.i18n.Messages.getString("org.kalypso.model.wspm.ui.profil.wizard.pointsInsert.impl.ProfilEndTarget.0"), profile, changes, false ); //$NON-NLS-1$
    new ProfilOperationJob( operation ).schedule();

  }
}