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
package de.openali.odysseus.chart.framework.model.mapper.impl;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.util.resource.IPair;
import de.openali.odysseus.chart.framework.util.resource.Pair;

/**
 * @author alibu
 */
public class CoordinateMapper<T_Domain, T_Target> implements ICoordinateMapper<T_Domain, T_Target>
{
  private final IAxis<T_Domain> m_domainAxis;

  private final IAxis<T_Target> m_targetAxis;

 // private final ORIENTATION m_ori;

  //private final DataOperatorHelper m_doh = new DataOperatorHelper();

  public CoordinateMapper( final IAxis<T_Domain> domain, final IAxis<T_Target> target )
  {
    m_domainAxis = domain;
    m_targetAxis = target;
 //   m_ori = m_domainAxis.getPosition().getOrientation();
  }

//  @Override
//  public IDataOperator< ? > getDataOperator( final Class< ? > clazz )
//  {
//    return m_doh.getDataOperator( clazz );
//  }

  @Override
  public Point numericToScreen( final Double domVal, final Double targetVal )
  {
    final int domScreen = m_domainAxis.numericToScreen( domVal );
    final int valScreen = m_targetAxis.numericToScreen( targetVal );
    return new Point( domScreen, valScreen );
   // final Point unswitched = new Point( domScreen, valScreen );
    // Koordinaten switchen
   // return new Point( m_ori.getX( unswitched ), m_ori.getY( unswitched ) );
  }

  @Override
  public IAxis<T_Domain> getDomainAxis( )
  {
    return m_domainAxis;
  }

  @Override
  public IAxis<T_Target> getTargetAxis( )
  {
    return m_targetAxis;
  }

  @Override
  public Point logicalToScreen( final T_Domain domainValue, final T_Target targetValue )
  {
    return numericToScreen( m_domainAxis.logicalToNumeric( domainValue ), m_targetAxis.logicalToNumeric( targetValue ) );
  }

  @Override
  public IPair<Double, Double> screenToNumeric( final Point screenValue )
  {
    if( screenValue == null )
      return null;

  //  final int domainScreen = getDomainAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) ? screenValue.x : screenValue.y;
  //  final int targetScreen = getTargetAxis().getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) ? screenValue.x : screenValue.y;

    final Double domainNum = getDomainAxis().screenToNumeric( screenValue.x );
    final Double targetNum = getTargetAxis().screenToNumeric( screenValue.y );
    return new Pair<>( domainNum, targetNum );
  }

  @Override
  public Point getScreenSize( )
  {
    final IAxis<T_Domain> domainAxis = getDomainAxis();
    final IAxis<T_Target> targetAxis = getTargetAxis();

    return new Point( domainAxis.getScreenHeight(), targetAxis.getScreenHeight() );
  }

  @Override
  public IPair<T_Domain, T_Target> screenToLogical( Point screenValue )
  {
    return new Pair<>( getDomainAxis().screenToLogical( screenValue.x ), getTargetAxis().screenToLogical( screenValue.y ) );
  }
}