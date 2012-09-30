/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.wizard.shape;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.ui.i18n.Messages;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.sld.UserStyle;

/**
 * @author Gernot Belger
 */
public class ImportShapeFileData extends AbstractModelObject
{
  public enum StyleImport
  {
    useDefault(Messages.getString("ImportShapeFileData.0"), Messages.getString("ImportShapeFileData.1")), //$NON-NLS-1$ //$NON-NLS-2$
    generateDefault(Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.9" ), Messages.getString("ImportShapeFileData.2")), //$NON-NLS-1$ //$NON-NLS-2$
    selectExisting(Messages.getString("ImportShapeFileData.3"), Messages.getString("ImportShapeFileData.4")); //$NON-NLS-1$ //$NON-NLS-2$

    private final String m_label;

    private final String m_tooltip;

    private StyleImport( final String label, final String tooltip )
    {
      m_label = label;
      m_tooltip = tooltip;
    }

    public String getTooltip( )
    {
      return m_tooltip;
    }

    @Override
    public String toString( )
    {
      return m_label;
    }
  }

  static final String EXTENSIONS_SLD = "sld"; //$NON-NLS-1$

  public static final String[] EMPTY_STYLES = new String[] { Messages.getString( "ImportShapeFileData.5" ) }; //$NON-NLS-1$

  static final String[] FEATURETYPE_STYLES = new String[] { Messages.getString("ImportShapeFileData.6") }; //$NON-NLS-1$

  public static final String PROPERTY_SRS = "srs"; //$NON-NLS-1$

  public static final String PROPERTY_SHAPE_FILE = "shapeFile"; //$NON-NLS-1$

  private static final String PROPERTY_STYLE_FILE = "styleFile"; //$NON-NLS-1$

  public static final String PROPERTY_STYLE_IMPORT_TYPE = "styleImportType"; //$NON-NLS-1$

  public static final String PROPERTY_STYLES = "styles"; //$NON-NLS-1$

  public static final String PROPERTY_STYLE = "style"; //$NON-NLS-1$

  public static final String PROPERTY_STYLE_CONTROLS_ENABLED = "styleControlsEnabled"; //$NON-NLS-1$

  public static final String PROPERTY_STYLE_NAME_CONTROL_ENABLED = "styleNameControlEnabled"; //$NON-NLS-1$

  private final PropertyChangeListener m_shapeFileListener = new PropertyChangeListener()
  {
    @Override
    public void propertyChange( final PropertyChangeEvent evt )
    {
      handleShapeFileChanged();
    }
  };

  private final PropertyChangeListener m_styleFileListener = new PropertyChangeListener()
  {
    @Override
    public void propertyChange( final PropertyChangeEvent evt )
    {
      handleStyleFileChanged();
    }
  };

  private final Job m_loadStyleJob = new LoadStyleJob( this );

  private final FileAndHistoryData m_shapeFile = new FileAndHistoryData( PROPERTY_SHAPE_FILE );

  private String m_srs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

  private final FileAndHistoryData m_styleFile = new FileAndHistoryData( PROPERTY_STYLE_FILE );

  private Object[] m_styles = EMPTY_STYLES;

  private Object m_style = EMPTY_STYLES[0];

  private StyleImport m_styleImportType = StyleImport.generateDefault;

  private int m_insertionIndex;

  public ImportShapeFileData( )
  {
    m_shapeFile.addPropertyChangeListener( FileAndHistoryData.PROPERTY_PATH, m_shapeFileListener );
    m_styleFile.addPropertyChangeListener( FileAndHistoryData.PROPERTY_PATH, m_styleFileListener );
  }

  public void init( final IDialogSettings settings )
  {
    m_shapeFile.init( settings );
    m_styleFile.init( settings );
  }

  public void storeSettings( final IDialogSettings settings )
  {
    m_shapeFile.storeSettings( settings );
    m_styleFile.storeSettings( settings );
  }

  public FileAndHistoryData getShapeFile( )
  {
    return m_shapeFile;
  }

  public String getSrs( )
  {
    return m_srs;
  }

  public void setSrs( final String srs )
  {
    final Object oldValue = m_srs;

    m_srs = srs;

    firePropertyChange( PROPERTY_SRS, oldValue, srs );
  }

  public FileAndHistoryData getStyleFile( )
  {
    return m_styleFile;
  }

  public void setStyleImportType( final StyleImport styleImport )
  {
    if( styleImport == null )
      return;

    final StyleImport oldValue = m_styleImportType;
    final boolean oldEnablement = getStyleControlsEnabled();
    final boolean oldNameEnablement = getStyleNameControlEnabled();

    m_styleImportType = styleImport;

    firePropertyChange( PROPERTY_STYLE_IMPORT_TYPE, oldValue, styleImport );
    firePropertyChange( PROPERTY_STYLE_CONTROLS_ENABLED, oldEnablement, getStyleControlsEnabled() );
    firePropertyChange( PROPERTY_STYLE_NAME_CONTROL_ENABLED, oldNameEnablement, getStyleNameControlEnabled() );

    final Object oldStylePath = m_styleFile.getPath();
    m_styleFile.firePropertyChange( FileAndHistoryData.PROPERTY_PATH, oldStylePath, m_styleFile.getPath() );
  }

  public StyleImport getStyleImportType( )
  {
    return m_styleImportType;
  }

  public Object[] getStyles( )
  {
    return m_styles;
  }

  void setStyles( final Object[] styles )
  {
    final Object[] oldValue = m_styles;

    m_styles = styles;

    firePropertyChange( PROPERTY_STYLES, oldValue, styles );

    if( styles.length == 0 )
      setStyle( null );
    else
      setStyle( styles[0] );
  }

  public Object getStyle( )
  {
    return m_style;
  }

  public void setStyle( final Object style )
  {
    final Object oldValue = m_style;

    m_style = style;

    firePropertyChange( PROPERTY_STYLE, oldValue, style );
  }

  public boolean getStyleControlsEnabled( )
  {
    return m_styleImportType == StyleImport.selectExisting;
  }

  public boolean getStyleNameControlEnabled( )
  {
    final boolean hasStyle = m_styles != EMPTY_STYLES && m_styles != FEATURETYPE_STYLES && m_styles != null;
    return getStyleControlsEnabled() && hasStyle;
  }

  protected void handleShapeFileChanged( )
  {
    final IPath shapePath = m_shapeFile.getPath();
    if( shapePath == null )
      return;

    /* Do nothing, if style was already chosen */
    final IPath stylePath = m_styleFile.getPath();
    if( stylePath != null && !stylePath.isEmpty() )
      return;

    final IPath sldPath = shapePath.removeFileExtension().addFileExtension( EXTENSIONS_SLD );
    final IFile sldFile = ResourcesPlugin.getWorkspace().getRoot().getFile( sldPath );
    if( !sldFile.exists() )
      return;

    setStyleImportType( StyleImport.selectExisting );
    m_styleFile.setPath( sldPath );
  }

  protected void handleStyleFileChanged( )
  {
    m_loadStyleJob.cancel();
    m_loadStyleJob.schedule( 500 );
  }

  public String getStyleName( )
  {
    if( m_style instanceof UserStyle )
      return ((UserStyle) m_style).getName();

    return null;
  }

  public void setInsertionIndex( final int insertionIndex )
  {
    m_insertionIndex = insertionIndex;
  }

  public int getInsertionIndex( )
  {
    return m_insertionIndex;
  }
}