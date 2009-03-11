package de.openali.odysseus.service.ods.util;

/**
 * @author burtscher
 */
public interface IODSConstants
{
  /**
   * System property key describing ODS configuration dir
   */
  public static final String ODS_CONFIG_PATH_KEY = "de.openali.odysseus.ods.configdir";

  /**
   * Default path to ODS configuration dir - only used if there's no system property set
   */
  public static final String ODS_CONFIG_PATH_DEFAULT = System.getProperty( "user.home" ) + "/.ods/";

  /**
   * System property key describing name of ods config file
   */
  public static final String ODS_CONFIG_FILENAME_KEY = "de.openali.odysseus.ods.configfile";

  /**
   * Default name of ODS configuration file - only used if there's no system property set
   */
  public static final String ODS_CONFIG_FILENAME_DEFAULT = "ODSConfiguration.xml";

  /**
   * Default name of ODS configuration file - only used if there's no system property set
   */
  public static final String ODS_CONFIG_TMPDIR_KEY = "de.openali.odysseus.ods.tmpdir";

  /**
   * Default path to ODS tmp dir - only used if there's no system property set
   */
  public static final String ODS_CONFIG_TMPDIR_DEFAULT = ODS_CONFIG_PATH_DEFAULT + "tmp/";

  public static final String ODS_SERVICE_SHORT = "ODS";

  public static final String ODS_VERSION = "0.2.0";

}
