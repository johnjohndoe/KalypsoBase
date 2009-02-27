package de.openali.diagram.framework.model.styles.impl;

import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.framework.logging.Logger;
import de.openali.diagram.framework.model.styles.IStyleConstants;
import de.openali.diagram.framework.model.styles.IStyledElement;
import de.openali.diagram.framework.model.styles.IStyleConstants.SE_TYPE;

/**
 * @author alibu
 *
 * Dummy-Implementierung für IStyledElement; wird vom StyleContainer zurückgegeben, falls ein
 * nicht vorhandene StyledElement angefordert wird
 */
public class StyleDummy implements IStyledElement {

	/**
	 * gibt DUMMY als Type zurück
	 */
	public SE_TYPE getType() {
		// TODO Auto-generated method stub
		return SE_TYPE.DUMMY;
	}

	/**
	 * schreibt eine Nachricht ins Log, in der auf die nicht korrekte Verwendung hingewiesen wird
	 */
	public void paint(GCWrapper gc) {
		Logger.logWarning(Logger.TOPIC_LOG_STYLE, "Trying to paint layer without correct style setting");
	}

	/**
	 * nicht implementiert
	 */
	public void setPath(List<Point> path) {

	}

}
