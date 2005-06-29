package org.kalypso.metaproject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Comparator;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author schlienger
 */
public class MetaProject
{
  private final IProject m_project;

  private final MetaProject[] m_dependencies;

  public MetaProject( final IProject project, final MetaProject[] dependencies )
  {
    m_project = project;
    m_dependencies = dependencies;
  }

  public MetaProject[] getDependencies()
  {
    return m_dependencies;
  }

  public IProject getProject()
  {
    return m_project;
  }

  public boolean hasDependency( final Project project )
  {
    for( int i = 0; i < m_dependencies.length; i++ )
    {
      if( m_dependencies[i].equals( project ) )
        return true;

      if( m_dependencies[i].hasDependency( project ) )
        return true;
    }

    return false;
  }

  public String toString()
  {
    final StringBuffer bf = new StringBuffer();

    bf.append( m_project.getName() ).append( "\n" );

    for( int i = 0; i < m_dependencies.length; i++ )
      bf.append( "\t" ).append( m_dependencies[i].toString() );

    return bf.toString();
  }

  public String getDescription()
  {
    final IFile readme = m_project.getFile( "readme.txt" );
    if( readme.exists() )
    {
      InputStream contents = null;
      try
      {
        contents = readme.getContents();

        final StringWriter writer = new StringWriter();
        CopyUtils.copy( contents, writer, readme.getCharset() );

        contents.close();

        return writer.toString();
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
      finally
      {
        IOUtils.closeQuietly( contents );
      }
    }

    return "";
  }

  public void setDescription( final String text )
  {
    final IFile readme = m_project.getFile( "readme.txt" );

    final Job job = new Job( "Saving File " + readme.getName() )
    {
      protected IStatus run( final IProgressMonitor monitor )
      {
        try
        {
          if( readme.exists() )
            readme.setContents( new ByteArrayInputStream( text.getBytes( readme.getCharset() ) ), true, true, monitor );
          else
            readme.create( new ByteArrayInputStream( text.getBytes( readme.getCharset() ) ), true, monitor );
        }
        catch( final Exception e )
        {
          return new Status( IStatus.ERROR, MetaProjectPlugin.getId(), 0,
              "Error while saving file " + readme.getName(), e );
        }

        return Status.OK_STATUS;
      }
    };

    job.schedule();
  }

  public static class ProjectDependencyComparator implements Comparator
  {
    public int compare( Object arg1, Object arg2 )
    {
      final IProject p1;
      if( arg1 instanceof IProject )
        p1 = (IProject)arg1;
      else if( arg1 instanceof MetaProject )
        p1 = ( (MetaProject)arg1 ).getProject();
      else
        throw new IllegalArgumentException();

      final IProject p2;
      if( arg2 instanceof IProject )
        p2 = (IProject)arg2;
      else if( arg2 instanceof MetaProject )
        p2 = ( (MetaProject)arg2 ).getProject();
      else
        throw new IllegalArgumentException();

      if( p1.equals( p2 ) )
        return 0;

      return -1;
    }
  }
}
