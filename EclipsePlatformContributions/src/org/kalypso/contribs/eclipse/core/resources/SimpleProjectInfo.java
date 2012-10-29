/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.core.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;
import org.kalypso.contribs.java.xml.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class reads the .project file and stores basic information of a project. This is usefull, if you have a project
 * outside the current eclipse workspace, but need information like name and natures of it. <strong>Not tested!</strong>
 *
 * @author Holger Albert
 */
public class SimpleProjectInfo
{
  /**
   * The project file.
   */
  private final File m_projectFile;

  /**
   * The name of the project.
   */
  private String m_name;

  /**
   * The natures of the project.
   */
  private String[] m_natures;

  /**
   * The constructor.
   *
   * @param projectFile
   *          The project file.
   */
  public SimpleProjectInfo( final File projectFile )
  {
    m_projectFile = projectFile;
    m_name = "Unknown";
    m_natures = new String[] {};
  }

  /**
   * This function reads the .project file and stores basic information of the project.
   */
  public void read( ) throws CoreException
  {
    if( m_projectFile == null || !m_projectFile.exists() )
      throw new CoreException( new Status( IStatus.ERROR, EclipsePlatformContributionsPlugin.getID(), "The file '.project' does not exist..." ) );

    /* The input stream. */
    InputStream is = null;

    try
    {
      /* Need an input stream. */
      is = new BufferedInputStream( new FileInputStream( m_projectFile ) );

      /* Get as DOM. */
      final Document document = XMLHelper.getAsDOM( is, true );

      /* Get the element. */
      final Element element = document.getDocumentElement();

      /* Get the child nodes. */
      final NodeList childNodes = element.getChildNodes();
      for( int i = 0; i < childNodes.getLength(); i++ )
      {
        final Node childNode = childNodes.item( i );
        if( childNode.getNodeType() != Node.ELEMENT_NODE )
          continue;

        if( childNode.getNodeName().equals( "name" ) )
        {
          m_name = childNode.getTextContent();
          continue;
        }

        if( childNode.getNodeName().equals( "natures" ) )
        {
          final List<String> natures = new ArrayList<>();
          final NodeList natureNodes = childNode.getChildNodes();
          for( int j = 0; j < natureNodes.getLength(); j++ )
          {
            final Node natureNode = natureNodes.item( i );
            if( natureNode.getNodeType() != Node.ELEMENT_NODE )
              continue;

            natures.add( natureNode.getTextContent() );
          }

          m_natures = natures.toArray( new String[] {} );
          continue;
        }
      }
    }
    catch( final Exception ex )
    {
      throw new CoreException( new Status( IStatus.ERROR, EclipsePlatformContributionsPlugin.getID(), ex.getLocalizedMessage(), ex ) );
    }
    finally
    {
      /* Close the input stream. */
      if( is != null )
      {
        try
        {
          is.close();
        }
        catch( final IOException ex )
        {
          /* Ignore. */
          ex.printStackTrace();
        }
      }
    }
  }

  /**
   * This function returns the directory of the project.
   *
   * @return The directory of the project.
   */
  public File getDirectory( )
  {
    return m_projectFile.getParentFile();
  }

  /**
   * This function returns the name of the project.
   *
   * @return The name of the project.
   */
  public String getName( )
  {
    return m_name;
  }

  /**
   * This function returns the natures of the project.
   *
   * @return The natures of the project.
   */
  public String[] getNatures( )
  {
    return m_natures;
  }
}