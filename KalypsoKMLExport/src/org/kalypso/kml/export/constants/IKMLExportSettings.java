/**
 *
 */
package org.kalypso.kml.export.constants;

import java.io.File;

/**
 * @author Dirk Kuch
 */
public interface IKMLExportSettings
{
  String CONST_TARGET_FILE = "kmlExportTargetFile"; //$NON-NLS-1$

  String getExportDescription( );

  File getExportFile( );

  String getExportName( );
}
