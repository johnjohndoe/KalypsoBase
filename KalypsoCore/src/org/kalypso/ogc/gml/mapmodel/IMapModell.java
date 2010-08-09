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
package org.kalypso.ogc.gml.mapmodel;

import java.awt.Graphics;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author Gernot Belger
 */
public interface IMapModell
{
  String getLabel( );

  /**
   * Adds a listener to the list of listeners. Has no effect if the same listeners is already registered.
   */
  void addMapModelListener( final IMapModellListener l );

  /**
   * Removes a listener from the list of listeners. Has no effect if the listeners is not registered.
   */
  void removeMapModelListener( final IMapModellListener l );

  /** dispose off all themes! */
  void dispose( );

  void activateTheme( final IKalypsoTheme theme );

  IKalypsoTheme getActiveTheme( );

  void addTheme( final IKalypsoTheme theme );

  void insertTheme( final IKalypsoTheme theme, final int position );

  /**
   * Gets all themes of this model. Does NOT recurse into cascaded themes.
   */
  IKalypsoTheme[] getAllThemes( );

  /**
   * This function returns the name of the coordinate system used.
   * 
   * @return The name of the coordinate system.
   */
  String getCoordinatesSystem( );

  /**
   * Directly paints all themes contained inside this model. <br>
   * Blocks until all themes are painted. .
   */
  IStatus paint( final Graphics g, final GeoTransform p, final IProgressMonitor monitor );

  IKalypsoTheme getTheme( final int pos );

  int getThemeSize( );

  boolean isThemeActivated( final IKalypsoTheme theme );

  void moveDown( IKalypsoTheme theme );

  void moveUp( IKalypsoTheme theme );

  void removeTheme( final IKalypsoTheme theme );

  void swapThemes( IKalypsoTheme theme1, IKalypsoTheme theme2 );

  GM_Envelope getFullExtentBoundingBox( );

  IProject getProject( );

  void accept( final IKalypsoThemeVisitor visitor, int depth );

  /**
   * Iterates through all themes of this modell, starting at the given theme.
   * 
   * @see #accept(KalypsoThemeVisitor, int).
   */
  void accept( final IKalypsoThemeVisitor visitor, final int depth, final IKalypsoTheme theme );

  void setName( final I10nString name );

  I10nString getName( );

  // HACK In order to have nice parents for outline tree even for cascading themes, we something like this...
  Object getThemeParent( final IKalypsoTheme theme );

  /**
   * Internal method for setting the active theme.
   * <p>
   * This method is not intended to be called from outside of {@link IMapModell} implementations.
   */
  void internalActivate( IKalypsoTheme theme );

  /**
   * Check if this map modell is still beeing filled with themes.<br>
   * Implementors must ensure, that this flag becomes eventually <code>true</code> (even if there are errors while
   * loading).
   * 
   * @return <code>false</code> if this map modell is under construction (for example if many theme are about to be
   *         added in the near future...)
   */
  boolean isLoaded( );
}