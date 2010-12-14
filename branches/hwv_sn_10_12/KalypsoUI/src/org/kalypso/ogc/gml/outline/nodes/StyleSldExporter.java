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
package org.kalypso.ogc.gml.outline.nodes;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
public class StyleSldExporter
{
  private final FeatureTypeStyle[] m_ftStyles;

  private final UserStyle[] m_userStyles;

  private final String m_parentTitle;

  public StyleSldExporter( final IKalypsoStyle[] styles, final String parentTitle )
  {
    m_parentTitle = parentTitle;
    final List<UserStyle> userStyles = new ArrayList<UserStyle>();
    final List<FeatureTypeStyle> ftStyles = new ArrayList<FeatureTypeStyle>();

    for( final IKalypsoStyle style : styles )
    {
      if( style instanceof UserStyle )
        userStyles.add( (UserStyle) style );

      if( style instanceof FeatureTypeStyle )
        ftStyles.add( (FeatureTypeStyle) style );
    }

    m_ftStyles = ftStyles.toArray( new FeatureTypeStyle[ftStyles.size()] );
    m_userStyles = userStyles.toArray( new UserStyle[userStyles.size()] );
  }

  public Marshallable createMarshallable( )
  {
    if( m_userStyles.length == 0 )
    {
      if( m_ftStyles.length == 0 )
        return null;

      if( m_ftStyles.length == 1 )
        return m_ftStyles[0];

      return (Marshallable) StyleFactory.createUserStyle( "userStyle", m_parentTitle, formatExportAbstract(), false, m_ftStyles );
    }

    /** A bit hack: we put all feature type styles into one sub-user-style */
    final UserStyle[] userStyles;
    if( m_ftStyles.length == 0 )
      userStyles = m_userStyles;
    else
    {
      final UserStyle userStyle = StyleFactory.createUserStyle( "topLevelFeatureTypeStyles", m_parentTitle, formatExportAbstract(), false, m_ftStyles );
      userStyles = new UserStyle[m_userStyles.length + 1];
      userStyles[0] = userStyle;
      System.arraycopy( m_userStyles, 0, userStyles, 1, m_userStyles.length );
    }

    if( userStyles.length == 1 )
      return (Marshallable) userStyles[0];

    return (Marshallable) SLDFactory.createNamedLayer( m_parentTitle, null, userStyles );
  }

  private String formatExportAbstract( )
  {
    final DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();
    dateTimeInstance.setTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() );
    final String dateString = dateTimeInstance.format( new Date() );
    return String.format( "Exported by Kalypso %s", dateString ); //$NON-NLS-1$
  }
}
