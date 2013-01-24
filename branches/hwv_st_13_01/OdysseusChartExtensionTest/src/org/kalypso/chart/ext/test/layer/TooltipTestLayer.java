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
package org.kalypso.chart.ext.test.layer;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ext.test.layer.provider.TooltipTestLayerProvider;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;

/**
 * @author burtscher1
 */
public class TooltipTestLayer extends AbstractChartLayer implements ITooltipChartLayer
{

  public TooltipTestLayer( final TooltipTestLayerProvider provider )
  {
    super( provider );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#createLegendEntries()
   */
  @Override
  protected ILegendEntry[] createLegendEntries( )
  {
    final LegendEntry le = new LegendEntry( this, getDescription() )
    {

      @Override
      public void paintSymbol( final GC gc, final Point size )
      {
        // TODO Auto-generated method stub

      }

    };
    return new ILegendEntry[] { le };
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    return new DataRange<Number>( 0, 1 );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    return new DataRange<Number>( 0, 1 );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  @Override
  public EditInfo getHover( final Point pos )
  {
    String text = "Halli Hallo.\n ";
    text += "Ich bin ein richtig langer Text, der als Tooltip meist im Wege steht.\n";
    text += "Mit mir kann man testen, ob der Tooltip sich auf in grenzwertigen Situationen\n";
    text += "richtig ausrichtet.";
    text += "Ich bin ein richtig langer Text, der als Tooltip meist im Wege steht.\n";
    text += "Mit mir kann man testen, ob der Tooltip sich auf in grenzwertigen Situationen\n";
    text += "richtig ausrichtet.";
    text += "Ich bin ein richtig langer Text, der als Tooltip meist im Wege steht.\n";
    text += "Mit mir kann man testen, ob der Tooltip sich auf in grenzwertigen Situationen\n";
    text += "richtig ausrichtet.";
    text += "Ich bin ein richtig langer Text, der als Tooltip meist im Wege steht.\n";
    text += "Mit mir kann man testen, ob der Tooltip sich auf in grenzwertigen Situationen\n";
    text += "richtig ausrichtet.";
    return new EditInfo( this, null, null, null, text, pos );
  }
}
