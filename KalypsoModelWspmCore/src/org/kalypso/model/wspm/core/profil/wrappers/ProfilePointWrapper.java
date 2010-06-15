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
package org.kalypso.model.wspm.core.profil.wrappers;

import java.util.Comparator;

import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.observation.result.IRecord;

/**
 * @author Dirk Kuch
 */
public class ProfilePointWrapper extends AbstractRecordWrapper implements IRecord
{

  public static final Comparator<ProfilePointWrapper> COMPARATOR = new Comparator<ProfilePointWrapper>()
  {

    @Override
    public int compare( final ProfilePointWrapper o1, final ProfilePointWrapper o2 )
    {
      final double b1 = o1.getBreite();
      final double b2 = o2.getBreite();

      return Double.valueOf( b1 ).compareTo( Double.valueOf( b2 ) );
    }
  };

  public ProfilePointWrapper( final IRecord record )
  {
    super( record );
  }

  public double getHoehe( )
  {
    return ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_HOEHE, this );
  }

  public double getBreite( )
  {

    return ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_BREITE, this );
  }

  public double getHochwert( )
  {

    return ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_HOCHWERT, this );
  }

  public double getRechtswert( )
  {
    return ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_RECHTSWERT, this );
  }

  public void setBreite( final double width )
  {
    final int index = findComponent( IWspmConstants.POINT_PROPERTY_BREITE );
    getRecord().setValue( index, Double.valueOf( width ) );

  }

  public void setHoehe( final double hoehe )
  {
    final int index = findComponent( IWspmConstants.POINT_PROPERTY_HOEHE );
    getRecord().setValue( index, Double.valueOf( hoehe ) );
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return String.format( "[%.2f, %.2f]", getBreite(), getHoehe() ); //$NON-NLS-1$
  }

  public void setKsValue( final Double ksValue )
  {
    final int index = findComponent( IWspmConstants.POINT_PROPERTY_RAUHEIT_KS );
    if( index < 0 )
      return;

    getRecord().setValue( index, ksValue );
  }

  public void setKstValue( final Double kstValue )
  {
    final int index = findComponent( IWspmConstants.POINT_PROPERTY_RAUHEIT_KST );
    if( index < 0 )
      return;

    getRecord().setValue( index, kstValue );
  }

  public double getKsValue( )
  {
    return ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_RAUHEIT_KS, this );
  }

  public double getKstValue( )
  {
    return ProfilUtil.getDoubleValueFor( IWspmConstants.POINT_PROPERTY_RAUHEIT_KST, this );
  }

}
