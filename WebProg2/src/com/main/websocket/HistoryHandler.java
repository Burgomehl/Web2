package com.main.websocket;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.main.messages.DeleteMessage;
import com.main.messages.forms.Ellipse;
import com.main.messages.forms.FormMessage;
import com.main.messages.forms.Rectangle;
import com.main.model.History;

public class HistoryHandler {
	private static HistoryHandler historyHandler = new HistoryHandler();
	private History history;
	private ObjectMapper objMapper = new ObjectMapper();
	private Random r = new Random();
	private int maxSizeX = 800;
	private int maxSizeY = 800;

	private HistoryHandler() {
		history = new History();
	}

	public synchronized static HistoryHandler getInstance() {
		return historyHandler;
	}

	public void addHistory(FormMessage m) {
		history.addHistory(m);
		;
	}

	public List<FormMessage> getHistory() {
		return history.getHistory();
	}

	public void deleteHistory() {
		history.deleteHistory();
	}

	public void deleteHistoryItemsById(Long id) {
		history.deleteHistoryItemsById(id);
	}

	public long getCurrentId() {
		return history.getCurrentId();
	}

	private int generateNextPosition(int lastValue, int xyPosition) {
		int nextValue = lastValue + r.nextInt(21) - 10;
		System.out.println("for loop lastValue" + lastValue + "next value " + nextValue + " limit " + xyPosition);
		while ((nextValue + xyPosition) > maxSizeX || (nextValue + xyPosition) < 0) {
			if (xyPosition == 0) {
				nextValue = lastValue + r.nextInt(20) - 10;
			} else {
				nextValue = lastValue + r.nextInt(Math.abs(xyPosition) % 20) - (Math.abs(xyPosition) / 2) % 10;
			}
		}
		System.out.println("lastValue" + lastValue + "next value " + nextValue + " limit " + xyPosition);
		return nextValue;
	}

	public void animate(DeleteMessage objectsToAnimate) {
		System.out.println("size of list " + getHistory().size());
		for (String id : objectsToAnimate.getIds()) {
			List<FormMessage> collect = history.getHistory().parallelStream().filter(e -> e.getId() == Long.valueOf(id))
					.collect(Collectors.toList());
			for (FormMessage formMessage : collect) {
				switch (formMessage.getType()) {
				case RECTANGLE:
					Rectangle rec;
					try {
						rec = objMapper.readValue(formMessage.getContent(), Rectangle.class);
						rec.setA(generateNextPosition(rec.getA(), 0));
						rec.setB(generateNextPosition(rec.getB(), 0));
						JsonNode readTree = objMapper.readTree(objMapper.writeValueAsString(rec));
						formMessage.setContent(readTree);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case ELLIPSE:
					Ellipse ell;
					try {
						ell = objMapper.readValue(formMessage.getContent(), Ellipse.class);
						ell.setX(generateNextPosition(ell.getX(), 0));
						ell.setY(generateNextPosition(ell.getY(), 0));
						JsonNode readTree = objMapper.readTree(objMapper.writeValueAsString(ell));
						formMessage.setContent(readTree);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
		System.out.println("size of list " + getHistory().size());
	}
}
