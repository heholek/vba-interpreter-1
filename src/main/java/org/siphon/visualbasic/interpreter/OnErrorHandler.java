package org.siphon.visualbasic.interpreter;

public abstract class OnErrorHandler {
	public static class NoErrorHandler extends OnErrorHandler {
		private NoErrorHandler() {}
	}
	
	public static class ResumeNextErrorHandler extends OnErrorHandler {
		private ResumeNextErrorHandler() {}
	}
	
	public static class GotoErrorHandler extends OnErrorHandler {
		protected String label = null;
		protected int line = -1;
		
		public GotoErrorHandler(String label) {
			this.label = label;
		}
		
		public GotoErrorHandler(int line) {
			this.line = line;
		}
		
		public String getLabel() {
			return this.label;
		}
		
		public int getLine() {
			return this.line;
		}
	}
	
	public static final OnErrorHandler NO_ERROR_HANDLER = new NoErrorHandler();
	public static final OnErrorHandler RESUME_NEXT_ERROR_HANDLER = new ResumeNextErrorHandler();

}
