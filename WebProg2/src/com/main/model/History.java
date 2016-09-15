package com.main.model;

import java.util.ArrayList;
import java.util.List;

import com.main.messages.forms.FormMessage;

public class History {
	private long currentId;

	public History(){
		
	}

	private List<FormMessage> history = new ArrayList<>();

	public void addHistory(FormMessage m) {
		history.add(m);
	}

	public List<FormMessage> getHistory() {
		return history;
	}

	public void deleteHistory() {
		history = new ArrayList<>();
	}

	public void deleteHistoryItemsById(Long id) {
		history.removeIf(e -> e.getId() == id);
	}

	public synchronized long getCurrentId() {
		return currentId++;
	}

	
}
