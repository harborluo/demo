package com.demo.component;


public class Folder extends AbstractComponent  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3186632451963694752L;

	@Override
	public void validate() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public String getValidateAttributes() {
		return "^$";
	}

	@Override
	public boolean isCache() {
		return false;
	}
	
}
