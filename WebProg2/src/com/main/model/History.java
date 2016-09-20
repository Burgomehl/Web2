package com.main.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.main.messages.forms.FormMessage;

public class History {
	private long currentId;

	public History(){
		
	}

	private List<FormMessage> history = new Vector<>();

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

	public long getCurrentId() {
		return currentId++;
	}

	
}
