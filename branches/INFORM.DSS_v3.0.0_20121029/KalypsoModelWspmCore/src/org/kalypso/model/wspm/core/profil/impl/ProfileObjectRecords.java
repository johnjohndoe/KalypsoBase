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
package org.kalypso.model.wspm.core.profil.impl;

import java.util.ArrayList;
import java.util.List;

import org.kalypso.model.wspm.core.profil.IProfileObjectRecord;
import org.kalypso.model.wspm.core.profil.IProfileObjectRecords;

/**
 * @author Holger Albert
 */
class ProfileObjectRecords implements IProfileObjectRecords
{
  private final AbstractProfileObject m_parent;

  private final List<IProfileObjectRecord> m_records;

  public ProfileObjectRecords( final AbstractProfileObject parent )
  {
    m_parent = parent;
    m_records = new ArrayList<>();
  }

  @Override
  public int size( )
  {
    return m_records.size();
  }

  @Override
  public IProfileObjectRecord addNewRecord( )
  {
    final ProfileObjectRecord profileObjectRecord = new ProfileObjectRecord( this );
    addRecord( profileObjectRecord );
    return profileObjectRecord;
  }

  @Override
  public void addRecord( final IProfileObjectRecord record )
  {
    m_records.add( record );
    fireProfileObjectRecordsChanged();
  }

  @Override
  public IProfileObjectRecord getRecord( final int index )
  {
    return m_records.get( index );
  }

  @Override
  public IProfileObjectRecord removeRecord( final int index )
  {
    final IProfileObjectRecord removed = m_records.remove( index );
    fireProfileObjectRecordsChanged();
    return removed;
  }

  @Override
  public void clearRecords( )
  {
    m_records.clear();
    fireProfileObjectRecordsChanged();
  }

  protected void fireProfileObjectRecordChanged( final IProfileObjectRecord changedRecord )
  {
    if( m_parent == null )
      return;

    m_parent.fireProfileObjectRecordChanged( changedRecord );
  }

  private void fireProfileObjectRecordsChanged( )
  {
    if( m_parent == null )
      return;

    m_parent.fireProfileObjectRecordsChanged();
  }

  @Override
  public IProfileObjectRecord[] getAll( )
  {
    return m_records.toArray( new IProfileObjectRecord[m_records.size()] );
  }
}