/*
 * Insert INFORM.DSS licence here.
 */
package org.kalypso.gml.ui;

import org.kalypso.commons.eclipse.core.runtime.PluginImageProvider.ImageKey;

/**
 * Utility class for handling images in this plugin.
 * 
 * @author belger
 */
public class KalypsoGmlUiImages
{
  public static enum DESCRIPTORS implements ImageKey
  {
    COVERAGE_ADD("icons/cview16/addCoverage.gif"),
    COVERAGE_REMOVE("icons/cview16/removeCoverage.gif"),
    COVERAGE_UP("icons/cview16/upCoverage.gif"),
    COVERAGE_DOWN("icons/cview16/downCoverage.gif"),
    COVERAGE_JUMP("icons/cview16/jumptoCoverage.gif");

    private final String m_imagePath;

    private DESCRIPTORS( final String imagePath )
    {
      m_imagePath = imagePath;
    }

    /**
     * @see org.kalypso.commons.eclipse.core.runtime.PluginImageProvider.ImageKey#getImagePath()
     */
    public String getImagePath( )
    {
      return m_imagePath;
    }
  }
}