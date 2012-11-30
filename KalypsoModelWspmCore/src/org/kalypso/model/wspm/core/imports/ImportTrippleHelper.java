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
package org.kalypso.model.wspm.core.imports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfilePointPropertyProvider;
import org.kalypso.model.wspm.core.profil.ProfileFactory;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * small refactoring - projects Lanu and nofdp uses this helper!
 * 
 * @author Dirk Kuch
 */
public final class ImportTrippleHelper
{
  private ImportTrippleHelper( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Imports the profile trippel data and converts it into IProfils
   * 
   * @param trippleFile
   *          file with profile tripples
   */
  public static IProfile[] importTrippelData( final File trippleFile, final String separator, final String profileType, final String crs ) throws CoreException
  {
    final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( profileType );

    final IComponent rechtswert = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );
    final IComponent hochwert = provider.getPointProperty( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );

    if( trippleFile == null )
      return new IProfile[0];

    /* read profiles, show warnings */
    final List<IProfile> profiles = new ArrayList<>();
    IProfile currentProfile = null;

    /* file loading */
    LineNumberReader fileReader = null;

    try( InputStreamReader inputReader = new InputStreamReader( new FileInputStream( trippleFile ) ) )
    {
      fileReader = new LineNumberReader( inputReader );

      /* File Header */
      fileReader.readLine();

      IProfileRecord lastPoint = null;
      while( fileReader.ready() )
      {
        final String line = fileReader.readLine();
        if( line == null )
        {
          break;
        }

        /* ignore empty lines */
        if( StringUtils.isBlank( line ) )
        {
          continue;
        }

        /* trippel-format should be: station, x, y, z */
        final String[] tokens = StringUtils.split( line, separator );

        /* continue just if there are enough values in the trippel file */
        if( tokens.length != 4 )
        {
          // FIXME: better error handling
          // inform the user that his profile has not enough values...
          final String message = Messages.getString( "org.kalypso.model.wspm.core.imports.ImportTrippleHelper.0", fileReader.getLineNumber() ); //$NON-NLS-1$
          final IStatus status = new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), message );
          throw new CoreException( status );
        }

        try
        {
          /* first value = profile station */
          final double station = NumberUtils.parseDouble( tokens[0] );
          final BigDecimal currentStation = ProfileUtil.stationToBigDecimal( station );

          final BigDecimal currentProfileStation = currentProfile == null ? null : ProfileUtil.stationToBigDecimal( currentProfile.getStation() );

          if( !ObjectUtils.equals( currentStation, currentProfileStation ) )
          {
            lastPoint = null;

            currentProfile = ProfileFactory.createProfil( profileType, null );

            currentProfile.setStation( station );
            currentProfile.setName( Messages.getString( "org.kalypso.model.wspm.core.imports.ImportTrippleHelper.1" ) ); //$NON-NLS-1$
            currentProfile.setDescription( Messages.getString( "org.kalypso.model.wspm.core.imports.ImportTrippleHelper.2" ) ); //$NON-NLS-1$
            currentProfile.setSrsName( crs );

            currentProfile.addPointProperty( rechtswert );
            currentProfile.addPointProperty( hochwert );

            profiles.add( currentProfile );
          }

          final IProfileRecord point = ImportTrippleHelper.createProfilePoint( currentProfile, tokens, lastPoint );
          if( point != null )
          {
            currentProfile.addPoint( point );
          }

          lastPoint = point;
        }
        catch( final NumberFormatException e )
        {
          e.printStackTrace();
          final String message = Messages.getString( "org.kalypso.model.wspm.core.imports.ImportTrippleHelper.3", fileReader.getLineNumber() ); //$NON-NLS-1$
          final IStatus status = new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), message, e );
          throw new CoreException( status );
        }
      }

      fileReader.close();
    }
    catch( final IOException e )
    {
      e.printStackTrace();

      final int lineNumber = fileReader == null ? 0 : fileReader.getLineNumber();

      final String message = Messages.getString( "org.kalypso.model.wspm.core.imports.ImportTrippleHelper.4", lineNumber ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), message, e );
      throw new CoreException( status );
    }

    return profiles.toArray( new IProfile[profiles.size()] );
  }

  /**
   * creates a new profile point and adds it to the point list of the current profile
   * 
   * @param profilPointList
   *          point list of the current profile
   * @param tokenizer
   *          holds the point data (x, y, z)
   */
  private static IProfileRecord createProfilePoint( final IProfile profile, final String[] tokens, final IProfileRecord lastPoint )
  {
    final IProfileRecord point = profile.createProfilPoint();

    /* observation of profile */
    final int iRechtswert = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );
    final int iHochwert = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );
    final int iBreite = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_BREITE );
    final int iHoehe = profile.indexOfProperty( IWspmPointProperties.POINT_PROPERTY_HOEHE );

    final double x = NumberUtils.parseDouble( tokens[1] );
    point.setValue( iRechtswert, x );

    final double y = NumberUtils.parseDouble( tokens[2] );
    point.setValue( iHochwert, y );

    final double z = NumberUtils.parseDouble( tokens[3] );
    point.setValue( iHoehe, z );

    /* calculate width coordinate */
    final double width = ImportTrippleHelper.calculateSegmentLength( x, y, lastPoint );
    point.setValue( iBreite, width );

    return point;
  }

  /**
   * calculates the width coordinate by the segment length (2-dim distance of the profile points)
   * 
   * @param profilPointList
   *          point list of the current profile
   */
  private static double calculateSegmentLength( final double xPoint, final double yPoint, final IRecord previousPoint )
  {
    if( previousPoint == null )
      return 0.0;

    /* get the segment length of the already imported profile points */
    final double previousWidth = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, previousPoint );

    /* add the segment length of the segment defined by the last imported profile point and the new to add profile point */
    final double xPrevious = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT, previousPoint );
    final double yPrevious = ProfileUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOCHWERT, previousPoint );

    final Coordinate posPrevious = new Coordinate( xPrevious, yPrevious );
    final Coordinate posPoint = new Coordinate( xPoint, yPoint );

    return previousWidth + posPrevious.distance( posPoint );
  }
}
