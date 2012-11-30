package de.renew.workflow.connector.context;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;

/**
 * Interface to implement in order to be notified if the active scenario has changed.
 *
 * @author Patrice Congo, Stefan Kurzbach
 */
public interface IActiveScenarioChangeListener
{
  void activeScenarioChanged( final ScenarioHandlingProjectNature newProject, final IScenario caze );
}