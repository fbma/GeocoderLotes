package commandline;

/**
 * Exception thrown when a command line cannot be parsed.
 *
 * More information about this class and code samples for suggested use are
 * available from <a target="_top" href=
 * "http://ostermiller.org/utils/CmdLn.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.07.00
 */
public class CmdLnException extends IllegalArgumentException
{
	/**
	 * serial version id
	 *
	 * @since ostermillerutils 1.07.00
	 */
	private static final long serialVersionUID = 3984942697362044497L;

	/**
	 * @param message detail message
	 *
	 * @since ostermillerutils 1.07.00
	 */
	CmdLnException(String message) {
		super(message);
	}
}
