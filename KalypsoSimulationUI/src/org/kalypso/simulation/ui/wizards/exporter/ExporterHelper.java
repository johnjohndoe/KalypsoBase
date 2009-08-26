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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.simulation.ui.KalypsoSimulationUIPlugin;
import org.kalypso.simulation.ui.i18n.Messages;
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
  public static final String MSG_TOKEN_NOT_FOUND = "Token not found"; //$NON-NLS-1$

  private ExporterHelper( )
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
    for( final Entry<String, Object> entry : tokens.entrySet() )
    {
      final String tokenname = entry.getKey();
      final String featureProperty = (String) entry.getValue();

      final IPropertyType pt = feature.getFeatureType().getProperty( featureProperty );
      final Object property = pt == null ? null : feature.getProperty( pt );
      String replace = null;
      if( property instanceof TimeseriesLinkType )
        replace = ((TimeseriesLinkType) property).getHref();
      else if( property != null )
        replace = property.toString();
      else if( property == null )
        replace = "!" + MSG_TOKEN_NOT_FOUND + ": " + tokenname + "!"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
    final List<String> values = new ArrayList<String>();

    for( final Entry<String, Object> entry : args.entrySet() )
    {
      final String argName = entry.getKey();

      if( argName.startsWith( prefix ) )
      {
        final String argValue = (String) entry.getValue();
        values.add( argValue );
      }
    }

    return values.toArray( new String[values.size()] );
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
  public static UrlArgument[] createUrlItems( final String argumentPrefix, final Arguments arguments, final URL context ) throws CoreException
  {
    final Collection<UrlArgument> items = new ArrayList<UrlArgument>();
    final Collection<IStatus> stati = new ArrayList<IStatus>();

    for( final Entry<String, Object> entry : arguments.entrySet() )
    {
      final String key = entry.getKey();
      if( key.startsWith( argumentPrefix ) )
      {
        final Arguments args = (Arguments) entry.getValue();
        final String label = args.getProperty( "label", "<unbekannt>" ); //$NON-NLS-1$ //$NON-NLS-2$
        final String strFile = args.getProperty( "href" ); //$NON-NLS-1$
        if( strFile == null )
          stati.add( new Status( IStatus.WARNING, KalypsoSimulationUIPlugin.getID(), 0, Messages.getString("org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.0") + key, null ) ); //$NON-NLS-1$

        try
        {
          items.add( new UrlArgument( label, UrlResolverSingleton.resolveUrl( context, strFile ), args ) );
        }
        catch( final MalformedURLException e )
        {
          stati.add( new Status( IStatus.WARNING, KalypsoSimulationUIPlugin.getID(), 0, Messages.getString("org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.1") + strFile, e ) ); //$NON-NLS-1$
        }
      }
    }

    if( stati.size() > 0 )
      throw new CoreException( new MultiStatus( KalypsoSimulationUIPlugin.getID(), 0, stati.toArray( new IStatus[stati.size()] ), Messages.getString("org.kalypso.simulation.ui.wizards.exporter.ExporterHelper.2"), null ) ); //$NON-NLS-1$

    return items.toArray( new UrlArgument[items.size()] );
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

    public String getLabel( )
    {
      return m_label;
    }

    public URL getUrl( )
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

    @Override
    public String toString( )
    {
      return m_label;
    }

    public Arguments getArguments( )
    {
      return m_args;
    }

    public void setLabel( final String name )
    {
      m_label = name;
    }
  }
}
