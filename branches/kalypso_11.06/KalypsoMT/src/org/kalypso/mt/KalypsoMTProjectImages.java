/*
 * Insert INFORM.DSS licence here.
 */
package org.kalypso.mt;

import org.kalypso.commons.eclipse.core.runtime.PluginImageProvider.ImageKey;

/**
 * Utility class for handling images in this plugin.
 * 
 * @author belger
 */
public class KalypsoMTProjectImages
{
  public static enum DESCRIPTORS implements ImageKey
  {
    ARROW_RIGHT("icons/arrowRight.png"), //$NON-NLS-1$
    ADD_PANEL("icons/addPanel.png"), //$NON-NLS-1$
    UNDO("icons/backandforth.png"), //$NON-NLS-1$
    CANCEL("icons/cancel.png"), //$NON-NLS-1$
    DELETE_TOOLBOX("icons/deleteToolbox.png"), //$NON-NLS-1$
    PLUS("icons/plus.png"), //$NON-NLS-1$
    TOGGLE("icons/toggle.png"); //$NON-NLS-1$

    private final String m_imagePath;

    private DESCRIPTORS( final String imagePath )
    {
      m_imagePath = imagePath;
    }

    /**
     * @see org.kalypso.commons.eclipse.core.runtime.PluginImageProvider.ImageKey#getImagePath()
     */
    @Override
    public String getImagePath( )
    {
      return m_imagePath;
    }
  }
}