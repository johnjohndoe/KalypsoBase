/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 *
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 *
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 *
 * and
 *
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact:
 *
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 *
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.simulation.ui.ant;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.kalypso.contribs.java.util.DateUtilities;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;
import org.kalypso.simulation.core.ant.GMLWeightingOperation;
import org.kalypso.simulation.core.ant.IGMLWeightingData;

/**
 * This Task generates from a number of input zml files new zml output files. <br>
 * It uses a combination of operation and n-operation filters like this: <br>
 * <center><b>ZMLout = sum( f(i)*ZMLin(i) ) </b> </center> <br>
 * f(i): factor <br>
 * ZMLin(i): input timeseries <br>
 * the parameters are given by a gml model and some configuration strings (featurepath and property names)
 * 
 * @author doemming
 */
public class GMLWeightingTask extends Task implements IGMLWeightingData
{
  private File m_targetMapping = null;

  private URL m_modelURL;

  private URL m_targetContext;

  private String m_featurePathTarget; // e.g. "PegelCollectionAssociation/PegelMember"

  private String m_propZMLTarget; // e.g. "Niederschlag"

  private String m_propRelationWeightMember; // e.g. "gewichtung"

  private String m_propWeight; // e.g. "faktor"

  private String m_propOffset; // e.g. "faktor"

  private String m_propRelationSourceFeature; // e.g. "ombrometerMember"

  private String m_propZMLSource;// e.g. Niederschlag_gemessen

  private String m_propSourceUsed;

  private String m_sourceFilter;

  private String m_from;

  private String m_to;

  private String m_forecastFrom;

  private String m_forecastTo;

  private String m_sourceFrom;

  private String m_sourceTo;

  private String m_targetFrom;

  private String m_targetTo;

  @Override
  public void execute( )
  {
    try
    {
      final Project antProject = getProject();
      // REMARK: It is NOT possible to put this inner class into an own .class file (at least not inside the plugin
      // code) else we get an LinkageError when accessing the Project class.
      final ILogger logger = new ILogger()
      {
        @Override
        public void log( final Level level, final int msgCode, final String message )
        {
          final String outString = LoggerUtilities.formatLogStylish( level, msgCode, message );

          if( antProject == null )
            System.out.println( outString );
          else
            antProject.log( outString );
        }
      };

      final String message = getDescription();
      if( message != null && message.length() > 0 )
        logger.log( Level.INFO, LoggerUtilities.CODE_NEW_MSGBOX, message );

      /* Perpare ant */
      final GMLWeightingOperation operation = new GMLWeightingOperation( this, logger );
      operation.execute();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new BuildException( e );
    }
  }

  /**
   * @param targetMapping
   *          gml file that will be generated and includes the mapping that will be generated from the model
   */
  public final void setTargetMapping( final File targetMapping )
  {
    m_targetMapping = targetMapping;
  }

  /**
   * @param modelURL
   *          reference to the model that describes the mapping in a gml structure
   */
  public final void setModelURL( final URL modelURL )
  {
    m_modelURL = modelURL;
  }

  /**
   * @param targetContext
   *          context to use
   */
  public final void setTargetContext( final URL targetContext )
  {
    m_targetContext = targetContext;
  }

  /**
   * @param featurePathTarget
   *          path to the features that contain the ZML-target properties and the references to weighting features
   */
  public final void setFeaturePathTarget( final String featurePathTarget )
  {
    m_featurePathTarget = featurePathTarget;
  }

  /**
   * @param propRelationSourceFeature
   *          name of property that links from weighting feature to zml source feature
   */
  public final void setPropRelationSourceFeature( final String propRelationSourceFeature )
  {
    m_propRelationSourceFeature = propRelationSourceFeature;
  }

  /**
   * @param propRelationWeightMember
   *          name of property that links from zml target feature to the list of weighting features
   */
  public final void setPropRelationWeightMember( final String propRelationWeightMember )
  {
    m_propRelationWeightMember = propRelationWeightMember;
  }

  /**
   * @param propWeight
   *          property name of the weighting property, feature property type must be double
   */
  public final void setPropWeight( final String propWeight )
  {
    m_propWeight = propWeight;
  }

  /**
   * @param propOffset
   *          property name of the offset property, feature property type must be double
   */
  public final void setPropOffset( final String propOffset )
  {
    m_propOffset = propOffset;
  }

  /**
   * @param propZMLSource
   *          property name of the zml source property, feature property type must be TimeSeriesLink
   */
  public final void setPropZMLSource( final String propZMLSource )
  {
    m_propZMLSource = propZMLSource;
  }

  /**
   * @param propSourceUsed
   *          property name of the zml sourceUsed property, feature property type must be Boolean. If set, the property
   *          is used to determined if this particular source is used or not.
   */
  public final void setPropSourceUsed( final String propSourceUsed )
  {
    m_propSourceUsed = propSourceUsed;
  }

  /**
   * @param sourceFilter
   *          If non- <code>null</code>, this filter will be applied to every source-zml
   */
  public final void setSourceFilter( final String sourceFilter )
  {
    m_sourceFilter = sourceFilter;
  }

  /**
   * @param propZMLTarget
   *          property name of the zml target property, feature property type must be TimeSeriesLink
   */
  public final void setPropZMLTarget( final String propZMLTarget )
  {
    m_propZMLTarget = propZMLTarget;
  }

  /**
   * @param from
   *          beginning of measure periode
   * @deprecated Use sourceFrom, targetFrom or forecastFrom instead
   */
  @Deprecated
  public final void setFrom( final String from )
  {
    m_from = from;
  }

  /**
   * @param forecastFrom
   *          beginning of forecast periode (end of measure periode)
   */
  public final void setForecastFrom( final String forecastFrom )
  {
    m_forecastFrom = forecastFrom;
  }

  /**
   * @param forecastTo
   *          end of forecast periode (end of measure periode)
   */
  public final void setForecastTo( final String forecastTo )
  {
    m_forecastTo = forecastTo;
  }

  /**
   * @param to
   *          end of forecast periode
   * @deprecated Use sourceTo, targetTo or forecastTo instead
   */
  @Deprecated
  public final void setTo( final String to )
  {
    m_to = to;
  }

  /**
   * @param sourceFrom
   *          start of request for source - observations
   */
  public final void setSourceFrom( final String sourceFrom )
  {
    m_sourceFrom = sourceFrom;
  }

  /**
   * @param sourceTo
   *          end of request for source - observations
   */
  public final void setSourceTo( final String sourceTo )
  {
    m_sourceTo = sourceTo;
  }

  /**
   * @param targetFrom
   *          start of request for source - observations
   */
  public final void setTargetFrom( final String targetFrom )
  {
    m_targetFrom = targetFrom;
  }

  /**
   * @param targetTo
   *          end of request for source - observations
   */
  public final void setTargetTo( final String targetTo )
  {
    m_targetTo = targetTo;
  }

  @Override
  public URL getModelLocation( )
  {
    return m_modelURL;
  }

  @Override
  public String getTargetFeaturePath( )
  {
    return m_featurePathTarget;
  }

  @Override
  public String getTargetZMLProperty( )
  {
    return m_propZMLTarget;
  }

  @Override
  public URL getTargetContext( )
  {
    return m_targetContext;
  }

  @Override
  public String getWeightMember( )
  {
    return m_propRelationWeightMember;
  }

  @Override
  public String getOffsetProperty( )
  {
    return m_propOffset;
  }

  @Override
  public String getWeightProperty( )
  {
    return m_propWeight;
  }

  @Override
  public String getSourceMember( )
  {
    return m_propRelationSourceFeature;
  }

  @Override
  public String getSourceZMLProperty( )
  {
    return m_propZMLSource;
  }

  @Override
  public String getSourceIsUsedProperty( )
  {
    return m_propSourceUsed;
  }

  @Override
  public String getSourceFilter( )
  {
    return m_sourceFilter;
  }

  @Override
  public Date getSourceFrom( )
  {
    if( m_sourceFrom == null )
      return DateUtilities.parseDateTime( m_from );

    return DateUtilities.parseDateTime( m_sourceFrom );
  }

  @Override
  public Date getSourceTo( )
  {
    if( m_sourceTo == null )
      return DateUtilities.parseDateTime( m_forecastFrom );

    return DateUtilities.parseDateTime( m_sourceTo );
  }

  @Override
  public Date getTargetFrom( )
  {
    if( m_targetFrom == null )
      return DateUtilities.parseDateTime( m_forecastFrom );

    return DateUtilities.parseDateTime( m_targetFrom );
  }

  @Override
  public Date getTargetTo( )
  {
    if( m_targetTo == null )
      return DateUtilities.parseDateTime( m_to );

    return DateUtilities.parseDateTime( m_targetTo );
  }

  @Override
  public Date getForecastFrom( )
  {
    return DateUtilities.parseDateTime( m_forecastFrom );
  }

  @Override
  public Date getForecastTo( )
  {
    if( m_forecastTo == null )
      return DateUtilities.parseDateTime( m_to );

    return DateUtilities.parseDateTime( m_forecastTo );
  }

  @Override
  public File getTargetMapping( )
  {
    return m_targetMapping;
  }
}