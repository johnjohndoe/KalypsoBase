package de.renew.workflow.connector.cases;

/**
 * @author Stefan Kurzbach
 */
public interface ICaseManagerListener
{
  public void caseAdded( final IScenario scneario );

  public void caseRemoved( final IScenario scenario );
}
