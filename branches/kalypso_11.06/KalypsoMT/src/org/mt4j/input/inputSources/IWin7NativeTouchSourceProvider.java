package org.mt4j.input.inputSources;


public interface IWin7NativeTouchSourceProvider {
	public void cleanup();

	public boolean setSunAwtCanvasHandle(int HWND);

	public boolean pollMTEvent(Native_WM_TOUCH_Event myEvent);

	public static class Native_WM_TOUCH_Event {
		// can be real enums in Java 5.0.
		/** The Constant TOUCH_DOWN. */
		public static final int TOUCH_DOWN = 0;

		/** The Constant TOUCH_MOVE. */
		public static final int TOUCH_MOVE = 1;

		/** The Constant TOUCH_UP. */
		public static final int TOUCH_UP = 2;

		/** The type. */
		public int type;

		/** The id. */
		public int id;

		/** The x value. */
		public int x;

		/** The y value. */
		public int y;

		/** The contact size area X dimension */
		public int contactSizeX;

		/** The contact size area Y dimension */
		public int contactSizeY;
	}

}
