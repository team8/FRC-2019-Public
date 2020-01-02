package com.palyrobotics.frc2019.util.commands;

import org.opencv.core.Point;

import com.esotericsoftware.kryonet.*;
import com.palyrobotics.frc2019.util.config.AbstractConfig;

/**
 * Input data recieved from odroid
 *
 * @author Aditya Oberai
 */
public class KumquatCommandReciever {

	Client client;
	private AtomicString mResult = new AtomicString();
	private static KumquatCommandReciever.VisionData sVisionData = new KumquatCommandReciever.VisionData();

	public KumquatCommandReciever() {
		client = new Client();
		client.getKryo().setRegistrationRequired(false);
		try {
			client.addListener(new Listener() {

				@Override
				public void connected(Connection connection) {
					System.out.println("Connected!");
				}

				@Override
				public void disconnected(Connection connection) {
					System.out.println("Disconnected!");
				}

				@Override
				public void received(Connection connection, Object data) {
					if (data instanceof VisionData) { // I might have to do
						// 'data.getClass().getName().equals("VisionData");'
						client.sendTCP(connection.getID());
						sVisionData = (VisionData) data;
					} else {
						System.out.println("not instance of VisionData");
					}
				}
			});
		} catch (IllegalMonitorStateException exception) {
			exception.printStackTrace();
		}
		client.start();
	}

	public static KumquatCommandReciever.VisionData getVisionData() {
		return sVisionData;
	}

	public static class VisionData extends AbstractConfig {

		public static int tx = -1;
		public static Point centroidPoint = new Point(-1, -1);
	}
}
