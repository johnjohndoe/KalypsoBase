package de.openali.odysseus.chart.framework.model.style.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.util.StyleUtils;

public class StyleSet implements IStyleSet
{
  private final Map<String, IStyle> m_styleMap = new LinkedHashMap<>();

  @Override
  public void addStyle( final String id, final IStyle style )
  {
    m_styleMap.put( id, style );
  }

  @Override
  public IStyle getStyle( final String identifier )
  {
    return m_styleMap.get( identifier );
  }

  public boolean isEmpty( )
  {
    return m_styleMap.isEmpty();
  }

  /**
   * if no style is found, a default style is created, saved for later use and returned
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends IStyle> T getStyle( final String id, final Class<T> styleClass )
  {
    final IStyle style = m_styleMap.get( id );
    if( style == null )
    {
      Logger.logWarning( Logger.TOPIC_LOG_STYLE, "No style for id '" + id + "'. Need " + styleClass + ". Returning default style and saving it to style set." ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      final T myStyle = StyleUtils.getDefaultStyle( styleClass );
      m_styleMap.put( id, myStyle );
      return myStyle;
    }

    // check if this is the correct style type

    final Class< ? extends IStyle> myStyleClass = style.getClass();

    if( styleClass.isAssignableFrom( myStyleClass ) )
      return (T) style;
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_STYLE, "Wrong style type for id '" + id + "'. Expected " + styleClass + ", got " + style.getClass() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          + ". Returning default style but NOT SAVING it to style set." ); //$NON-NLS-1$
      return StyleUtils.getDefaultStyle( styleClass );
    }
  }

  @Override
  public void dispose( )
  {
  }

  @Override
  public Map<String, IStyle> getStyles( )
  {
    return m_styleMap;
  }

}
