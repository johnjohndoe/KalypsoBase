/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.simulation.ui.wizards.exporter;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.impl.AbstractExporter;
import org.kalypso.metadoc.ui.ExportableTreeItem;
import org.kalypso.simulation.ui.wizards.exporter.sumuptable.ExportableSumUpTable;

/**
 * Exports a table showing a list of forecast-observations with the following structure:
 * 
 * <pre>
 * User-defined Columns (*) | Max-Value (**) | Time(Max-Value) | Alarmlevel | Value(Alarmlevel) | Time i + 0.p (***) | Time i + 1.p | ... | Time i + n.p
 * </pre>
 * <ul>
 * <li>(*) the list of user-defined columns to display is defined in the arguments of the exporter
 * <li>(**) the max-value is taken for one axis which is defined in the arguments of the exporter
 * <li>(***) the value of p is also defined in the arguments
 * </ul>
 * <p>
 * This exporter has no wizard pages.
 * <p>
 * Arguments (minimal) specification:
 * <ul>
 * <li>documentName [optional, default is 'übersicht.cvs'] the name of the created document. If more than one
 * SumpUpTableexporter is configured, then documentName should be provided. It is required to give another documentName
 * for each configured SumUpTableExporter.
 * <li>separator [optional, default is semicolon] the separator to use for output
 * <li>charset [optional, uses the system default charset when no specified] the charset used for the created document
 * <li>axisType the axis type (as found in TimeserieConstants for instance) of the axis to export
 * <li>delta the delta-value used for comparing values from the specified axis (see also axisType)
 * <li>dateFormat the date-format to use for output
 * <li>timeUnit the unit of time to add for each forecast-column (either a text-literal as defined in CalendarUtilities
 * or the field-value as in Calendar)
 * <li>timeStep the amount of time-unit to add for each forecast-column
 * <li>obs... the list of observations to export. Each obs argument should have 'label' and 'href' subarguments
 * <li>label (sub-argument of obs) can be used as name for the observation
 * <li>href (sub-argument of obs) the location of the observation (either absolute or relative to the context such as a
 * calccase)
 * <li>column... the list of user-defined columns to export. Each column argument should have 'position', 'label' and
 * 'content' subarguments
 * <li>position (sub-argument of column) the position of this column compared to the other user-defined columns
 * <li>label (sub-argument of column) the label of the column
 * <li>content (sub-argument of column) defines what will come in each cell in this column. See
 * {@link org.kalypso.simulation.ui.wizards.exporter.sumuptable.ColumnSpec}for more information on the syntax.
 * </ul>
 * <p>
 * Example:
 * 
 * <pre>
 * &lt;arg name=&quot;exporter3&quot;&gt;
 *  &lt;arg name=&quot;id&quot; value=&quot;sumUpTableExporter&quot; /&gt;
 * 
 *  &lt;arg name=&quot;documentName&quot; value=&quot;übersicht.csv&quot; /&gt;
 *  &lt;arg name=&quot;separator&quot; value=&quot;;&quot;/&gt;
 *  &lt;!--arg name=&quot;charset&quot; value=&quot;cp1252&quot;/--&gt;
 *  &lt;arg name=&quot;delta&quot; value=&quot;0.0001&quot;/&gt;
 *  &lt;arg name=&quot;dateFormat&quot; value=&quot;dd.MM.yyyy HH:mm:ss&quot;/&gt;
 * 
 *  &lt;arg name=&quot;obs3&quot;&gt;
 *  &lt;arg name=&quot;label&quot; value=&quot;Gröditz&quot; /&gt;
 *  &lt;arg name=&quot;href&quot; value=&quot;./Ergebnisse/Zeitreihen/QV_GROEDI.zml&quot; /&gt;
 *  &lt;/arg&gt;
 *  &lt;arg name=&quot;obs2&quot;&gt;
 *  &lt;arg name=&quot;label&quot; value=&quot;Boxberg&quot; /&gt;
 *  &lt;arg name=&quot;href&quot; value=&quot;./Ergebnisse/Zeitreihen/QV_BOXBRG.zml&quot; /&gt;
 *  &lt;/arg&gt;
 *  &lt;arg name=&quot;obs1&quot;&gt;
 *  &lt;arg name=&quot;label&quot; value=&quot;Bautzen&quot; /&gt;
 *  &lt;arg name=&quot;href&quot; value=&quot;./Ergebnisse/Zeitreihen/QV_BAUTZWB.zml&quot; /&gt;
 *  &lt;/arg&gt;
 * 
 *  &lt;arg name=&quot;column1&quot;&gt;
 *  &lt;arg name=&quot;position&quot; value=&quot;1&quot;/&gt;
 *  &lt;arg name=&quot;label&quot; value=&quot;Pegel&quot;/&gt;
 *  &lt;arg name=&quot;content&quot; value=&quot;arg:label&quot; /&gt;
 *  &lt;/arg&gt;
 *  
 *  &lt;arg name=&quot;column2&quot;&gt;
 *  &lt;arg name=&quot;position&quot; value=&quot;2&quot;/&gt;
 *  &lt;arg name=&quot;label&quot; value=&quot;Gewässer&quot;/&gt;
 *  &lt;arg name=&quot;content&quot; value=&quot;metadata:Gewässer&quot; /&gt;
 *  &lt;/arg&gt;
 * 
 *  &lt;arg name=&quot;axisType&quot; value=&quot;W&quot;/&gt;
 *  &lt;arg name=&quot;timeUnit&quot; value=&quot;HOUR&quot; /&gt;
 *  &lt;arg name=&quot;timeStep&quot; value=&quot;6&quot; /&gt;
 *  &lt;/arg&gt;
 * </pre>
 * 
 * @author schlienger
 */
public class SumUpTableExporter extends AbstractExporter
{
  /**
   * @see org.kalypso.metadoc.impl.AbstractExporter#createTreeItem()
   */
  @Override
  public ExportableTreeItem createTreeItem( final ExportableTreeItem parent ) throws CoreException
  {
    // from supplier
    final Arguments arguments = (Arguments) getFromSupplier( "arguments" );
    final URL context = (URL) getFromSupplier( "context" );

    final String documentFormatString = arguments.getProperty( "documentName", "übersicht.csv" );
    final String documentName = validateDocumentName( documentFormatString );
    final String documentTitle = arguments.getProperty( "documentTitle" );
    
    final IExportableObject expObj = new ExportableSumUpTable( arguments, context, getClass().getName(), documentName, documentTitle );

    return new ExportableTreeItem( getName(), getImageDescriptor(), parent, expObj, true, false );
  }
}
