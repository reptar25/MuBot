package com.github.MudPitBot.Heroku;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HerokuSocket extends Thread {

	private static ServerSocket serverSocket;
	private int port;
	private boolean running = false;

	public HerokuSocket(int port) {
		this.port = port;

		this.startServer();
	}

	public void startServer() {
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(0);
			this.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stopServer() {
		running = false;
		this.interrupt();
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				System.out.println("Listening for a connection");

				// Call accept() to receive the next connection
				Socket socket = serverSocket.accept();
				socket.setSoTimeout(0);
				System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
