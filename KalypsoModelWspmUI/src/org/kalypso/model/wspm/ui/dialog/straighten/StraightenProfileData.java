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
package org.kalypso.model.wspm.ui.dialog.straighten;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.ui.action.ProfilesSelection;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_AMOUNT;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_ENABLEMENT;

import com.vividsolutions.jts.geom.Point;

/**
 * Contains configuration data for the {@link StraightenProfileOperation}.
 * 
 * @author Holger Albert
 */
public class StraightenProfileData extends AbstractModelObject
{
  static final String PROPERTY_CORRECT_POINTS_ENABLEMENT = "correctPointsEnablement"; //$NON-NLS-1$

  static final String PROPERTY_CORRECT_POINTS_AMOUNT = "correctPointsAmount"; //$NON-NLS-1$

  static final String PROPERTY_CORRECT_POINTS_AMOUNT_ENABLED = "correctPointsAmountEnabled"; //$NON-NLS-1$

  /**
   * The selection, which contains the profile.
   */
  private final ProfilesSelection m_profileSelection;

  /**
   * The profile.
   */
  private final IProfileFeature m_profile;

  /**
   * The first point.
   */
  private final Point m_firstPoint;

  /**
   * The second point.
   */
  private final Point m_secondPoint;

  /**
   * The width in the profile of the first point.
   */
  private final double m_firstWidth;

  /**
   * The width in the profile of the second point.
   */
  private final double m_secondWidth;

  /**
   * The option, if the width of the profile points should be adjusted.
   */
  private CORRECT_POINTS_ENABLEMENT m_correctPointsEnablement;

  /**
   * The option, how many profile points should be adjusted.
   */
  private CORRECT_POINTS_AMOUNT m_correctPointsAmount;

  /**
   * The constructor.
   * 
   * @param profileSelection
   *          The selection, which contains the profile.
   * @param profile
   *          The profile.
   * @param firstPoint
   *          The first point.
   * @param secondPoint
   *          The second point.
   * @param firstWidth
   *          The width in the profile of the first point.
   * @param secondWidth
   *          The width in the profile of the second point.
   */
  public StraightenProfileData( final ProfilesSelection profileSelection, final IProfileFeature profile, final Point firstPoint, final Point secondPoint, final double firstWidth, final double secondWidth )
  {
    m_profileSelection = profileSelection;
    m_profile = profile;
    m_firstPoint = firstPoint;
    m_secondPoint = secondPoint;
    m_firstWidth = firstWidth;
    m_secondWidth = secondWidth;
    m_correctPointsEnablement = CORRECT_POINTS_ENABLEMENT.ON;
    m_correctPointsAmount = CORRECT_POINTS_AMOUNT.ALL;
  }

  /**
   * This function returns the selection, which contains the profile.
   * 
   * @return The selection, which contains the profile.
   */
  public ProfilesSelection getProfileSelection( )
  {
    return m_profileSelection;
  }

  /**
   * This function returns the profile.
   * 
   * @return The profile.
   */
  public IProfileFeature getProfile( )
  {
    return m_profile;
  }

  /**
   * This function returns the first point.
   * 
   * @return The first point.
   */
  public Point getFirstPoint( )
  {
    return m_firstPoint;
  }

  /**
   * This function returns the second point.
   * 
   * @return The second point.
   */
  public Point getSecondPoint( )
  {
    return m_secondPoint;
  }

  /**
   * This function returns the width in the profile of the first point.
   * 
   * @return The width in the profile of the first point.
   */
  public double getFirstWidth( )
  {
    return m_firstWidth;
  }

  /**
   * This function returns the width in the profile of the second point.
   * 
   * @return The width in the profile of the second point.
   */
  public double getSecondWidth( )
  {
    return m_secondWidth;
  }

  /**
   * This function returns the option, if the width of the profile points should be adjusted.
   * 
   * @return The option, if the width of the profile points should be adjusted.
   */
  public CORRECT_POINTS_ENABLEMENT getCorrectPointsEnablement( )
  {
    return m_correctPointsEnablement;
  }

  /**
   * This function returns the option, how many profile points should be adjusted.
   * 
   * @return The option, how many profile points should be adjusted.
   */
  public CORRECT_POINTS_AMOUNT getCorrectPointsAmount( )
  {
    return m_correctPointsAmount;
  }

  public void loadSettings( final IDialogSettings settings )
  {
    final String correctPointsEnablement = settings.get( PROPERTY_CORRECT_POINTS_ENABLEMENT );
    final String correctPointsAmount = settings.get( PROPERTY_CORRECT_POINTS_AMOUNT );

    if( correctPointsEnablement != null && correctPointsEnablement.length() > 0 )
      m_correctPointsEnablement = CORRECT_POINTS_ENABLEMENT.valueOf( correctPointsEnablement );

    if( correctPointsAmount != null && correctPointsAmount.length() > 0 )
      m_correctPointsAmount = CORRECT_POINTS_AMOUNT.valueOf( correctPointsAmount );
  }

  public void storeSettings( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    settings.put( PROPERTY_CORRECT_POINTS_ENABLEMENT, m_correctPointsEnablement.name() );
    settings.put( PROPERTY_CORRECT_POINTS_AMOUNT, m_correctPointsAmount.name() );
  }

  public boolean getCorrectPointsAmountEnabled( )
  {
    return m_correctPointsEnablement == CORRECT_POINTS_ENABLEMENT.ON;
  }

  public void setCorrectPointsEnablement( final CORRECT_POINTS_ENABLEMENT correctPointsEnablement )
  {
    final Object oldValue = m_correctPointsEnablement;
    final boolean oldEnablement = getCorrectPointsAmountEnabled();

    m_correctPointsEnablement = correctPointsEnablement;

    firePropertyChange( PROPERTY_CORRECT_POINTS_ENABLEMENT, oldValue, correctPointsEnablement );
    firePropertyChange( PROPERTY_CORRECT_POINTS_AMOUNT_ENABLED, oldEnablement, getCorrectPointsAmountEnabled() );
  }

  public void setCorrectPointsAmount( final CORRECT_POINTS_AMOUNT correctPointsAmount )
  {
    final Object oldValue = m_correctPointsAmount;

    m_correctPointsAmount = correctPointsAmount;

    firePropertyChange( PROPERTY_CORRECT_POINTS_AMOUNT, oldValue, correctPointsAmount );
  }
}