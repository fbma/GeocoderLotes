package commandline;

/**
 * Exception thrown when a command line option that was unexpected is found.
 */
public class UnknownCmdLnOptionException extends CmdLnException {

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 6461128374875358108L;

	/**
	 * Constructs an <code>UnknownCmdLnOptionException</code>
	 */
	public UnknownCmdLnOptionException() {
		super("Unknown command line option");
	}

	private String argument;

	/**
	 * Get the argument that caused this exception
	 * @return the argument
	 */
	public String getArgument() {
		return argument;
	}

	/**
	 * Set the argument
	 * @param argument the argument to set
	 * @return this for method chaining
	 */
	UnknownCmdLnOptionException setArgument(String argument) {
		this.argument = argument;
		return this;
	}

	private String option;

	/**
	 * Get the option that caused this exception
	 * @return the option
	 */
	public String getOption() {
		return option;
	}

	/**
	 * Set the option
	 * @param option the option to set
	 * @return this for method chaining
	 */
	UnknownCmdLnOptionException setOption(String option) {
		this.option = option;
		return this;
	}

	@Override public String getMessage(){
		return super.getMessage() + ": " + getArgument() + ": " + getOption();
	}
}