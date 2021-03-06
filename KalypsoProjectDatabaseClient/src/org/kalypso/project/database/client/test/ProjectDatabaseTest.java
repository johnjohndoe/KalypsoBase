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
package org.kalypso.project.database.client.test;

import org.junit.Test;
import org.kalypso.project.database.client.KalypsoProjectDatabaseClient;
import org.kalypso.project.database.sei.IProjectDatabase;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;

/**
 * @author Dirk Kuch
 */
public class ProjectDatabaseTest
{
  private static final String PROJECT_TYPE = "PlanerClientProject"; //$NON-NLS-1$

  // FIXME: clean this test or remove it
// @Test
// public void testCreateProjects( )
// {
// for( int i = 0; i < 10; i++ )
// {
//      createProject( "project_one", i ); //$NON-NLS-1$
//      createProject( "project_two", i ); //$NON-NLS-1$
// }
// }

// public void createProject( final String name, final int version )
// {
// try
// {
// // copy project.zip to server incoming directory
// final URL project = ProjectDatabaseTest.class.getResource( "data/project.zip" );
// final FileSystemManager manager = VFSUtilities.getManager();
// final FileObject src = manager.resolveFile( project.toExternalForm() );
//
// final String url = ProjectModelUrlResolver.getUrlAsWebdav( new ProjectModelUrlResolver.IResolverInterface()
// {
// @Override
// public String getPath( )
// {
// return System.getProperty( IProjectDataBaseClientConstant.CLIENT_WRITEABLE_PATH );
// }
//
// }, "test.zip" );
//
// final FileObject destination = manager.resolveFile( url );
// VFSUtilities.copy( src, destination );
//
// /* create project */
// final IProjectDatabase service = KalypsoProjectDatabaseClient.getService();
//
// final URL myUrl = ProjectModelUrlResolver.getUrlAsHttp( new ProjectModelUrlResolver.IResolverInterface()
// {
// @Override
// public String getPath( )
// {
// return System.getProperty( IProjectDataBaseClientConstant.CLIENT_READABLE_PATH );
// }
//
// }, "test.zip" );
//
// final KalypsoProjectBean bean = new KalypsoProjectBean();
// bean.setName( name );
// bean.setDescription( name );
// bean.setUnixName( name );
// bean.setProjectVersion( version );
// bean.setProjectType( PROJECT_TYPE );
//
// final KalypsoProjectBean myBean = service.createProject( bean, myUrl );
// Assert.assertNotNull( myBean );
//
// destination.delete();
//
// // KalypsoProjectBeanWrapper wrapper = new KalypsoProjectBeanWrapper( bean );
// // FileObject dest = wrapper.getFileObject( access );
// // Assert.assertTrue( dest.exists() );
// }
// catch( final Exception e )
// {
// e.printStackTrace();
// }
// }

  @Test
  public void testGetProjects( )
  {
    final IProjectDatabase service = KalypsoProjectDatabaseClient.getService();
    final KalypsoProjectBean[] projects = service.getProjectHeads( PROJECT_TYPE );

    for( final KalypsoProjectBean project : projects )
    {
      System.out.println( String.format( "Project: %s ", project.getName() ) ); //$NON-NLS-1$
    }
  }

}
