/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.ant;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tools.ant.Project;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.java.lang.reflect.ClassUtilities;
import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.merged.Source;
import org.kalypso.simulation.core.ant.copyobservation.CopyObservationFeatureVisitor;
import org.kalypso.simulation.core.ant.copyobservation.ICopyObservationSource;
import org.kalypso.simulation.core.ant.copyobservation.source.FeatureCopyObservationSource;
import org.kalypso.simulation.core.ant.copyobservation.target.CopyObservationTargetFactory;
import org.kalypso.simulation.core.ant.copyobservation.target.ICopyObservationTarget;
import org.kalypsodeegree.model.feature.FeatureVisitor;

/**
 * Ein Ant Task, der Zeitreihen-Links in GMLs kopiert. Die generelle Idee ist es, alle Features eines GML durchzugehen,
 * und für jedes Feature eine Zeitreihe (definiert über einen Link) zu lesen und an eine andere Stelle (definiert durch
 * eine andere Property des Features) zu schreiben. <code>
 *    <copyObservation gml="${project.dir}/.templates/Modell/wiskiimport_durchfluß.gml" featurePath="Wiski" context="${calc.dir}" targetObservation="lokal">
 *      <source property="wiski_vergangenheit" from="${startsim}" to="${stopsim}" />
 *    </copyObservation>
 * </code>
 * 
 * @author belger
 */
public class CopyObservationTask extends AbstractFeatureVisitorTask
{
  /**
   * Zielverzeichnis für generierte Zeitreihen. Überschreibt targetobservation.
   */
  private File m_targetObservationDir;

  /**
   * Name der Feature-Property, welche den Link enthält, an welche Stelle das Ergebnis geschrieben wird.
   */
  private String m_targetobservation;

  private String m_targetFrom;

  private String m_targetTo;

  /**
   * Wir benutzt, um den entsprechenden Metadata-Eintrag in den Zeitreiehen zu generieren Default mit -1 damit getestet
   * werden kann ob die Eigenschaft gesetzt wurde.
   */
  private String m_forecastFrom = null;

  /**
   * Wird benutzt, um den entsprechenden Metadata-Eintrag in den Zeitreiehen zu generieren Default mit -1 damit getestet
   * werden kann ob die Eigenschaft gesetzt wurde.
   */
  private String m_forecastTo = null;

  /**
   * Die Liste der Tokens und deren Ersetzung in der Form:
   * <p>
   * tokenName-featurePropertyName;tokenName-featurePropertyName;...
   * <p>
   * Die werden benutzt um token-replace im Zml-Href durchzuführen (z.B. um automatisch der Name der Feature als
   * Request-Name zu setzen)
   */
  private String m_tokens = "";

  /**
   * Ordered List of 'Source' Elements. Each source will be read as Observation, the combination of all sources will be
   * written to 'targetobservation'
   */
  private final List<Source> m_sources = new ArrayList<Source>();

  /**
   * List of metadata-properties and values to set to the target observation
   */
  private final MetadataList m_metadata = new MetadataList();

  public CopyObservationTask( )
  {
    super( false );
  }

  /**
   * @see org.kalypso.simulation.ui.ant.AbstractFeatureVisitorTask#createVisitor(java.net.URL,
   *      org.kalypso.contribs.java.net.IUrlResolver, org.kalypso.contribs.java.util.logging.ILogger)
   */
  @Override
  public final FeatureVisitor createVisitor( final URL context, final ILogger logger )
  {
    final DateRange forecastRange = DateRange.createDateRangeOrNull( parseDateTime( m_forecastFrom ), parseDateTime( m_forecastTo ) );
    final DateRange targetRange = DateRange.createDateRangeOrNull( parseDateTime( m_targetFrom ), parseDateTime( m_targetTo ) );

    final Source[] srcs = m_sources.toArray( new Source[m_sources.size()] );

    final ICopyObservationTarget obsTarget = CopyObservationTargetFactory.getLink( context, m_targetobservation, m_targetObservationDir, targetRange, forecastRange );
    final ICopyObservationSource obsSource = new FeatureCopyObservationSource( context, srcs, m_tokens );

    return new CopyObservationFeatureVisitor( obsSource, obsTarget, m_metadata, logger );
  }

  private Date parseDateTime( final String lexicalDate )
  {
    if( lexicalDate == null )
      return null;

    return DateUtilities.parseDateTime( lexicalDate );
  }

  public final String getTargetobservation( )
  {
    return m_targetobservation;
  }

  public final void setTargetobservation( final String targetobservation )
  {
    m_targetobservation = targetobservation;
  }

  public final void addConfiguredSource( final Source source )
  {
    m_sources.add( source );
  }

  public final void addConfiguredMetadata( final Metadata metadata )
  {
    if( metadata.getName() == null )
    {
      getProject().log( "Cannot add Metadata since property name is null", Project.MSG_WARN );
      return;
    }

    if( metadata.getValue() == null )
    {
      getProject().log( "Cannot add Metadata since property value is null. Property name: " + metadata.getName(), Project.MSG_WARN );
      return;
    }

    m_metadata.setProperty( metadata.getName(), metadata.getValue() );
  }

  public final String getForecastFrom( )
  {
    return m_forecastFrom;
  }

  public final void setForecastFrom( final String forecastFrom )
  {
    m_forecastFrom = forecastFrom;
  }

  public final String getForecastTo( )
  {
    return m_forecastTo;
  }

  public final void setForecastTo( final String forecastTo )
  {
    m_forecastTo = forecastTo;
  }

  public final void setTargetFrom( final String targetFrom )
  {
    m_targetFrom = targetFrom;
  }

  public final void setTargetTo( final String targetTo )
  {
    m_targetTo = targetTo;
  }

  public final String getTokens( )
  {
    return m_tokens;
  }

  public final void setTokens( final String tokens )
  {
    m_tokens = tokens;
  }

  public static final class Metadata
  {
    private String m_name;

    private String m_value;

    public String getName( )
    {
      return m_name;
    }

    public void setName( final String name )
    {
      m_name = name;
    }

    public String getValue( )
    {
      return m_value;
    }

    public void setValue( final String value )
    {
      m_value = value;
    }
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.IErrorHandler#handleError(org.eclipse.swt.widgets.Shell,
   *      org.eclipse.core.runtime.IStatus)
   */
  @Override
  public final void handleError( final Shell shell, final IStatus status )
  {
    ErrorDialog.openError( shell, ClassUtilities.getOnlyClassName( getClass() ), "Fehler beim Kopieren der Zeitreihen", status );
  }

  public final File getTargetObservationDir( )
  {
    return m_targetObservationDir;
  }

  public final void setTargetObservationDir( final File targetObservationDir )
  {
    m_targetObservationDir = targetObservationDir;
  }
}
