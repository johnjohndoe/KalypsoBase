package org.kalypso.model.wspm.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.kalypso.model.wspm.core.preferences.WspmCorePreferences;
import org.kalypso.model.wspm.core.profil.validator.IValidatorRule;
import org.kalypso.model.wspm.core.profil.validator.ValidatorFactory;
import org.kalypso.model.wspm.core.profil.validator.ValidatorRuleSet;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

public class KalypsoModelWspmCorePlugin extends Plugin
{
  public static final String MARKER_ID = "org.kalypso.model.wspm.ui.profilemarker"; //$NON-NLS-1$

  /** The shared instance. */
  private static KalypsoModelWspmCorePlugin PLUGIN;

  /**
   * Storage for preferences.
   */
  private ScopedPreferenceStore m_preferenceStore;

  public static KalypsoModelWspmCorePlugin getDefault( )
  {
    return PLUGIN;
  }

  public static String getID( )
  {
    return getDefault().getBundle().getSymbolicName();
  }

  /** The rules will will created (lazy) only once and used in every rule set. */

  private ValidatorFactory m_validatorFactory = null;

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );

    PLUGIN = this;
  }

  @Override
  public void stop( final BundleContext context ) throws Exception
  {
    if( m_preferenceStore != null )
    {
      try
      {
        InstanceScope.INSTANCE.getNode( getID() ).flush();
      }
      catch( final BackingStoreException e )
      {
        e.printStackTrace();
      }

      m_preferenceStore = null;
    }

    super.stop( context );


    PLUGIN = null;
  }

  public static ValidatorRuleSet getValidatorSet( final String type )
  {
    final ValidatorFactory vf = getDefault().getValidatorFactory();

    final IValidatorRule[] rules = vf.createValidatorRules( type );
    return new ValidatorRuleSet( rules );
  }

  public ValidatorFactory getValidatorFactory( )
  {
    if( m_validatorFactory == null )
    {
      m_validatorFactory = new ValidatorFactory();
    }

    return m_validatorFactory;
  }

  /**
   * Returns the preference store for this plug-in.
   * This preference store is used to hold persistent settings for this plug-in in
   * the context of a workbench. Some of these settings will be user controlled,
   * whereas others may be internal setting that are never exposed to the user.
   * <p>
   * If an error occurs reading the preference store, an empty preference store is quietly created, initialized with defaults, and returned.
   * </p>
   * <p>
   * <strong>NOTE:</strong> As of Eclipse 3.1 this method is no longer referring to the core runtime compatibility layer and so plug-ins relying on Plugin#initializeDefaultPreferences will have to
   * access the compatibility layer themselves.
   * </p>
   *
   * @return the preference store
   */
  public synchronized IPreferenceStore getPreferenceStore( )
  {
    // Create the preference store lazily.
    if( m_preferenceStore == null )
    {
      m_preferenceStore = new ScopedPreferenceStore( InstanceScope.INSTANCE, getID() );

      WspmCorePreferences.initDefaults( m_preferenceStore );
    }

    return m_preferenceStore;
  }
}