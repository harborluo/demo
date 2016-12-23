package com.websystique.springmvc.service.vo;

public class Cell {
	
    private String value;
    
    private int rowSpan =1;
    
    private boolean fireEvent = false;
	
	public Cell(String value, boolean fireEvent) {
		this.value = (value==null ? "" : value);
		this.fireEvent = fireEvent;
	}
	
	
	
	public String getValue() {
		return value;
	}



	public void addRowSpan(int rowSpan) {
		this.rowSpan += rowSpan;
//		System.out.println("row span:"+this.rowSpan);
	}
	
	public void merge(Cell target){
//		System.out.println("val:"+this.value+" target:"+target.getValue());
		
		if(this.rowSpan >= 8190 ) return;
		
		if(this.value.equals(target.getValue())){
			target.addRowSpan(this.rowSpan);
			this.rowSpan = 0;
		}
	}
	
	public String getHtml(){
//		if(this.rowSpan==0) return "";
		if(this.rowSpan==0){
			return "<td class='reportCell' style='display:none'>" + this.value + "</td>";
		}
		
		if(this.rowSpan>1){
			return "<td class='reportCell' rowspan='" + this.rowSpan + "' title='" + this.value + "'>" + this.value + "</td>";
		}else{
			return "<td class='reportCell"+(this.fireEvent?" chartCell":"")+"' " + (this.fireEvent ? "onclick='v(this)'" :"") + ">" + this.value + "</td>"; 
		}
		
	}
    
}
