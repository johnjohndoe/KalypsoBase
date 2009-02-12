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
package org.kalypso.swtchart.table;

import java.util.Map;

import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

/**
 * @author burtscher1
 *
 */
public class ObservationTable implements ITable
{
  /**
   * Text displayed as table header
   */
  private String m_title;
  /**
   * CSS class used for table header
   */
  private String m_titleClass;
  /**
   * Text displayed as table abstract
   */
  private String m_abstract;
  /**
   * CSS class used for table abstract
   */
  private String m_abstractClass;
  /**
   * table content as TupleResult; name is used as column header
   */
  private TupleResult m_content;
  /**
   * CSS classes for value cells
   */
  private Map<IComponent, String> m_componentClasses;
  /**
   * CSS classes for column header cells
   */
  private Map<IComponent, String> m_componentHeaderClasses;
  /**
   * CSS class used for the whole table
   */
  private String m_tableClass;
  /**
   * Map containing headers for value columns
   */
  private Map<IComponent, String> m_componentHeaders;
  
  public ObservationTable(String tableClass, String title, String titleClass, String tAbstract, String tAbstractClass, TupleResult content, Map<IComponent, String> componentHeaders, Map<IComponent, String> componentHeaderClasses, Map<IComponent, String> componentClasses  )
  {
    m_tableClass=tableClass;
    m_title=title;
    m_titleClass=titleClass;
    m_abstract=tAbstract;
    m_abstractClass=tAbstractClass;
    m_content=content;
    m_componentHeaders=componentHeaders;
    m_componentHeaderClasses=componentHeaderClasses;
    m_componentClasses=componentClasses;
  }
  
  /**
   * @see org.kalypso.swtchart.table.ITable#showTable()
   */
  public String showTable( )
  {
    String html="";
    
    int componentCount=m_content.getComponents().length;
    int contentCount=m_content.size();
    
    html+="<table class='"+m_tableClass+"'>";
    //Header
    html+="<tr>";
    html+="<td colspan='"+componentCount+"' class='"+m_titleClass+"'>";
    html+=m_title;
    html+="</td>";
    html+="</tr>";
    
    //Abstract
    html+="<tr>";
    html+="<td colspan='"+componentCount+"' class='"+m_abstractClass+"'>";
    html+=m_abstract;
    html+="</td>";
    html+="</tr>";
    
    IComponent[] components = m_content.getComponents();
    
    //cell headers
    html+="<tr>";
    for( IComponent component : components )
    {
      html+="<td class='"+m_componentHeaderClasses.get(component)+"'>";
      String header=m_componentHeaders.get(component);
      //TODO: hier muss evtl. der Name der Component verwendet werden, falls die Header-Angabe leer ist
      html+=header;
      html+="</td>";
    }
    html+="</tr>";
    
    for (int i=0;i<contentCount;i++)
    {
      html+="<tr>";
      final IRecord rec = m_content.get( i );
      for( IComponent component : components )
      {
        html+="<td class='"+m_componentClasses.get(component)+"'>";
        
        //TODO: im moment werden die nur einfach geschrieben - d.h. die Formattierung findet im TableProvider statt
        html+=rec.getValue( component );
        html+="</td>";
      }
      html+="</tr>";
    }
    
    //Content
    html+="</table>";
    return html;
  }
  
}
