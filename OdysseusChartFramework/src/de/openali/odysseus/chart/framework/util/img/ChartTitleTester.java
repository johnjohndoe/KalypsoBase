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
package de.openali.odysseus.chart.framework.util.img;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;

public class ChartTitleTester
{
  final public static TitleTypeBean[] getTitleTypes( )
  {
    final TitleTypeBean[] types = new TitleTypeBean[9];
    types[0] = new TitleTypeBean( "-------------------topLeft---------------------" ); //$NON-NLS-1$
    types[0].setRotation( -90 );
    types[0].setTextAnchorX( ALIGNMENT.RIGHT );

    types[1] = new TitleTypeBean( "-------------------topCenter-------------------" ); //$NON-NLS-1$
    types[1].setPositionHorizontal( ALIGNMENT.CENTER );
    types[1].setTextAnchorX( ALIGNMENT.CENTER );
    types[1].setTextAnchorY( ALIGNMENT.TOP );

    types[2] = new TitleTypeBean( "-------------------topRight--------------------" ); //$NON-NLS-1$
    types[2].setPositionHorizontal( ALIGNMENT.RIGHT );
    types[2].setTextAnchorX( ALIGNMENT.RIGHT );
    types[2].setTextAnchorY( ALIGNMENT.TOP );

    types[3] = new TitleTypeBean( "-------------------centerLeft---------------------" ); //$NON-NLS-1$
    types[3].setPositionVertical( ALIGNMENT.CENTER );
    types[3].setTextAnchorY( ALIGNMENT.CENTER );

    types[4] = new TitleTypeBean( "-------------------topCenter2-------------------" ); //$NON-NLS-1$
    types[4].setPositionHorizontal( ALIGNMENT.CENTER );
    types[4].setPositionVertical( ALIGNMENT.TOP );
    types[4].setTextAnchorX( ALIGNMENT.CENTER );
    types[4].setTextAnchorY( ALIGNMENT.TOP );

    types[5] = new TitleTypeBean( "-------------------centerCenter--------------------" ); //$NON-NLS-1$
    types[5].setPositionHorizontal( ALIGNMENT.CENTER );
    types[5].setPositionVertical( ALIGNMENT.CENTER );
    types[5].setTextAnchorX( ALIGNMENT.CENTER );
    types[5].setTextAnchorY( ALIGNMENT.TOP );
    types[5].getTextStyle().setHeight( 15 );

    return types;
  }
}
