package com.main.model;

import java.util.ArrayList;
import java.util.List;

import com.main.messages.forms.FormMessage;

public class History {
	private long currentId; 
	private static History historyObject;
	private History(){
		currentId = 0;
	}
	
	public static History getInstance(){
		if(historyObject == null){
			historyObject = new History();
		}
		return historyObject;
	}
	private List<FormMessage> history = new ArrayList<>();
	
	public void addHistory(FormMessage m){
		history.add(m);
	}
	
	public List<FormMessage> getHistory(){
		return history;
	}
	
	public void deleteHistory(){
		history = new ArrayList<>();
	}
	
	public void deleteHistoryItemsById(Long id){
		history.removeIf(e -> e.getId()==id);
	}

	public synchronized long getCurrentId() {
		return currentId++;
	}
}
