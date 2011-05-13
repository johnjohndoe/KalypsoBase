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
package org.kalypso.project.database.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jws.WebService;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.kalypso.commons.io.VFSUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.project.database.IProjectDataBaseServerConstant;
import org.kalypso.project.database.KalypsoProjectDatabase;
import org.kalypso.project.database.KalypsoProjectDatabaseExtensions;
import org.kalypso.project.database.sei.IProjectDatabase;
import org.kalypso.project.database.sei.beans.KalypsoProjectBean;
import org.kalypso.project.database.sei.beans.KalypsoProjectBeanPrimaryKey;
import org.kalypso.project.database.server.trigger.TriggerHelper;

/**
 * @author Dirk Kuch
 */
@WebService(endpointInterface = "org.kalypso.project.database.sei.IProjectDatabase")
public class ProjectDatabase implements IProjectDatabase
{
  private SessionFactory m_factory = null;

  public ProjectDatabase( )
  {
    try
    {
      // FIXME: better error handling needed if factory cannot be created -> lots of NPE later
      final String property = System.getProperty( IProjectDataBaseServerConstant.HIBERNATE_CONFIG_FILE );
      if( property != null && !"".equals( property.trim() ) ) //$NON-NLS-1$
      {
        AnnotationConfiguration configure = null;

        try
        {
          final URL url = new URL( property );
          configure = new AnnotationConfiguration().configure( url );

          configure.addAnnotatedClass( KalypsoProjectBean.class );
          configure.addAnnotatedClass( KalypsoProjectBeanPrimaryKey.class );
          m_factory = configure.buildSessionFactory();
        }
        catch( final Exception ex )
        {
          final String msg = String.format( "Configuration error - couldn't find hibernate config file for KalypsoProjectData setup. location: %s\nStarting KalypsoProjectDatabase with default configuration!", property ); //$NON-NLS-1$
          System.out.println( msg ); //$NON-NLS-1$

          KalypsoProjectDatabase.getDefault().getLog().log( StatusUtilities.createErrorStatus( msg, ex ) );
        }
      }
    }
    catch( final Exception e )
    {
      KalypsoProjectDatabase.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  public void dispose( ) // NO_Ufinal CD
  {
    if( m_factory != null )
      m_factory.close();
  }

  /**
   * @see org.kalypso.projectfinal .database.sei.IProjectDatabase#getProjects()
   */
  @Override
  public KalypsoProjectBean[] getProjectHeads( final String projectType )
  {
    synchronized( this )
    {
      /** Getting the Session Factory and session */
      final Session session = m_factory.getCurrentSession();

      /** Starting the Transaction */
      final Transaction tx = session.beginTransaction();

      /* names of existing projects */
      final List< ? > names = session.createQuery( String.format( "select m_unixName from KalypsoProjectBean where m_projectType = '%s' ORDER by m_name", projectType ) ).list(); //$NON-NLS-1$
      tx.commit();

      final Set<String> projects = new HashSet<String>();

      for( final Object object : names )
      {
        if( !(object instanceof String) )
          continue;

        final String name = object.toString();
        projects.add( name );
      }

      final List<KalypsoProjectBean> projectBeans = new ArrayList<KalypsoProjectBean>();

      for( final String project : projects )
      {
        final TreeMap<Integer, KalypsoProjectBean> myBeans = new TreeMap<Integer, KalypsoProjectBean>();

        final Session mySession = m_factory.getCurrentSession();
        final Transaction myTx = mySession.beginTransaction();
        final List< ? > beans = mySession.createQuery( String.format( "from KalypsoProjectBean where m_unixName = '%s'  ORDER by m_projectVersion", project ) ).list(); //$NON-NLS-1$
        myTx.commit();

        for( final Object object : beans )
        {
          if( !(object instanceof KalypsoProjectBean) )
            continue;

          final KalypsoProjectBean b = (KalypsoProjectBean) object;
          myBeans.put( b.getProjectVersion(), b );
        }

        final Integer[] keys = myBeans.keySet().toArray( new Integer[] {} );

        /* determine head */
        final KalypsoProjectBean head = myBeans.get( keys[keys.length - 1] );

        KalypsoProjectBean[] values = myBeans.values().toArray( new KalypsoProjectBean[] {} );
        values = (KalypsoProjectBean[]) ArrayUtils.remove( values, values.length - 1 ); // remove last entry -> cycle!

        // TODO check needed? - order by clauses
        Arrays.sort( values, new Comparator<KalypsoProjectBean>()
        {
          @Override
          public int compare( final KalypsoProjectBean o1, final KalypsoProjectBean o2 )
          {
            return o1.getProjectVersion().compareTo( o2.getProjectVersion() );
          }
        } );

        head.setChildren( values );
        projectBeans.add( head );
      }

      return projectBeans.toArray( new KalypsoProjectBean[] {} );
    }

  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#getProject()
   */
  @Override
  public KalypsoProjectBean getProject( final String projectUnixName )
  {
    synchronized( this )
    {
      /** Getting the Session Factory and session */
      final Session session = m_factory.getCurrentSession();

      /** Starting the Transaction */
      final Transaction tx = session.beginTransaction();

      /* names of exsting projects */
      final List< ? > projects = session.createQuery( String.format( "from KalypsoProjectBean where m_unixName = '%s' ORDER by m_projectVersion desc", projectUnixName ) ).list(); //$NON-NLS-1$
      tx.commit();

      if( projects.size() <= 0 )
        return null;

      /* determine head */
      final KalypsoProjectBean head = (KalypsoProjectBean) projects.get( 0 );

      final List<KalypsoProjectBean> beans = new ArrayList<KalypsoProjectBean>();
      for( int i = 1; i < projects.size(); i++ )
        beans.add( (KalypsoProjectBean) projects.get( i ) );

      head.setChildren( beans.toArray( new KalypsoProjectBean[] {} ) );

      return head;
    }
  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#createProject(java.lang.String)
   */
  @Override
  public KalypsoProjectBean createProject( final KalypsoProjectBean bean, final URL incoming ) throws IOException
  {
    synchronized( this )
    {
      final FileSystemManager manager = VFSUtilities.getManager();
      final FileObject src = manager.resolveFile( incoming.toExternalForm() );

      try
      {
        if( !src.exists() )
          throw new FileNotFoundException( String.format( "Incoming file not exists: %s", incoming.toExternalForm() ) ); //$NON-NLS-1$

        /* destination of incoming file */
        final String urlDestination = ProjectDatabaseHelper.resolveDestinationUrl( bean );

        final FileObject destination = manager.resolveFile( urlDestination );
        VFSUtilities.copy( src, destination );

        /* store project bean in database */
        bean.setCreationDate( Calendar.getInstance().getTime() );

        final Session session = m_factory.getCurrentSession();
        final Transaction tx = session.beginTransaction();
        session.save( bean );

        tx.commit();

        final IConfigurationElement confElementTrigger = KalypsoProjectDatabaseExtensions.getProjectDatabaseTriggers( bean.getProjectType() );
        if( confElementTrigger != null )
          TriggerHelper.handleBean( bean, confElementTrigger );

        return bean;
      }
      catch( final Exception e )
      {
        throw new IOException( e.getMessage(), e );
      }
    }

  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#updateProject(java.lang.String)
   */
  @Override
  public KalypsoProjectBean udpateProject( final KalypsoProjectBean bean, final URL incoming ) throws IOException
  {
    synchronized( this )
    {
      /* get head */
      final KalypsoProjectBean head = getProject( bean.getUnixName() );
      bean.setProjectVersion( head.getProjectVersion() + 1 );

      return createProject( bean, incoming );
    }

  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#acquireProjectEditLock(org.kalypso.project.database.sei.beans.KalypsoProjectBean)
   */
  @Override
  public String acquireProjectEditLock( final String projectUnixName )
  {
    synchronized( this )
    {
      // TODO lock already acquired
      final Session mySession = m_factory.getCurrentSession();
      final Transaction myTx = mySession.beginTransaction();

      final String ticket = String.format( "Ticket%d", Calendar.getInstance().getTime().hashCode() ); //$NON-NLS-1$

      final DateFormat sdf = new SimpleDateFormat( "yyyy-mm-dd hh:mm:ss" ); //$NON-NLS-1$
      final String now = sdf.format( new Date() );

      final int updated = mySession.createQuery( String.format( "update KalypsoProjectBean set m_editLockTicket = '%s', edit_lock_date = '%s' where m_unixName = '%s'", ticket, now, projectUnixName ) ).executeUpdate(); //$NON-NLS-1$
      myTx.commit();

      if( updated == 0 )
        return null;

      final KalypsoProjectBean project = getProject( projectUnixName );
      if( !project.hasEditLock() )
        throw new IllegalStateException( "Updating edit lock of projects failed." ); //$NON-NLS-1$

      final KalypsoProjectBean[] children = project.getChildren();
      for( final KalypsoProjectBean child : children )
        if( !child.hasEditLock() )
          throw new IllegalStateException( "Updating edit lock of projects failed." ); //$NON-NLS-1$

      return ticket;
    }

  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#releaseProjectEditLock(java.lang.String, java.lang.String)
   */
  @Override
  public Boolean releaseProjectEditLock( final String projectUnixName, final String ticketId )
  {
    synchronized( this )
    {
      // TODO lock already released
      final Session mySession = m_factory.getCurrentSession();
      final Transaction myTx = mySession.beginTransaction();

      mySession.createQuery( String.format( "update KalypsoProjectBean set m_editLockTicket = '' where m_unixName = '%s' and m_editLockTicket = '%s'", projectUnixName, ticketId ) ).executeUpdate(); //$NON-NLS-1$
      myTx.commit();

      final KalypsoProjectBean project = getProject( projectUnixName );
      if( project.hasEditLock() )
        return false;

      final KalypsoProjectBean[] children = project.getChildren();
      for( final KalypsoProjectBean child : children )
        if( child.hasEditLock() )
          return false;

      return true;
    }
  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#getProjectTypes()
   */
  @SuppressWarnings("unchecked")
  @Override
  public String[] getProjectTypes( )
  {
    if( m_factory == null )
      return new String[] {};

    /** Getting the Session Factory and session */
    final Session session = m_factory.getCurrentSession();

    /** Starting the Transaction */
    final Transaction tx = session.beginTransaction();

    /* list of project types */
    final List<String> projects = session.createQuery( "Select distinct m_projectType from KalypsoProjectBean ORDER by m_projectType" ).list(); //$NON-NLS-1$
    tx.commit();

    return projects.toArray( new String[] {} );
  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#getProjectHeads()
   */
  @Override
  public KalypsoProjectBean[] getAllProjectHeads( )
  {
    final Set<KalypsoProjectBean> myBeans = new TreeSet<KalypsoProjectBean>();

    final String[] types = getProjectTypes();
    for( final String type : types )
    {
      final KalypsoProjectBean[] beans = getProjectHeads( type );
      for( final KalypsoProjectBean bean : beans )
        myBeans.add( bean );
    }

    return myBeans.toArray( new KalypsoProjectBean[] {} );
  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#ping()
   */
  @Override
  public Boolean ping( )
  {
    return Boolean.TRUE;
  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#deleteProject(org.kalypso.project.database.sei.beans.KalypsoProjectBean)
   */
  @Override
  public Boolean deleteProject( final KalypsoProjectBean bean )
  {
    synchronized( this )
    {
      return ProjectDatabaseHelper.removeBean( m_factory.getCurrentSession(), bean );
    }
  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#forceUnlock(org.kalypso.project.database.sei.beans.KalypsoProjectBean)
   */
  @Override
  public void forceUnlock( final KalypsoProjectBean bean )
  {
    synchronized( this )
    {
      final Session mySession = m_factory.getCurrentSession();
      final Transaction myTx = mySession.beginTransaction();

      final String unixName = bean.getUnixName();

      mySession.createQuery( String.format( "update KalypsoProjectBean set m_editLockTicket = '' where m_unixName = '%s'", unixName ) ).executeUpdate(); //$NON-NLS-1$
      myTx.commit();
    }
  }

  /**
   * @see org.kalypso.project.database.sei.IProjectDatabase#setProjectDescription(org.kalypso.project.database.sei.beans.KalypsoProjectBean,
   *      java.lang.String)
   */
  @Override
  public void setProjectDescription( final KalypsoProjectBean bean, final String description )
  {
    synchronized( this )
    {
      final Session mySession = m_factory.getCurrentSession();
      final Transaction myTx = mySession.beginTransaction();

      mySession.createQuery( String.format( "update KalypsoProjectBean set m_description = '%s' where m_unixName = '%s'", description, bean.getUnixName() ) ).executeUpdate(); //$NON-NLS-1$
      myTx.commit();
    }
  }
}
