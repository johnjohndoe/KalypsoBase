package org.kalypso.service.ods.util;

/**
 * @author burtscher
 */
public interface IODSConstants
{
  /**
   * System property key describing ODS configuration dir
   */
  public static final String ODS_CONFIG_PATH_KEY = "kalypso.ods.config.path";

  /**
   * Default path to ODS configuration dir - only used if there's no system property set
   */
  public static final String ODS_CONFIG_PATH_DEFAULT = System.getProperty( "user.home" ) + "/.ods/";

  /**
   * System property key describing name of ods config file
   */
  public static final String ODS_CONFIG_NAME_KEY = "kalypso.ods.config.name";

  /**
   * Default name of ODS configuration file - only used if there's no system property set
   */
  public static final String ODS_CONFIG_NAME_DEFAULT = "ODSConfiguration.xml";

  public static final String ODS_VERSION = "0.1.0";
}
