package com.main.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.main.messages.DeleteAfterBeforeMessage;
import com.main.messages.DeleteMessage;
import com.main.messages.forms.Ellipse;
import com.main.messages.forms.FormMessage;
import com.main.messages.forms.Line;
import com.main.messages.forms.Polygon;
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
	public static List<String> lastAnimatedElements = new ArrayList<>();

	private HistoryHandler() {
		history = new History();
	}

	public synchronized static HistoryHandler getInstance() {
		return historyHandler;
	}

	public synchronized void addHistory(FormMessage m) {
		m.setxAnimation(r.nextInt(20)-10);
		m.setyAnimation(r.nextInt(20)-10);
		history.addHistory(m);
		;
	}

	public synchronized List<FormMessage> getHistory() {
		return history.getHistory();
	}

	public synchronized void deleteHistory() {
		history.deleteHistory();
	}

	public synchronized void deleteHistoryItemsById(Long id) {
		history.deleteHistoryItemsById(id);
	}

	public synchronized long getCurrentId() {
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
	
	private int testNewXPosition(FormMessage form, int newPosition, int size){
		if(newPosition + size>maxSizeX || newPosition < 0){
			form.setxAnimation(-form.getxAnimation());
			return newPosition - 2*form.getxAnimation();
		}
		return newPosition;
	}
	
	private int testNewYPosition(FormMessage form, int newPosition, int size){
		if(newPosition +size>maxSizeY || newPosition < 0){
			form.setyAnimation(-form.getyAnimation());
			return newPosition - 2*form.getyAnimation();
		}
		return newPosition;
	}
	
	private int testNextStep(int pos,int move, int size){
		if(pos+size+move > maxSizeX || pos + move < 0){
			return -move;
		}
		return move;
	}
	
	public synchronized void unFlagAnimmatedHistoryObjects(DeleteMessage objectsToUnflag){
		for (String id : objectsToUnflag.getIds()) {
			history.getHistory().parallelStream()
					.filter(e -> e.getId() == Long.valueOf(id))
					.forEach(e-> e.setAnimated(false));
		}
	}

	public synchronized void animate(DeleteMessage objectsToAnimate) {
//		synchronized (lastAnimatedElements) {
//			if (!lastAnimatedElements.isEmpty() || !lastAnimatedElements.containsAll(objectsToAnimate.getIds())) {
//				lastAnimatedElements.removeAll(objectsToAnimate.getIds());
//				for (String id : lastAnimatedElements) {
//					synchronized (history.getHistory()) {
//						history.getHistory()
//								.stream()
//								.filter(e -> e.getId() == Long.valueOf(id))
//								.forEach(formMessage -> formMessage.setAnimated(false));
//					}
//				}
//			}
//			lastAnimatedElements = objectsToAnimate.getIds();
//		}

		for (String id : objectsToAnimate.getIds()) {
			synchronized (history.getHistory()) {
				List<FormMessage> collect = history.getHistory().parallelStream()
						.filter(e -> e.getId() == Long.valueOf(id)).collect(Collectors.toList());
				for (FormMessage formMessage : collect) {
					formMessage.setAnimated(true);
					switch (formMessage.getType()) {
					case RECTANGLE:
						Rectangle rec;
						try {
							rec = objMapper.readValue(formMessage.getContent(), Rectangle.class);
							formMessage.setxAnimation(testNextStep(rec.getX(), formMessage.getxAnimation(), rec.getA()));
							formMessage.setyAnimation(testNextStep(rec.getY(), formMessage.getyAnimation(), rec.getB()));
							rec.setX(rec.getX()+formMessage.getxAnimation());
							rec.setY(rec.getY()+formMessage.getyAnimation());
							JsonNode readTree = objMapper.valueToTree(rec);
							formMessage.setContent(readTree);
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					case ELLIPSE:
						Ellipse ell;
						try {
							ell = objMapper.readValue(formMessage.getContent(), Ellipse.class);
							formMessage.setxAnimation(testNextStep(ell.getX(), formMessage.getxAnimation(), ell.getA()));
							formMessage.setyAnimation(testNextStep(ell.getY(), formMessage.getyAnimation(), ell.getB()));
							ell.setX(ell.getX()+formMessage.getxAnimation());
							ell.setY(ell.getY()+formMessage.getyAnimation());
							JsonNode readTree = objMapper.valueToTree(ell);
							formMessage.setContent(readTree);
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					case LINE:
						Line li;
						try {
							li = objMapper.readValue(formMessage.getContent(), Line.class);
							formMessage.setxAnimation(testNextStep(li.getX(), formMessage.getxAnimation(), 0));
							formMessage.setyAnimation(testNextStep(li.getY(), formMessage.getyAnimation(), 0));
							formMessage.setxAnimation(testNextStep(li.getA(), formMessage.getxAnimation(), 0));
							formMessage.setyAnimation(testNextStep(li.getB(), formMessage.getyAnimation(), 0));
							li.setX(li.getX()+formMessage.getxAnimation());
							li.setY(li.getY()+formMessage.getyAnimation());
							li.setA(li.getA()+formMessage.getxAnimation());
							li.setB(li.getB()+formMessage.getyAnimation());
							JsonNode readTree = objMapper.valueToTree(li);
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
								JsonNode readTree = objMapper.valueToTree(sna);
								formMessage.setContent(readTree);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					case POLYGON:
						Polygon pol;
						try {
							pol = objMapper.readValue(formMessage.getContent(), Polygon.class);
							for (int i = 0; i < pol.getaElements().length; ++i) {
								formMessage.setxAnimation(testNextStep(pol.getaElements()[i], formMessage.getxAnimation(), 0));
								formMessage.setyAnimation(testNextStep(pol.getbElements()[i], formMessage.getyAnimation(), 0));
								pol.getaElements()[i] =  pol.getaElements()[i]+formMessage.getxAnimation();
								pol.getbElements()[i] =  pol.getbElements()[i]+formMessage.getyAnimation();
							}
							JsonNode readTree = objMapper.valueToTree(pol);
							formMessage.setContent(readTree);
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
		}
	}

	public void deleteAfter(DeleteAfterBeforeMessage objectsToDeleteAfter) {
		synchronized (history.getHistory()) {
			List<FormMessage> hist = history.getHistory();
			hist.removeIf(e -> e.getId() > Long.valueOf(objectsToDeleteAfter.getId()));
		}
	}

	public void deleteBefore(DeleteAfterBeforeMessage objectsToDeleteBefore) {
		synchronized (history.getHistory()) {
			List<FormMessage> hist = history.getHistory();
			hist.removeIf(e -> e.getId() < Long.valueOf(objectsToDeleteBefore.getId()));
		}
	}
}
