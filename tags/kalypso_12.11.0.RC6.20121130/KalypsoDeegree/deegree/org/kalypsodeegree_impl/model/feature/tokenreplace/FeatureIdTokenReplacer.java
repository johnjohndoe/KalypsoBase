package org.kalypsodeegree_impl.model.feature.tokenreplace;

import org.kalypso.commons.tokenreplace.ITokenReplacer;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Tokens of kind ${id}, replaced by the feature's id.
 * 
 * @author Gernot Belger
 */
public final class FeatureIdTokenReplacer implements ITokenReplacer
{
  @Override
  public String replaceToken( final Object value, final String argument )
  {
    return ((Feature) value).getId();
  }

  @Override
  public String getToken( )
  {
    return "id";
  }
}