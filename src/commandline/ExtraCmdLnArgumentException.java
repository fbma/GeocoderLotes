package commandline;

/**
 * Exception thrown when a command line option is missing an argument
 */
public class ExtraCmdLnArgumentException extends CmdLnArgumentException
{
	/**
	 * serial version id
	 */
	private static final long serialVersionUID = -8552921685243918697L;

	/**
	 * Construct a new exception.
	 */
	ExtraCmdLnArgumentException() {
		super("Option does not take an argument");
	}

}