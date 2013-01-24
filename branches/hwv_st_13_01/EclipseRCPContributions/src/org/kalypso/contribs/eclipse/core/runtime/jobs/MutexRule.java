package org.kalypso.contribs.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * This rule excludes all rules except itself. Usefull if you want to prohibit that a group of jobs runs at the same
 * time. Just instantiate one mutex rule and set it to all jobs.
 * 
 * @author ? (original)
 * @author Holger Albert
 */
public class MutexRule implements ISchedulingRule
{
  /**
   * This name is only used in the to string function, if given. If it is null or empty, the normal toString() will be
   * done.
   */
  private final String m_name;

  public MutexRule( )
  {
    this( null );
  }

  /**
   * @param name
   *          This name is only used in the toString method, if given. If it is null or empty, the normal toString()
   *          will be done.
   */
  public MutexRule( final String name )
  {
    m_name = name;
  }

  @Override
  public boolean contains( final ISchedulingRule rule )
  {
    return rule == this;
  }

  @Override
  public boolean isConflicting( final ISchedulingRule rule )
  {
    return rule == this;
  }

  @Override
  public String toString( )
  {
    if( m_name == null || m_name.length() == 0 )
      return super.toString();

    return m_name + " - Mutex";
  }
}