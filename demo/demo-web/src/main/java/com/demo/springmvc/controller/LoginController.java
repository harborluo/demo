package com.demo.springmvc.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.demo.component.ComponentRepository;
import com.demo.component.Table;

@Controller
public class LoginController extends AbstractController  {
	
	@RequestMapping(value="/login", method = RequestMethod.GET)
	public String login() {
		return "login";
	}
	
	
	@RequestMapping(value="/test", method = RequestMethod.GET)
	public ResponseEntity<Table> test() throws ClassNotFoundException {
//		String message = "Nice to meet you!";
//		Table tab = new Table();
//		tab.setLabel(message);
//		tab.setName("test_table");
//		tab.setPath("com.test.hello");
//		tab.setProperty("table-name", "test_table_name");
//		tab.setProperty("schema", "demo_schema");
		Table tab = (Table) ComponentRepository.getInstance().getComponentByPath("core.users");
		Class rt_class = Class.forName("java.util.ArrayList");
		rt_class.getAnnotation(tab.getClass());
//		getLogger().debug(tab.generateDDL());
//		return new ResponseEntity<ResponseDemo>(new ResponseDemo(), HttpStatus.OK);
		return new ResponseEntity<Table>(tab, HttpStatus.OK);
	}
}

class ResponseDemo{
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	private String message = "hello";
	private List<String> data = new ArrayList<>();
	public ResponseDemo(){
		for(int i=0;i<20;i++){
			data.add("test\"abc\"中华人民共和国<asdfset><asdf>/"+i);
		}
	}
	
	public String getMessage() {
		return message;
	}
	
//	public void setMessage(String message) {
//		this.message = message;
//	}
	public List<String> getData() {
		return data;
	}
//	public void setData(List<String> data) {
//		this.data = data;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResponseDemo other = (ResponseDemo) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
	
	
}
