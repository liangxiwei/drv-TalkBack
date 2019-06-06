package com.example.jrd48.service;

public interface Startable {
	String getName();
	boolean isStarted();
	void start();
	void stop();
	void restart();
}
