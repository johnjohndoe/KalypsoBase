/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.zml.ui.imports;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.TimeZone;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.core.KalypsoCoreExtensions;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;

/**
 * @author Gernot Belger
 */
public class ImportObservationData extends AbstractModelObject implements IStoreObservationData
{
  static final String PROPERTY_TIMEZONE = "timezone"; //$NON-NLS-1$

  static final String PROPERTY_ADAPTER = "adapter"; //$NON-NLS-1$

  public static final String PROPERTY_PARAMETER_TYPE = "parameterType"; //$NON-NLS-1$

  private static final String PROPERTY_TARGET_FILE = "targetFile"; //$NON-NLS-1$

  private final PropertyChangeListener m_sourceFileListener = new PropertyChangeListener()
  {
    @Override
    public void propertyChange( final PropertyChangeEvent evt )
    {
      handleSourceFileChanged( (File) evt.getNewValue() );
    }
  };

  private String m_timezone = KalypsoCorePlugin.getDefault().getTimeZone().getID();

  private final INativeObservationAdapter[] m_adapters;

  private INativeObservationAdapter m_adapter = null;

  private final FileAndHistoryData m_sourceFileData = new FileAndHistoryData( "sourceFile" ); //$NON-NLS-1$

  private final String[] m_allowedParameterTypes;

  private String m_parameterType;

  private IFile m_targetFile;

  private String[] m_existingTimeserieses;

  public ImportObservationData( final String... allowedParameterTypes )
  {
    m_adapters = KalypsoCoreExtensions.getObservationImporters();
    m_allowedParameterTypes = allowedParameterTypes;

    if( m_adapters.length > 0 )
      m_adapter = m_adapters[0];

    if( m_allowedParameterTypes.length > 0 )
      m_parameterType = m_allowedParameterTypes[0];

    m_sourceFileData.addPropertyChangeListener( FileAndHistoryData.PROPERTY_FILE, m_sourceFileListener );
  }

  public void init( final IDialogSettings settings )
  {
    try
    {
      if( settings == null )
        return;

      m_sourceFileData.init( settings );

      setParameterType( DialogSettingsUtils.getString( settings, PROPERTY_PARAMETER_TYPE, m_parameterType ) );

      /* Adapter */
      final String currentAdapterId = m_adapter == null ? null : m_adapter.getId();
      final String adapterId = DialogSettingsUtils.getString( settings, PROPERTY_ADAPTER, currentAdapterId );
      setAdapter( KalypsoCoreExtensions.getObservationImporter( adapterId ) );

      setTimezone( DialogSettingsUtils.getString( settings, PROPERTY_TIMEZONE, m_timezone ) );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }
  }

  public void storeSettings( final IDialogSettings settings )
  {
    if( settings == null )
      return;

    m_sourceFileData.storeSettings( settings );

    settings.put( PROPERTY_PARAMETER_TYPE, m_parameterType );

    if( m_adapter != null )
      settings.put( PROPERTY_ADAPTER, m_adapter.getId() );

    settings.put( PROPERTY_TIMEZONE, m_timezone );
  }

  protected void handleSourceFileChanged( final File newSourceFile )
  {
    /* Guess best importer by file extension */
    if( newSourceFile == null )
      return;

    final String extension = FilenameUtils.getExtension( newSourceFile.getName() );
    final INativeObservationAdapter guessedAdapter = findAdapterByExtension( extension );

    if( guessedAdapter != null )
      setAdapter( guessedAdapter );

    /* load data and show preview */
    // this is probably too slow for big timeseries, so we cannot show a preview?
    // IObservation sourceObservation = ZmlFactory.parseXML( newSourceFile.toURI().toURL() );
  }

  private INativeObservationAdapter findAdapterByExtension( final String extension )
  {
    if( StringUtils.isBlank( extension ) )
      return null;

    final INativeObservationAdapter[] allAdapters = getObservationAdapters();
    for( final INativeObservationAdapter adapter : allAdapters )
    {
      final String defaultExtension = adapter.getDefaultExtension();
      if( !StringUtils.isBlank( defaultExtension ) && extension.compareToIgnoreCase( defaultExtension ) == 0 )
        return adapter;
    }

    return null;
  }

  public FileAndHistoryData getSourceFileData( )
  {
    return m_sourceFileData;
  }

  public IFile getTargetFile( )
  {
    return m_targetFile;
  }

  public void setTargetFile( final IFile targetFile )
  {
    final IFile oldValue = targetFile;
    m_targetFile = targetFile;

    firePropertyChange( PROPERTY_TARGET_FILE, oldValue, targetFile );
  }

  public TimeZone getTimezoneParsed( )
  {
    return TimeZone.getTimeZone( m_timezone );
  }

  public String getTimezone( )
  {
    return m_timezone;
  }

  public void setTimezone( final String timezone )
  {
    final String oldValue = m_timezone;

    m_timezone = timezone;

    firePropertyChange( PROPERTY_TIMEZONE, oldValue, timezone );
  }

  public INativeObservationAdapter[] getObservationAdapters( )
  {
    return m_adapters;
  }

  public INativeObservationAdapter getAdapter( )
  {
    return m_adapter;
  }

  public void setAdapter( final INativeObservationAdapter adapter )
  {
    final INativeObservationAdapter oldValue = m_adapter;

    m_adapter = adapter;

    firePropertyChange( PROPERTY_ADAPTER, oldValue, adapter );
  }

  public String[] getAllowedParameterTypes( )
  {
    return m_allowedParameterTypes;
  }

  @Override
  public String getParameterType( )
  {
    return m_parameterType;
  }

  public void setParameterType( final String parameterType )
  {
    final String oldValue = m_parameterType;
    m_parameterType = parameterType;

    firePropertyChange( PROPERTY_PARAMETER_TYPE, oldValue, parameterType );
  }

  public void setExistingTimeserieses( final String[] timeserieses )
  {
    m_existingTimeserieses = timeserieses;
  }

  @Override
  public String[] getExistingTimeserieses( )
  {
    return m_existingTimeserieses;
  }
}