package commandline;

/**
 * Exception thrown for a problem with a specific command line option.
 *
 * More information about this class and code samples for suggested use are
 * available from <a target="_top" href=
 * "http://ostermiller.org/utils/CmdLn.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.07.00
 */
public class CmdLnArgumentException extends CmdLnException {

	/**
	 * serial version id
	 *
	 * @since ostermillerutils 1.07.00
	 */
	private static final long serialVersionUID = -5457270771303129044L;

	/**
	 * @param message message explaining the exception
	 *
	 * @since ostermillerutils 1.07.00
	 */
	CmdLnArgumentException(String message) {
		super(message);
	}

	private CmdLnResult result;

	/**
	 * Get the partial result with missing arguments.
	 *
	 * @return the partial result
	 *
	 * @since ostermillerutils 1.07.00
	 */
	public CmdLnResult getResult() {
		return result;
	}

	/**
	 * Get the option that caused this exception
	 *
	 * @return the option
	 *
	 * @since ostermillerutils 1.07.00
	 */
	public CmdLnOption getOption() {
		return result.getOption();
	}

	/**
	 * Set the result
	 *
	 * @param result partial result
	 * @return this for method chaining
	 *
	 * @since ostermillerutils 1.07.00
	 */
	CmdLnArgumentException setResult(CmdLnResult result) {
		this.result = result;
		return this;
	}

	/**
	 * @return message with the option name
	 *
	 * @since ostermillerutils 1.07.00
	 */
	@Override public String getMessage(){
		return super.getMessage() + ": " + getOption();
	}
}