package org.kalypso.contribs.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * This rule excludes all rules except itself
 * <p>
 * Usefull if you want to prohibit that a group of jobs runs at the same time.
 * </p>
 * Just instantiate one mutex rule and set it to all jobs.
 * </p>
 */
public class MutexRule implements ISchedulingRule
{
  public boolean contains( final ISchedulingRule rule )
  {
    return rule == this;
  }

  public boolean isConflicting( final ISchedulingRule rule )
  {
    return rule == this;
  }
}
