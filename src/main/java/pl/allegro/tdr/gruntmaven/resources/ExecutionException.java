package pl.allegro.tdr.gruntmaven.resources;

public class ExecutionException extends Exception {
	private String result;
	private int exitValue;

	public ExecutionException( String error, String result, int exitValue ) {
		super(error);
		this.result = result;
		this.exitValue = exitValue;
	}

	public String getResult() {
		return result;
	}

	public int getExitValue() {
		return exitValue;
	}
}
