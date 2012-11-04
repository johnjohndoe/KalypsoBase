package org.kalypso.gml.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.gml.ui.coverage.CoverageManagementAction;

/**
 * Manages extension points of the gml ui plugin.
 * 
 * @author Holger Albert
 */
public class KalypsoGmlUiExtensions
{
  private static final String COVERAGE_MANAGEMENT_ACTION_EXTENSION_POINT = "org.kalypso.gml.ui.coverageManagementAction"; //$NON-NLS-1$

  private static final String COVERAGE_MANAGEMENT_ACTION_ACTION_ELEMENT = "action"; //$NON-NLS-1$

  // private static final String COVERAGE_MANAGEMENT_ACTION_ACTION_ID = "id"; //$NON-NLS-1$

  private static final String COVERAGE_MANAGEMENT_ACTION_ACTION_ROLE = "role"; //$NON-NLS-1$

  private static final String COVERAGE_MANAGEMENT_ACTION_ACTION_CLASS = "class"; //$NON-NLS-1$

  /**
   * The constructor.
   */
  private KalypsoGmlUiExtensions( )
  {
  }

//  /**
//   * This function creates and returns the coverage management action with the given id, if one is registered.
//   * 
//   * @param id
//   *          The id of the coverage management action.
//   * @return The coverage management action.
//   */
//  public static CoverageManagementAction createCoverageManagementAction( final String id ) throws CoreException
//  {
//    /* Assert. */
//    Assert.isNotNull( id );
//
//    /* Get the extension registry. */
//    final IExtensionRegistry registry = Platform.getExtensionRegistry();
//
//    /* Get the extension point. */
//    final IExtensionPoint extensionPoint = registry.getExtensionPoint( COVERAGE_MANAGEMENT_ACTION_EXTENSION_POINT );
//
//    /* Get all configuration elements. */
//    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
//    for( final IConfigurationElement element : configurationElements )
//    {
//      /* If the configuration element is not the action element, continue. */
//      if( !COVERAGE_MANAGEMENT_ACTION_ACTION_ELEMENT.equals( element.getName() ) )
//        continue;
//
//      /* Get the attributes. */
//      final String actionId = element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ID );
//      final String actionRole = element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ROLE );
//
//      /* Does the ids match? */
//      if( id.equals( actionId ) )
//      {
//        /* Create the coverage management action. */
//        final CoverageManagementAction action = (CoverageManagementAction)element.createExecutableExtension( COVERAGE_MANAGEMENT_ACTION_ACTION_CLASS );
//        action.init( actionRole );
//
//        return action;
//      }
//    }
//
//    throw new CoreException( new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), String.format( "Keine CoverageManagementAction mit ID '%s' gefunden.", id ), null ) ); //$NON-NLS-1$
//  }

  /**
   * This function creates and returns the coverage management actions.
   * 
   * @return The coverage management actions.
   */
  public static CoverageManagementAction[] createCoverageManagementActions( ) throws CoreException
  {
    /* Memory for the results. */
    final List<CoverageManagementAction> result = new ArrayList<>();

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
      // not needed: keep for later, maybe we want to reference by id sometimes
      // final String actionId = element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ID );
      final String actionRole = element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ROLE );

      /* Create the coverage management action. */
      final CoverageManagementAction action = (CoverageManagementAction)element.createExecutableExtension( COVERAGE_MANAGEMENT_ACTION_ACTION_CLASS );
      action.init( actionRole );

      /* Add the coverage management action. */
      result.add( action );
    }

    return result.toArray( new CoverageManagementAction[] {} );
  }

//  /**
//   * This function returns the ids of all coverage management actions.
//   * 
//   * @return The ids of all coverage management actions.
//   */
//  public static String[] getCoverageManagementActionIds( )
//  {
//    /* Memory for the results. */
//    final List<String> result = new ArrayList<>();
//
//    /* Get the extension registry. */
//    final IExtensionRegistry registry = Platform.getExtensionRegistry();
//
//    /* Get the extension point. */
//    final IExtensionPoint extensionPoint = registry.getExtensionPoint( COVERAGE_MANAGEMENT_ACTION_EXTENSION_POINT );
//
//    /* Get all configuration elements. */
//    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
//    for( final IConfigurationElement element : configurationElements )
//    {
//      /* If the configuration element is not the action element, continue. */
//      if( !COVERAGE_MANAGEMENT_ACTION_ACTION_ELEMENT.equals( element.getName() ) )
//        continue;
//
//      /* Add the action id. */
//      result.add( element.getAttribute( COVERAGE_MANAGEMENT_ACTION_ACTION_ID ) );
//    }
//
//    return result.toArray( new String[] {} );
//  }
}