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
package org.kalypso.contribs.eclipse.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;

/**
 * A {@link org.eclipse.core.resources.IResourceVisitor} which collects all resources which name matches a given regex.
 * 
 * @author belger
 */
public class RegexResourceVisitor implements IResourceVisitor
{
  private Pattern m_pattern;

  private final boolean m_recurseIntoNonMatched;

  private final boolean m_recurseIntoMatched;

  private final List<IResource> m_result = new ArrayList<IResource>();

  /**
   * @param regex
   *          The regular expression, the name of each visited resource must match.
   * @param recurseIntoNonMatched
   *          If false, recursion will stop on resources which did not match the pattern.
   * @param recurseIntoMatched
   *          If false, recursion will stop on resources which did match the pattern.
   */
  public RegexResourceVisitor( final String regex, final boolean recurseIntoMatched, final boolean recurseIntoNonMatched )
  {
    m_recurseIntoMatched = recurseIntoMatched;
    m_recurseIntoNonMatched = recurseIntoNonMatched;
    m_pattern = Pattern.compile( regex );
  }

  public Pattern getPattern( )
  {
    return m_pattern;
  }

  public IResource[] getResult( )
  {
    return m_result.toArray( new IResource[m_result.size()] );
  }

  /**
   * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
   */
  @Override
  public boolean visit( final IResource resource )
  {
    final Matcher m = m_pattern.matcher( resource.getName() );

    if( m.matches() )
    {
      m_result.add( resource );

      // we did match, continue?
      return m_recurseIntoMatched;
    }

    // we did not match, continue?
    return m_recurseIntoNonMatched;
  }

}
