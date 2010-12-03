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
package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;

import org.eclipse.core.runtime.Assert;

import de.openali.odysseus.chart.framework.model.data.IDataRange;

/**
 * @author alibu
 */
public class NumberLabelCreator extends AbstractLabelCreator implements ILabelCreator
{

  private final String m_formatString;

  public NumberLabelCreator( final String formatString )
  {
    Assert.isNotNull( formatString );

    m_formatString = formatString;
  }

  /**
   * @see org.kalypso.chart.ext.test.axisrenderer.ILabelCreator#getLabel(java.lang.Number,
   *      org.kalypso.chart.framework.model.data.IDataRange)
   */
  @Override
  public String getLabel( final Number[] ticks, final int i, final IDataRange<Number> range )
  {
    if( ticks == null )
      return "";

    if( "%s".equals( m_formatString ) )
    {
      final Format format = getFormat( range );
      return format == null ? null : format.format( ticks[i] );
    }
    else
    {
      return String.format( m_formatString, ticks[i] );
    }
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator#getLabel(java.lang.Number,
   *      de.openali.odysseus.chart.framework.model.data.IDataRange)
   */
  @Override
  public String getLabel( final Number value, final IDataRange<Number> range )
  {
    return getLabel( new Number[] { value }, 0, range );
  }

  public Format getFormat( final IDataRange<Number> range )
  {
    final Number min = range.getMin();
    if( min == null )
      return null;

    final Number max = range.getMax();
    if( max == null )
      return null;

    // Differenz bilden und sicherstellen, dass sie positiv ist
    Double diff = (max == null || min == null) ? 0.0 : Math.abs( max.doubleValue() - min.doubleValue() );

    final NumberFormat nf = new DecimalFormat();
    // Anzahl gültiger stellen
    final int validDigits = 3;
    // Fraction digits
    int fd = 0;
    // Integer digits
    int id = 1;

    // Minuszeichen einplanen
    if( min != null && max != null && (max.doubleValue() < 0 || min.doubleValue() < 0) )
    {
      id++;
    }

    // Vorkommastellen ausrechnen
    double tmpmax = (max == null || min == null) ? 0.0 : Math.max( Math.abs( max.doubleValue() ), Math.abs( min.doubleValue() ) );
    if( tmpmax >= 1 )
    {
      while( tmpmax >= 1 )
      {
        tmpmax /= 10;
        id++;
      }
    }

    // Bereichs-10er-potenz ausmachen
    int pow = 0;
    if( diff >= 1 )
    {
      while( diff >= 1 )
      {
        diff /= 10;
        pow++;
      }
    }
    else if( diff != 0 )
    {
      while( diff <= 0.1 )
      {
        diff *= 10;
        pow--;
      }
    }
    //
    if( pow >= 0 )
    {
      fd = pow > validDigits ? 0 : Math.abs( validDigits - pow );
    }
    else
    {
      fd = Math.abs( pow ) + validDigits;
    }

    nf.setMaximumIntegerDigits( id );
    nf.setMinimumIntegerDigits( 1 );
    nf.setMaximumFractionDigits( fd );
    nf.setMinimumFractionDigits( fd );
    nf.setGroupingUsed( false );

    return nf;
  }

}
