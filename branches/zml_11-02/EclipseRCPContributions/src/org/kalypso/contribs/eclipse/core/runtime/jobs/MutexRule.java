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
  private String m_name;

  /**
   * The constructor.
   */
  public MutexRule( )
  {
    m_name = null;
  }

  /**
   * The constructor.
   * 
   * @param name
   *          This name is only used in the to string function, if given. If it is null or empty, the normal toString()
   *          will be done.
   */
  public MutexRule( String name )
  {
    m_name = name;
  }

  /**
   * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
   */
  @Override
  public boolean contains( final ISchedulingRule rule )
  {
    return rule == this;
  }

  /**
   * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
   */
  @Override
  public boolean isConflicting( final ISchedulingRule rule )
  {
    return rule == this;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    if( m_name == null || m_name.length() == 0 )
      return super.toString();

    return m_name + " - Mutex";
  }
}