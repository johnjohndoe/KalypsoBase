/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.commons;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.commons.i18n.Messages;
import org.kalypso.commons.process.IProcess;
import org.kalypso.commons.process.IProcessFactory;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

/**
 * Extensions of <code>org.kalypso.commons</code>.
 * 
 * @author Gernot Belger
 */
public class KalypsoCommonsExtensions
{
  private final static String I10N_TRANSLATOR_EXTENSION_POINT = "org.kalypso.commons.i18n"; //$NON-NLS-1$

  private final static String PROCESS_EXTENSION_POINT = "org.kalypso.commons.process"; //$NON-NLS-1$

  private static Map<String, IConfigurationElement> m_i10nExtensions;

  private static Map<String, IConfigurationElement> m_processExtensions;

  public static synchronized ITranslator createTranslator( final String id )
  {
    if( id == null )
      return null;

    if( m_i10nExtensions == null )
    {
      m_i10nExtensions = new HashMap<String, IConfigurationElement>();

      final IExtensionRegistry registry = Platform.getExtensionRegistry();

      final IExtensionPoint extensionPoint = registry.getExtensionPoint( I10N_TRANSLATOR_EXTENSION_POINT );

      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

      for( final IConfigurationElement element : configurationElements )
      {
        final String elementId = element.getAttribute( "id" ); //$NON-NLS-1$
        m_i10nExtensions.put( elementId, element );
      }
    }

    final KalypsoCommonsPlugin activator = KalypsoCommonsPlugin.getDefault();
    final IConfigurationElement element = m_i10nExtensions.get( id );
    if( element == null )
    {
      final Status status = new Status( IStatus.ERROR, activator.getBundle().getSymbolicName(), 1, Messages.getString("org.kalypso.commons.KalypsoCommonsExtensions.0", id), null ); //$NON-NLS-1$
      activator.getLog().log( status );
      return null;
    }

    try
    {
      return (ITranslator) element.createExecutableExtension( "class" ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      activator.getLog().log( e.getStatus() );
      return null;
    }
  }

  /**
   * @param factoryId
   *          The extension-id of the {@link IProcessFactory} that should be used in order to create the new process.
   * @see IProcessFactory#newProcess(String, String, String...)
   */
  public static synchronized IProcess createProcess( final String factoryId, final String tempDirName, final String executeable, final String... commandlineArgs ) throws CoreException
  {
    Assert.isNotNull( factoryId );

    final IProcessFactory factory = getProcessFactory( factoryId );
    if( factory == null )
    {
      final IStatus status = StatusUtilities.createErrorStatus( Messages.getString("org.kalypso.commons.KalypsoCommonsExtensions.1", factoryId )); //$NON-NLS-1$
      throw new CoreException( status );
    }

    try
    {
      return factory.newProcess( tempDirName, executeable, commandlineArgs );
    }
    catch( final IOException e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e, Messages.getString("org.kalypso.commons.KalypsoCommonsExtensions.2", executeable, tempDirName )); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  private static IProcessFactory getProcessFactory( final String id ) throws CoreException
  {
    synchronized( PROCESS_EXTENSION_POINT )
    {
      if( m_processExtensions == null )
      {
        m_processExtensions = new HashMap<String, IConfigurationElement>();

        final IExtensionRegistry registry = Platform.getExtensionRegistry();

        final IExtensionPoint extensionPoint = registry.getExtensionPoint( PROCESS_EXTENSION_POINT );

        final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

        for( final IConfigurationElement element : configurationElements )
        {
          final String elementId = element.getAttribute( "id" ); //$NON-NLS-1$
          m_processExtensions.put( elementId, element );
        }
      }
    }

    final IConfigurationElement element = m_processExtensions.get( id );
    if( element == null )
      return null;

    return (IProcessFactory) element.createExecutableExtension( "class" ); //$NON-NLS-1$
  }
}
