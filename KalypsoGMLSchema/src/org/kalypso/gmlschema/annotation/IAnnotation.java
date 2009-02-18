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
package org.kalypso.gmlschema.annotation;

/**
 * @author doemming
 */
public interface IAnnotation
{
  /** Constant for the name annotation element. */
  public static String ANNO_NAME = "name";

  /** Constant for the label annotation element. */
  public static String ANNO_LABEL = "label";

  /** Constant for the description annotation element. */
  public static String ANNO_DESCRIPTION = "description";

  /** Constant for the label annotation element. */
  public static String ANNO_TOOLTIP = "tooltip";

  /**
   * A label for a feature. For example the label of a tree element. Pattern replacement may take place.
   * <p>
   * Same as {@link #getValue(ANNO_LABEL)}
   * </p>
   */
  public abstract String getLabel( );

  /**
   * A (lengthy) description for a feature. For example the status line text for a selected feature. Pattern replacement
   * may take place.
   * <p>
   * Same as {@link #getValue(ANNO_DESCRIPTION)}
   * </p>
   */
  public abstract String getDescription( );

  /**
   * A tooltip for a feature. For example the status line text for a selected feature. Pattern replacement may take
   * place.
   * <p>
   * Same as {@link #getValue(ANNO_TOOLTIP)}
   * </p>
   */
  public abstract String getTooltip( );

  /**
   * Returns the value for the given key.@param element Must be one of {@link #ANNO_NAME}, {@link #ANNO_NAME},
   * {@link #ANNO_LABEL}, {@link #ANNO_TOOLTIP}
   */
  public abstract String getValue( final String element );
}