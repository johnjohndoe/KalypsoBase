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
import org.kalypso.contribs.eclipse.EclipseRCPContributionsPlugin;
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
  private File m_projectFile;

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
  public SimpleProjectInfo( File projectFile )
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
      throw new CoreException( new Status( IStatus.ERROR, EclipseRCPContributionsPlugin.ID, "The file '.project' does not exist..." ) );

    /* The input stream. */
    InputStream is = null;

    try
    {
      /* Need an input stream. */
      is = new BufferedInputStream( new FileInputStream( m_projectFile ) );

      /* Get as DOM. */
      Document document = XMLHelper.getAsDOM( is, true );

      /* Get the element. */
      Element element = document.getDocumentElement();

      /* Get the child nodes. */
      NodeList childNodes = element.getChildNodes();
      for( int i = 0; i < childNodes.getLength(); i++ )
      {
        Node childNode = childNodes.item( i );
        if( childNode.getNodeType() != Node.ELEMENT_NODE )
          continue;

        if( childNode.getNodeName().equals( "name" ) )
        {
          m_name = childNode.getTextContent();
          continue;
        }

        if( childNode.getNodeName().equals( "natures" ) )
        {
          List<String> natures = new ArrayList<String>();
          NodeList natureNodes = childNode.getChildNodes();
          for( int j = 0; j < natureNodes.getLength(); j++ )
          {
            Node natureNode = natureNodes.item( i );
            if( natureNode.getNodeType() != Node.ELEMENT_NODE )
              continue;

            natures.add( natureNode.getTextContent() );
          }

          m_natures = natures.toArray( new String[] {} );
          continue;
        }
      }
    }
    catch( Exception ex )
    {
      throw new CoreException( new Status( IStatus.ERROR, EclipseRCPContributionsPlugin.ID, ex.getLocalizedMessage(), ex ) );
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
        catch( IOException ex )
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