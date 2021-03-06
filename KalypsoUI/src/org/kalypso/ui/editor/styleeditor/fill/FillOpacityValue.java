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
package org.kalypso.ui.editor.styleeditor.fill;

import org.eclipse.swt.widgets.Spinner;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.InputWithContextObservableValue;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Fill;

/**
 * @author Gernot Belger
 */
public class FillOpacityValue extends InputWithContextObservableValue<Fill, Integer>
{
  public FillOpacityValue( final IStyleInput<Fill> input )
  {
    super( input, Integer.class );
  }

  @Override
  protected Integer getValueFromData( final Fill data )
  {
    try
    {
      return (int)(data.getOpacity( null ) * 100.0);
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
      return 100;
    }
  }

  @Override
  protected void setValueToInput( final Fill data, final Integer value )
  {
    if( value == null )
      data.setOpacity( 1.0 );
    else
      data.setOpacity( value / 100.0 );
  }

  public void configureSpinner( final Spinner spinner )
  {
    spinner.setMinimum( 0 );
    spinner.setMaximum( 100 );
    spinner.setDigits( 0 );
    spinner.setIncrement( 1 );
    spinner.setPageIncrement( 10 );
  }
}
