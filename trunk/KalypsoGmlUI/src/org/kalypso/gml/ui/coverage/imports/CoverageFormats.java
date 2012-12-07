/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.gml.ui.coverage.imports;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserDelegateOpen;
import org.kalypso.contribs.java.io.FileExtensions;
import org.kalypso.contribs.java.io.FilePattern;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gml.ui.internal.coverage.imports.ASCCoverageImporter;
import org.kalypso.gml.ui.internal.coverage.imports.BINCoverageImporter;
import org.kalypso.gml.ui.internal.coverage.imports.GmlCoverageImporter;
import org.kalypso.gml.ui.internal.coverage.imports.HmoCoverageImporter;
import org.kalypso.gml.ui.internal.coverage.imports.ShapeCoverageImporter;
import org.kalypso.gml.ui.internal.coverage.imports.ZweiDMCoverageImporter;

/**
 * Helper that knows about all supported coverage formats.
 * 
 * @author Gernot Belger
 */
public final class CoverageFormats
{
  public static FileChooserDelegateOpen createFileOpenDelegate( )
  {
    final FileChooserDelegateOpen delegate = new FileChooserDelegateOpen();

    final Set<String> knownPatterns = new LinkedHashSet<>();

    final ICoverageImporter[] importers = getImporter();
    for( final ICoverageImporter importer : importers )
    {
      final FilePattern pattern = importer.getFilePattern();
      knownPatterns.add( pattern.getPattern() );
      delegate.addFilter( pattern );
    }

    /* known patterns filter */
    final StringBuilder knownPatternsLabel = new StringBuilder();
    knownPatternsLabel.append( Messages.getString( "CoverageFormats_0" ) ); //$NON-NLS-1$
    // knownPatternsLabel.append( "All supported formats (" );
    // knownPatternsLabel.append( StringUtils.join( knownPatterns, ", " ) );
    // knownPatternsLabel.append( ')' );

    final String knownPatternsFilter = StringUtils.join( knownPatterns, ";" ); //$NON-NLS-1$

    delegate.addFilter( knownPatternsLabel.toString(), knownPatternsFilter );

    /* All files filter */
    delegate.addFilter( FileExtensions.ALL_FILES );

    return delegate;
  }

  private static ICoverageImporter[] getImporter( )
  {
    // FIXME: add formats depending on target coverage collection: i.e. if collection only supports grid formats, only
    // show grid formats

    final Collection<ICoverageImporter> importers = new ArrayList<>();

    importers.add( new ASCCoverageImporter() );
    importers.add( new BINCoverageImporter() );
    // TODO: importers.add( new TifCoverageImporter( "dat" ) );

    importers.add( new HmoCoverageImporter() );
    importers.add( new GmlCoverageImporter() );
    importers.add( new ShapeCoverageImporter() );
    importers.add( new ZweiDMCoverageImporter() );

    return importers.toArray( new ICoverageImporter[importers.size()] );
  }

  public static ICoverageImporter findImporter( final File dataFile )
  {
    final ICoverageImporter[] importers = getImporter();
    for( final ICoverageImporter importer : importers )
    {
      final FilePattern pattern = importer.getFilePattern();
      if( pattern.matches( dataFile ) )
        return importer;
    }

    return null;
  }
}