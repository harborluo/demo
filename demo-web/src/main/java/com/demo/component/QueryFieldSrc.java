package com.demo.component;


/**
 * fixed for Bug 25505 - QA View: Mapped Hardware Lifecycle
 * virtual column component
 * @author harbor
 *
 */
public class QueryFieldSrc extends AbstractComponent {



	@Override
	public String getValidateAttributes() {
		return "^(src|datatype|filter-widget|header-label|valuelabels|values|ref-select)$";
	}

	@Override
	public void validate() {
		

	}

	@Override
	public boolean isCache() {
		return false;
	}	
}
