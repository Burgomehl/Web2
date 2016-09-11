package com.main.model;

import java.util.ArrayList;
import java.util.List;

import com.main.messages.Message;

public class History {
	private static History historyObject;
	private History(){
	}
	
	public static History getInstance(){
		if(historyObject == null){
			historyObject = new History();
		}
		return historyObject;
	}
	private List<Message> history = new ArrayList<>();
	
	public void addHistory(Message m){
		history.add(m);
	}
	
	public List<Message> getHistory(){
		return history;
	}
	
	public void deleteHistory(){
		history = new ArrayList<>();
	}
}
