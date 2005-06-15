package org.bce.eclipse.core.runtime.jobs;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/** This rule excludes all rules except itself */
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
