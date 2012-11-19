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
package org.kalypso.ui.catalogs;

/**
 * Constants used in the ui-properties for qnames.
 * 
 * @author Gernot Belger
 */
public interface IFeatureTypePropertiesConstants
{
  //
  // Common feature constants
  //

  /** How many levels of children should be created if a feature is created (-1 means infinite, 0 means none) */
  String FEATURE_CREATION_DEPTH = "feature.creationDepth"; //$NON-NLS-1$

  //
  // GmlTree Constants
  //

  /** Show children of this element, defaults to true */
  String GMLTREE_SHOW_CHILDREN = "gmltree.showChildren"; //$NON-NLS-1$

  /** List of komma separated qnames; names of children that will be hidden ni the gml tree. */
  String GMLTREE_HIDDEN_CHILDREN = "gmltree.hiddenChildren"; //$NON-NLS-1$

  /** Show new menu on this feature element, defaults to true */
  String GMLTREE_NEW_MENU_ON_FEATURE = "gmltree.showNewMenuOnFeature"; //$NON-NLS-1$

  /** Show new menu of sub-features for this feature element, defaults to false */
  String GMLTREE_NEW_MENU_SHOW_SUB_FEATURES = "gmltree.showNewMenuSubFeatures"; //$NON-NLS-1$

  /** Prohibit the 'duplicate' menu of a feature, defaults to true */
  String GMLTREE_SHOW_DUPLICATION_MENU = "gmltree.showDuplicateMenu"; //$NON-NLS-1$

  //
  // Map Constants
  //

  /**
   * Extension id of a {@link org.kalypso.ogc.gml.IKalypsoThemeInfo} registered with the <code>org.kalypso.core.themeInfo</code> extension-point.
   */
  String THEME_INFO_ID = "map.themeInfoId"; //$NON-NLS-1$
}