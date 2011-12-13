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
package org.kalypso.ui;

import org.kalypso.ogc.sensor.view.DiagramViewPart;
import org.kalypso.ogc.sensor.view.TableViewPart;
import org.kalypso.ui.perspectives.ModelerPerspectiveFactory;
import org.kalypso.ui.perspectives.ObservationRepositoryPerspectiveFactory;
import org.kalypso.ui.repository.view.RepositoryExplorerPart;

/**
 * Constants for the Kalypso UI. Not intended to be implemented nor extended.
 * 
 * @author schlienger
 */
public interface IKalypsoUIConstants
{
  public final static String ID_OBSDIAGRAM_VIEW = DiagramViewPart.ID;

  public final static String ID_OBSTABLE_VIEW = TableViewPart.ID;

  public final static String ID_REPOSITORY_VIEW = RepositoryExplorerPart.ID;

  public static final String MODELER_PERSPECTIVE = ModelerPerspectiveFactory.ID;

  public static final String REPOSITORY_PERSPECTIVE = ObservationRepositoryPerspectiveFactory.ID;

  /**
   * Constant for system property intended to be defined in config.ini.
   * <p>
   * if true, the pool asks to save released objects if they are dirty.
   * </p>
   */
  public static final String CONFIG_INI_DO_ASK_FOR_POOL_SAVE = "kalypso.ask_for_pool_save"; //$NON-NLS-1$

  /**
   * The property, which marks a theme as movie theme.
   */
  public static final String MOVIE_THEME_PROPERTY = "movieTheme"; //$NON-NLS-1$
}