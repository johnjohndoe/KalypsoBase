package org.kalypso.gml.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.gml.ui.internal.coverage.actions.CoverageManagementAction;

/**
 * Manages extension points of the gml ui plugin.
 * 
 * @author Holger Albert
 */
public class KalypsoGmlUiExtensions
{
  private static final String COVERAGE_MANAGEMENT_ACTION_EXTENSION_POINT = "org.kalypso.gml.ui.coverageManagementAction";

  private static final String COVERAGE_MANAGEMENT_ACTION_ACTION_ELEMENT = "action";

  private static final String COVERAGE_MANAGEMENT_ACTION_ACTION_ID = "id";

  private static final String COVERAGE_MANAGEMENT_ACTION_ACTION_ROLE = "role";

  private static final String COVERAGE_MANAGEMENT_ACTION_ACTION_CLASS = "class";

  /**
   * The constructor.
   */
  private KalypsoGmlUiExtensions( )
  {
  }

  /**
   * This function creates and returns the coverage management action with the given id, if one is registered.
   * 
   * @param id
   *          The id of the coverage management action.
   * @return The coverage management action.
   */
  public static CoverageManagementAction createCoverageManagementAction( final String id ) throws CoreException
  {
    /* Assert. */
    Assert.isNotNull( id );

    /* Get the extension registry. */
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    /* Get the extension point. */
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( COVERAGE_MANAGEMENT_ACTION_EXTENSION_POINT );

    /* Get all configuration elements. */
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement element : configurationElements )
    {
      /* If the configuration element is not the action element, continue. */
      if( !COVERAGE_MANAGEMENT_ACTION_ACTION_ELEMENT.equals( element.getName() ) )
        continue;

      /* Get the attributes. */
      final String actionId = element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ID );
      final String actionRole = element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ROLE );

      /* Does the ids match? */
      if( id.equals( actionId ) )
      {
        /* Create the coverage management action. */
        final CoverageManagementAction action = (CoverageManagementAction) element.createExecutableExtension( COVERAGE_MANAGEMENT_ACTION_ACTION_CLASS );
        action.init( actionId, actionRole );

        return action;
      }
    }

    throw new CoreException( new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), String.format( "Keine CoverageManagementAction mit ID '%s' gefunden.", id ), null ) );
  }

  /**
   * This function creates and returns the coverage management actions.
   * 
   * @return The coverage management actions.
   */
  public static CoverageManagementAction[] createCoverageManagementActions( ) throws CoreException
  {
    /* Memory for the results. */
    final List<CoverageManagementAction> result = new ArrayList<CoverageManagementAction>();

    /* Get the extension registry. */
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    /* Get the extension point. */
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( COVERAGE_MANAGEMENT_ACTION_EXTENSION_POINT );

    /* Get all configuration elements. */
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement element : configurationElements )
    {
      /* If the configuration element is not the action element, continue. */
      if( !COVERAGE_MANAGEMENT_ACTION_ACTION_ELEMENT.equals( element.getName() ) )
        continue;

      /* Get the attributes. */
      final String actionId = element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ID );
      final String actionRole = element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ROLE );

      /* Create the coverage management action. */
      final CoverageManagementAction action = (CoverageManagementAction) element.createExecutableExtension( COVERAGE_MANAGEMENT_ACTION_ACTION_CLASS );
      action.init( actionId, actionRole );

      /* Add the coverage management action. */
      result.add( action );
    }

    return result.toArray( new CoverageManagementAction[] {} );
  }

  /**
   * This function returns the ids of all coverage management actions.
   * 
   * @return The ids of all coverage management actions.
   */
  public static String[] getCoverageManagementActionIds( )
  {
    /* Memory for the results. */
    final List<String> result = new ArrayList<String>();

    /* Get the extension registry. */
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    /* Get the extension point. */
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( COVERAGE_MANAGEMENT_ACTION_EXTENSION_POINT );

    /* Get all configuration elements. */
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement element : configurationElements )
    {
      /* If the configuration element is not the action element, continue. */
      if( !COVERAGE_MANAGEMENT_ACTION_ACTION_ELEMENT.equals( element.getName() ) )
        continue;

      /* Add the action id. */
      result.add( element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ID ) );
    }

    return result.toArray( new String[] {} );
  }
}