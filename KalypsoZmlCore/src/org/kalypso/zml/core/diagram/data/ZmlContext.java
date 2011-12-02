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
package org.kalypso.zml.core.diagram.data;

import java.net.URL;

import org.kalypso.commons.java.lang.Objects;

import de.openali.odysseus.chart.framework.model.IChartModel;

/**
 * @author Dirk Kuch
 */
public final class ZmlContext
{
  public static final String PROPERTY_CALC_CASE_CONTEXT = "calcCaseContext"; //$NON-NLS-1$

  public static final String KOD_PROPERTY_CALC_CASE_FOLDER = "{$calcCaseFolder}"; //$NON-NLS-1$

  private ZmlContext( )
  {
  }

  public static URL resolveContext( final IChartModel model, final String href, final URL context )
  {
    if( href.startsWith( KOD_PROPERTY_CALC_CASE_FOLDER ) )
    {
      return (URL) Objects.firstNonNull( model.getData( PROPERTY_CALC_CASE_CONTEXT ), context );
    }

    return context;
  }

  public static String resolvePlainHref( final String href )
  {
    if( href.startsWith( KOD_PROPERTY_CALC_CASE_FOLDER ) )
      return href.substring( KOD_PROPERTY_CALC_CASE_FOLDER.length() );

    return href;
  }
}
