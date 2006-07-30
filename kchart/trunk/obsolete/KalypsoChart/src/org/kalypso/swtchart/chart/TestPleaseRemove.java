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
package org.kalypso.swtchart.chart;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.kalypso.jwsdp.JaxbUtilities;
import org.ksp.chart.layerstyle.ObjectFactory;
import org.ksp.chart.layerstyle.StyleType;

/**
 * @author alibu
 *
 */
public class TestPleaseRemove
{
  public final static ObjectFactory OF = new ObjectFactory();
  
  private static final JAXBContext JC = JaxbUtilities.createQuiet( ObjectFactory.class );
  
  public void irgendetwasSerialisieren( Object etwasWasPasst )
  {
    /*
    Style style = OF.createStyle();
    style.setId( etwasWasPasst.getID() );
    List<JAXBElement< ? extends PointType>> point = style.getPoint();
    
    Marshaller marshaller = JC.createMarshaller();
    marshaller.marshal( style, new FileWriter(new File("pfad zu Datei")) );
    */
  }
  
  
  public StyleType etwasLesen() throws JAXBException
  {
    Object object = JC.createUnmarshaller().unmarshal( new File("somewhere") );
    
    return (StyleType) object;
  }
  
  public void etwasMitDemStayleMachen( Object conf, StyleType style )
  {
    /*
       conf.addPointStyle( style.getPoint() );
       */
    
  }
}
