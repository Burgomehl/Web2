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
import com.main.messages.forms.Snake;
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
		while ((nextValue + xyPosition) > maxSizeX || (nextValue + xyPosition) < 0) {
			if (xyPosition == 0) {
				nextValue = lastValue + r.nextInt(20) - 10;
			} else {
				nextValue = lastValue + r.nextInt(Math.abs(xyPosition) % 20) - (Math.abs(xyPosition) / 2) % 10;
			}
		}
		return nextValue;
	}

	public void animate(DeleteMessage objectsToAnimate) {
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
				case SNAKE:
					Snake sna;
					try {
						sna = objMapper.readValue(formMessage.getContent(), Snake.class);
						if (sna.getaElements().length > 0) {
							int oldA = sna.getaElements()[0];
							int oldB = sna.getbElements()[0];
							while ((sna.getaElements()[0] < 0 || sna.getbElements()[0] < 0)
									|| (sna.getaElements()[0] == oldA && sna.getbElements()[0] == oldB)) {
								int direction = r.nextInt(4);
								sna.getaElements()[0] = oldA;
								sna.getbElements()[0] = oldB;
								switch (direction) {
								case 0:
									sna.getaElements()[0] += 10;
									break;
								case 1:
									sna.getbElements()[0] += 10;
									break;
								case 2:
									sna.getaElements()[0] -= 10;
									break;
								case 3:
									sna.getbElements()[0] -= 10;
									break;
								}
								sna.getaElements()[0] += 800;
								sna.getbElements()[0] += 800;
								sna.getaElements()[0] %= 800;
								sna.getbElements()[0] %= 800;
							}
							for (int i = 1; i < sna.getaElements().length; ++i) {
								int tempA = sna.getaElements()[i];
								int tempB = sna.getbElements()[i];
								sna.getaElements()[i] = oldA;
								sna.getbElements()[i] = oldB;
								oldA = tempA;
								oldB = tempB;
							}
							JsonNode readTree = objMapper.readTree(objMapper.writeValueAsString(sna));
							formMessage.setContent(readTree);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}
}
