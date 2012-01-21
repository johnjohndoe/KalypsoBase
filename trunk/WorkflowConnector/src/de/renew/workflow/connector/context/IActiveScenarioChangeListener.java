package de.renew.workflow.connector.context;

import de.renew.workflow.connector.cases.CaseHandlingProjectNature;
import de.renew.workflow.connector.cases.IScenario;

/**
 * Interface to implement in order to be notified if the active scenario has changed.
 * 
 * @author Patrice Congo, Stefan Kurzbach
 */
public interface IActiveScenarioChangeListener<T extends IScenario>
{
  void activeScenarioChanged( final CaseHandlingProjectNature<T> newProject, final T caze );
}