package org.kalypso.commons.java.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author Gernot Belger
 */
public abstract class AbstractModelObject
{
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport( this );

  public void addPropertyChangeListener( final PropertyChangeListener listener )
  {
    propertyChangeSupport.addPropertyChangeListener( listener );
  }

  public void addPropertyChangeListener( final String propertyName, final PropertyChangeListener listener )
  {
    propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
  }

  public void removePropertyChangeListener( final PropertyChangeListener listener )
  {
    propertyChangeSupport.removePropertyChangeListener( listener );
  }

  public void removePropertyChangeListener( final String propertyName, final PropertyChangeListener listener )
  {
    propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
  }

  protected void firePropertyChange( final String propertyName, final Object oldValue, final Object newValue )
  {
    propertyChangeSupport.firePropertyChange( propertyName, oldValue, newValue );
  }
}