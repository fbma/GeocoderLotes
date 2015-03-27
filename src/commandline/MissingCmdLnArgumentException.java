package commandline;

public class MissingCmdLnArgumentException extends CmdLnArgumentException {
	/**
	 * serial version id
	 */
	private static final long serialVersionUID = -8552921685243918697L;

	/**
	 * Construct a new exception.
	 */
	MissingCmdLnArgumentException() {
		super("Additional argument required");
	}

}
