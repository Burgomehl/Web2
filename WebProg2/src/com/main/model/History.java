package com.main.model;

import java.util.ArrayList;
import java.util.List;

import org.omg.Messaging.SyncScopeHelper;

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
	
	public void deleteHistoryItemsById(String id){
		System.out.println("Size: "+history.size());
		history.removeIf(e -> {System.out.println(e.getContent()); return e.getContent().toString().contains(id);});
		System.out.println("Size nach löschen: "+history.size());
	}
}
