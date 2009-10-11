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
    COVERAGE_ADD("icons/cview16/addCoverage.gif"), //$NON-NLS-1$
    COVERAGE_REMOVE("icons/cview16/removeCoverage.gif"), //$NON-NLS-1$
    COVERAGE_EXPORT("icons/cview16/exportCoverage.gif"), //$NON-NLS-1$
    COVERAGE_UP("icons/cview16/upCoverage.gif"), //$NON-NLS-1$
    COVERAGE_DOWN("icons/cview16/downCoverage.gif"), //$NON-NLS-1$
    COVERAGE_JUMP("icons/cview16/jumptoCoverage.gif"), //$NON-NLS-1$
    STYLE_EDIT("icons/cview16/style_edit.gif"); //$NON-NLS-1$

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