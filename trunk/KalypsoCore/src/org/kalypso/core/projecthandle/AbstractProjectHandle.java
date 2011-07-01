package org.kalypso.core.projecthandle;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.PlatformObject;

public abstract class AbstractProjectHandle extends PlatformObject implements IProjectHandle, Comparable<IProjectHandle>
{
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo( final IProjectHandle handler )
  {
    return getName().compareTo( handler.getName() );
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof IProjectHandle )
    {
      final IProjectHandle handler = (IProjectHandle) obj;
      return getUniqueName().equals( handler.getUniqueName() );
    }
    else if( obj instanceof IProject )
    {
      final IProject other = (IProject) obj;
      return getUniqueName().equals( other.getName() );
    }

    return super.equals( obj );
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getUniqueName() );

    return builder.toHashCode();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return String.format( "Project: %s", getName() );
  }
}
