package commandline;

public abstract class CmdLnListener
{
	/**
	 * Called when a command line option is found.
	 *
	 * @param result The command line option and its arguments
	 *
	 * @since ostermillerutils 1.07.00
	 */
	public abstract void found(CmdLnResult result);
}