package com.demo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {
	
	public static boolean isEmpty(String source){
		return source==null || source.length()==0 || "".equals(source.trim());
	}
	
	public static boolean isEmptyRowId(String source){
		return source==null || source.length()==0 || "-1".equals(source.trim());
	}
	

	/**
	 * deep clone a java object
	 * @param src
	 * @return
	 */
    public static Object deepClone(Object src)
    {
        Object o = null;
        try
        {
            if (src != null)
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(src);
                oos.close();
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bais);
                o = ois.readObject();
                ois.close();
                bais.close();
                baos.close();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return o;
    }
    
    public static String[] mergeSameValues(List<String[]> list){
    	
    	String[] values = new String[list.get(0).length];
    	
    	for(String[] row:list){
    		for(int i=0;i<row.length;i++){
    			if(values[i] == null || values.length == 0){
    				values[i] = isEmpty(row[i]) ? "" : row[i];
    			}else if(!values[i].equals(row[i])){
    				values[i] = "BDNA_NOT_ALL_THE_SAME";
    			}
    		}
    	}
    	
    	for(int i=0;i<values.length;i++){
    		if("BDNA_NOT_ALL_THE_SAME".equals(values[i])){
    			values[i] = "";
    		}
    	}
    	
    	return values;
    }
    
    public static String arrayToSring(String[] array, String separator){
    	String result = "";
    	for(String s:array){
    		if(isEmpty(s)) continue;
    		result += s +",";
    	}
    	return result.replaceAll(",$", "");
    }
    
//    public static List<Cell[]> mergetRowList(List<String[]> list){
//    	
//    	List<Cell[]> result = new ArrayList<Cell[]>(list.size());
//    	
//    	int cols = list.get(0).length;
//    	int mergetIndex = cols - 3;
//    	
//    	for(int row=0;row<list.size();row++){
//    		String[] ori = list.get(row);
//    		Cell[] cellRow = new Cell[cols];
//    		for(int col = 0; col < cols; col++){
//    			//last column should fire click event
//    			cellRow[col] = new Cell(ori[col], col == cols-1);
////    			if(mergetIndex >= col && row > 0){
////    				result.get(row-1)[col].merge(cellRow[col]);
////    			}
//    		}
//    		result.add(cellRow);
//    	}
//    	
//    	for(int row = result.size()-1;row>-1;row--){
//    		for(int col = 0; col < cols; col++){
//    			Cell[] cellRow = result.get(row);
//    			if(mergetIndex >= col && row > 0){
////    				result.get(row-1)[col].merge(cellRow[col]);
//    				cellRow[col].merge(result.get(row-1)[col]);
//    			}
//    		}
//    	}
//    	
//    	
//    	return result;
//    }
    
    public static String compactHeader(String source){
    	
    	if(source.indexOf(",")==-1) return source+":1";
    	
    	String[] labels = source.split(",");
    	String result = "";
    	String current = null;
    	int cnt = 1;
    	
    	for(int i=0;i<labels.length-1;i++){
    		if(i==0){
    			current = labels[i];
    			cnt = 1;
    		}
    		
    		if(current.equals(labels[i+1])){
    			cnt++;
    		}else{
    			result+=current+":"+cnt+",";
    			
    			current = labels[i+1];
    			cnt = 1;
    		}
    		
    	}
    	
    	result+=current+":"+cnt;
    	
    	return result;
    }

	public static final boolean matched(String regularExpression, String source){
		
//		System.out.println("regularExpression="+regularExpression);
		
		try{
			Pattern pattern = Pattern.compile(regularExpression);
			Matcher matcher = pattern.matcher(source);
			boolean found = matcher.find();
			pattern = null;
			matcher = null;
			return found;
		}catch(Exception e){
//			e.printStackTrace();
			return false;
		}
		
	}
	
    public static void main(String[] args){
    	System.out.println(isEmptyRowId("-1"));
    }
    
	
}
