/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and Coastal Engineering
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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.wizards.exporter;

import java.awt.Color;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.configuration.Configuration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.net.UrlResolverSingleton;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.DisposeHelper;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.diagview.jfreechart.ExportableChart;
import org.kalypso.ogc.sensor.diagview.jfreechart.ObservationChart;
import org.kalypso.ogc.sensor.tableview.TableView;
import org.kalypso.ogc.sensor.tableview.TableViewUtils;
import org.kalypso.ogc.sensor.tableview.swing.ExportableObservationTable;
import org.kalypso.ogc.sensor.tableview.swing.ObservationTable;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.template.obsdiagview.ObsdiagviewType;
import org.kalypso.template.obstableview.ObstableviewType;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Helps with the handling of exporter related operations.
 * 
 * @author belger
 * @author schlienger
 */
public final class ExporterHelper
{
  public static final String MSG_TOKEN_NOT_FOUND = "Token not found";

  private ExporterHelper()
  {
  // not intended to be instanciated
  }

  /**
   * Creates a replace-tokens properties list. For each token, it fetches the value of its associated feature-property.
   * 
   * @param feature
   *          feature from which to get properties' values
   * @param tokens
   *          list of name-property pair
   */
  public static Properties createReplaceTokens( final Feature feature, final Arguments tokens )
  {
    final Properties replacetokens = new Properties();
    for( final Iterator tokIt = tokens.entrySet().iterator(); tokIt.hasNext(); )
    {
      final Map.Entry entry = (Entry)tokIt.next();
      final String tokenname = (String)entry.getKey();
      final String featureProperty = (String)entry.getValue();

      final Object property = feature.getProperty( featureProperty );
      String replace = null;
      if( property instanceof TimeseriesLinkType )
        replace = ( (TimeseriesLinkType)property ).getHref();
      else if( property != null )
        replace = property.toString();
      else if( property == null )
        replace = "!" + MSG_TOKEN_NOT_FOUND + ": " + tokenname + "!";

      if( replace != null )
        replacetokens.setProperty( tokenname, replace );
    }

    return replacetokens;
  }

  /**
   * @return list of argument-values whose associated name begins with the given prefix
   */
  public static String[] getArgumentValues( final String prefix, final Arguments args )
  {
    final List values = new ArrayList();

    for( final Iterator it = args.entrySet().iterator(); it.hasNext(); )
    {
      final Map.Entry entry = (Map.Entry)it.next();
      final String argName = (String)entry.getKey();

      if( argName.startsWith( prefix ) )
      {
        final String argValue = (String)entry.getValue();
        values.add( argValue );
      }
    }

    return (String[])values.toArray( new String[values.size()] );
  }

  /**
   * Creates an {@link UrlArgument}for each argument which begins with 'argumentPrefix'. Each such Argument should at
   * least have the following sub-arguments:
   * <ul>
   * <li>'label'
   * <li>'href'
   * </ul>
   * <p>
   * Other sub-arguments are simply passed to the newly created UrlArgument so that they can later be retrieved using
   * UrlArgument.getProperty(...).
   * 
   * @return list of Url items found in the arguments that we got from the supplier (ex: ExportResultsWizardPage)
   */
  public static UrlArgument[] createUrlItems( final String argumentPrefix, final Arguments arguments, final URL context )
      throws CoreException
  {
    final Collection items = new ArrayList();
    final Collection stati = new ArrayList();

    for( final Iterator aIt = arguments.entrySet().iterator(); aIt.hasNext(); )
    {
      final Map.Entry entry = (Entry)aIt.next();
      final String key = (String)entry.getKey();
      if( key.startsWith( argumentPrefix ) )
      {
        final Arguments args = (Arguments)entry.getValue();
        final String label = args.getProperty( "label", "<unbekannt>" );
        final String strFile = args.getProperty( "href" );
        if( strFile == null )
          stati.add( new Status( IStatus.WARNING, KalypsoSimulationUIPlugin.getID(), 0, "Keine Datei-Angabe für: "
              + key, null ) );

        try
        {
          items.add( new UrlArgument( label, UrlResolverSingleton.resolveUrl( context, strFile ), args ) );
        }
        catch( final MalformedURLException e )
        {
          stati.add( new Status( IStatus.WARNING, KalypsoSimulationUIPlugin.getID(), 0, "Ungültiger Pfad: " + strFile,
              e ) );
        }
      }
    }

    if( stati.size() > 0 )
      throw new CoreException( new MultiStatus( KalypsoSimulationUIPlugin.getID(), 0, (IStatus[])stati
          .toArray( new IStatus[stati.size()] ), "Siehe Details", null ) );

    return (UrlArgument[])items.toArray( new UrlArgument[items.size()] );
  }

  /**
   * Exports a diagram based on the diagram-template (odt).
   * 
   * @param context
   *          context into which the template file is loaded
   * @param arguments
   *          export-arguments, used to retrieve: width, height and imageFormat
   * @param reader
   *          delivers the contents of the template file
   * @param output
   *          export will be written into it
   * @param monitor
   *          handles user feedback
   * @param templateUrl
   *          url of the template being used
   * @param metadataExtensions
   *          can be update from this export with information from observations
   * @param identifierPrefix
   *          prefix of the identifier of the exportable object
   * @param category
   *          the category of the exportable object
   */
  public static IStatus exportObsDiagram( final URL context, final Arguments arguments, final Reader reader,
      final OutputStream output, final IProgressMonitor monitor, final URL templateUrl,
      final Configuration metadataExtensions, final String identifierPrefix, final String category, final Integer kennzifferIndex )
  {
    DiagView tpl = null;
    ObservationChart chart = null;
    try
    {
      tpl = new DiagView();
      chart = new ObservationChart( tpl, true );
      chart.setBackgroundPaint( Color.WHITE );

      final ObsdiagviewType xml = DiagViewUtils.loadDiagramTemplateXML( reader );

      IStatus status = DiagViewUtils.applyXMLTemplate( tpl, xml, context, true, ExporterHelper.MSG_TOKEN_NOT_FOUND );

      if( tpl.getItems().length == 0 )
        // Status has been changed from ERROR to Warning to avoid error message in ElbePolte (gauge Usti) model
        return StatusUtilities.createWarningStatus( "Diagramm " + tpl.getTitle() + " (" + category
            + ") beinhaltet keine Daten. Der Export findet also nicht statt." );

      // wrap as warning because even if an obs is missing we can still export diagram
      status = StatusUtilities.wrapStatus( status, IStatus.WARNING, IStatus.WARNING | IStatus.ERROR );

      final int width = Integer.parseInt( arguments.getProperty( "width", "800" ) );
      final int height = Integer.parseInt( arguments.getProperty( "height", "600" ) );
      final String format = arguments.getProperty( "imageFormat", ExportableChart.DEFAULT_FORMAT );

      final IStatus status2 = new ExportableChart( chart, format, width, height, identifierPrefix, category, kennzifferIndex )
          .exportObject( output, monitor, metadataExtensions );

      return StatusUtilities.createStatus( new IStatus[]
      { status, status2 }, "Fehler während des Exports der Diagrammvorlage: " + templateUrl.getFile() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      new DisposeHelper().addDisposeCandidate( tpl ).addDisposeCandidate( chart ).dispose();
    }
  }

  /**
   * Exports an observation table based on the table-template (ott).
   * 
   * @param context
   *          context into which the template file is loaded
   * @param arguments
   *          export-arguments [currently not used]
   * @param reader
   *          delivers the contents of the template file
   * @param output
   *          export will be written into it
   * @param monitor
   *          handles user feedback
   * @param templateUrl
   *          url of the template being used
   * @param metadataExtensions
   *          can be update from this export with information from observations
   * @param identifierPrefix
   *          prefix of the identifier of the exportable object
   * @param category
   *          the category of the exportable object
   */
  public static IStatus exportObsTable( final URL context, final Arguments arguments, final Reader reader,
      final OutputStream output, final IProgressMonitor monitor, final URL templateUrl,
      final Configuration metadataExtensions, final String identifierPrefix, final String category, final Integer kennzifferIndex )
  {
    if( arguments == null )
    {
      // avoid yellow thingies!
    }

    TableView tpl = null;
    ObservationTable table = null;
    try
    {
      tpl = new TableView();
      table = new ObservationTable( tpl, true, false );
      final ObstableviewType xml = TableViewUtils.loadTableTemplateXML( reader );

      IStatus status = TableViewUtils.applyXMLTemplate( tpl, xml, context, true, ExporterHelper.MSG_TOKEN_NOT_FOUND );

      if( tpl.getItems().length == 0 )
        // Status has been changed from ERROR to Warning to avoid error message in ElbePolte (gauge Usti) model
        return StatusUtilities.createWarningStatus( "Tabelle (" + category
            + ") beinhaltet keine Daten. Der Export findet also nicht statt." );

      // wrap as warning because even if an obs is missing we can still export diagram
      status = StatusUtilities.wrapStatus( status, IStatus.WARNING, IStatus.WARNING | IStatus.ERROR );

      final IStatus status2 = new ExportableObservationTable( table, identifierPrefix, category, kennzifferIndex ).exportObject( output,
          monitor, metadataExtensions );

      return StatusUtilities.createStatus( new IStatus[]
      { status, status2 }, "Fehler während des Exports der Tabellenvorlage: " + templateUrl.getFile() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      new DisposeHelper().addDisposeCandidate( tpl ).addDisposeCandidate( table ).dispose();
    }
  }

  /**
   * Denotes the items which are displayed in the template selection wizard page
   * 
   * @author schlienger
   */
  public static class UrlArgument
  {
    private String m_label;
    private final URL m_templateUrl;
    private final Arguments m_args;

    public UrlArgument( final String label, final URL templateUrl, final Arguments args )
    {
      m_label = label;
      m_templateUrl = templateUrl;
      m_args = args;
    }

    public String getLabel()
    {
      return m_label;
    }

    public URL getUrl()
    {
      return m_templateUrl;
    }

    public String getProperty( final String key )
    {
      return m_args.getProperty( key );
    }

    public String getProperty( final String key, final String defaultValue )
    {
      return m_args.getProperty( key, defaultValue );
    }

    public String toString()
    {
      return m_label;
    }

    public Arguments getArguments()
    {
      return m_args;
    }

    public void setLabel( final String name )
    {
      m_label = name;
    }
  }
}
