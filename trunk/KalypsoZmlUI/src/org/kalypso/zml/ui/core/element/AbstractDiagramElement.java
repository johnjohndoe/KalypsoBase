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
package org.kalypso.zml.ui.core.element;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.core.registry.KodRegistry;

import de.openali.odysseus.chart.factory.config.StyleFactory;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;
import de.openali.odysseus.chart.framework.util.StyleUtils;
import de.openali.odysseus.chartconfig.x020.LayerType;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractDiagramElement implements IZmlDiagramElement
{

  @Override
  public final <T extends IStyle> T getStyle( final Class<T> clazz, final String type, final int index )
  {
    final IStyleSet styleSet = getStyleSet( type );
    final StyleSetVisitor visitor = new StyleSetVisitor();

    final T themeStyle = visitor.visit( styleSet, clazz, index );
    if( themeStyle != null )
      return themeStyle;

    try
    {
      final KodRegistry registy = KodRegistry.getInstance();
      final LayerType layer = registy.getLayer( type );

      final T kodStyle = visitor.visit( StyleFactory.createStyleSet( layer.getStyles() ), clazz, index );
      if( kodStyle != null )
        return kodStyle;
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
    }

    return StyleUtils.getDefaultStyle( clazz );
  }

}
