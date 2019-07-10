import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientSide {
	
	private byte[] setServerAddr;
	private int setPortNumber;
	private int count;
	double[][] dataValue;
	//int[][] intDataValue;
	byte[] eachDia;
	byte[] byteData;
	private int MAX_PACKET_SIZE = 50;
	int numPacket;
	int packetNumber;
	
	DatagramPacket clientPacket;
	DatagramSocket clientSocket;
	
	byte[] receivedDACK = new byte[3];
	
	int countPortNumber =0;
	
	byte[] receivedRACK = new byte[1];
	
	int timeout = 1000;
	
	final static int ClusPacketSize = 17;
	
	public ClientSide(byte[] serverAddress, int portNumber) throws Exception {

	}
	
	public double[][] getValue(String link) throws Exception {
		
		File file = new File(link);
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		count = 0;
		
		String[] numSet = new String[500];
		while((link = br.readLine()) != null) {
			numSet[count] = link;
			count++;
		}
		
		String[] inputSet = new String[count];
		inputSet = numSet;
		
		Pattern p = Pattern.compile("");
		dataValue = new double[count][2];
		for(int i = 0; i<count; i++) {
			
			String st = inputSet[i];
			p = Pattern.compile("\\(");
			Matcher m = p.matcher(st);
			StringBuffer sb = new StringBuffer();
			while(m.find()) {
				m.appendReplacement(sb, "");	
			}
			m.appendTail(sb);
			st = sb.toString();
			
			p = Pattern.compile("\\,");
			m = p.matcher(st);
			sb = new StringBuffer();
			while(m.find()) {
				m.appendReplacement(sb, "");	
			}
			m.appendTail(sb);
			
			st = sb.toString();
			
			p = Pattern.compile("\\)");
			m = p.matcher(st);
			sb = new StringBuffer();
			while(m.find()) {
				m.appendReplacement(sb, "");	
			}
			m.appendTail(sb);
			st = sb.toString();
			
			String[] arrOfSt = st.split(" ",2);
			
			dataValue[i][0] = Double.parseDouble(arrOfSt[0]);
			dataValue[i][1] = Double.parseDouble(arrOfSt[1]);
			
			//System.out.println("1First: " + dataValue[i][0] + " 2Second: " + dataValue[i][1]);
			
			
		}
		
		return dataValue;
	}
	
	public int totalNumberOfData() {
		return count;
	}
	
	public byte[] convertToByte() {	
		byte[] x;
		byte[] y;
		byteData = new byte[count*8+1];
		System.out.printf("count is %d\n", count);
		for(int i = 0; i<count; i++) {
			x = intToByteArray((int)Math.round(dataValue[i][0] * 100));
			System.out.printf("x is %d\n",  Math.round(dataValue[i][0] * 100));
			System.arraycopy(x, 0,byteData, i*8, x.length);
			y = intToByteArray((int)(dataValue[i][1] * 100));
			System.arraycopy(y, 0,byteData, i*8+4, y.length);
						
		}
			
		for(int j = 0; j < byteData.length; j++) {
			System.out.print(byteData[j] + " "); // 
		}
		return byteData;
	}
	
	public void dataSet() {
		int a = 0;
		double num = Math.ceil(count/(double)MAX_PACKET_SIZE);
		int numPacket = (int)num;
		System.out.printf("numPacket is %d\n", numPacket);
		for(int i = 1; i<= numPacket; i++) {
			if(i<numPacket) {
				System.out.println("\nPacket "+ i);
				for(int j = 1; j<=MAX_PACKET_SIZE*8; j++) {
					System.out.print(byteData[a] + " ");
					a++;
				}
				
			}
			else {
				System.out.println("\nPacket "+ i);
				for(int j = 1; j<=count%MAX_PACKET_SIZE*8; j++) {
					System.out.print(byteData[a] + " ");
					a++;
				}
			}
		}
		
	}
	
	public int numberOfDataPacket() {
		double num = Math.ceil(count/(double)MAX_PACKET_SIZE);
		numPacket = (int)num;
		return numPacket;
	}
	
	public byte[] dataPacket(int seqNum) {
		int size = 0;
		if(seqNum<(numPacket-1)) {
			size = MAX_PACKET_SIZE;
		}
		else {
			size = count%MAX_PACKET_SIZE;
		}
		byte[] dataPacket = new byte[5+(size*8)];
		int mask = 0xFF;
		
		// Packet Type
		dataPacket[0] = 0x00;
		
		// Sequence Number
		dataPacket[2] = (byte) (seqNum&mask);
		dataPacket[1] = (byte)((seqNum>>8)&mask);
		
		// Number of Data Vectors
		dataPacket[4] = (byte)(size);
		dataPacket[3] = (byte)((size>>8)&mask);
		
		// data vector
		System.out.printf("the size of byteData is %d\n", byteData.length);
		for(int i = 0; i< (size*8); i++) {
			//System.out.printf("i = %d and the size of byteData is %d\n", i, byteData.length);
			dataPacket[i+5] = byteData[(seqNum*MAX_PACKET_SIZE*8)+i];			
		}
		
		return dataPacket;
		
	}
	
	public int dataPacketSize(int seqNum) {

		int packetSize = 0;
		if(seqNum<(numPacket-1)) {
			packetSize = 5+(MAX_PACKET_SIZE*8);
		}
		else {
			packetSize = 5+(count%MAX_PACKET_SIZE)*8;
		}
		
		return packetSize;
	}
	
	public void sentDataPacket(byte[] sentData, int packetSize, InetAddress setServerAddress, int portNumber) throws Exception{
		// 4 create DatagramPacket object
		clientPacket = new DatagramPacket(sentData, packetSize, setServerAddress, portNumber);
		
		//5 create DatagramSocket object
		if(countPortNumber == 0) {
			clientSocket = new DatagramSocket(portNumber);
			countPortNumber = portNumber;
		}
				
		//6 send UDP packet
		clientSocket.send(clientPacket);
		System.out.println("Sending the request to the server ...");
		//clientSocket.close();
				
	}
	
	public boolean checkDACK(int seqNumber, int timeout) throws Exception {
		clientPacket = new DatagramPacket(receivedDACK, 3);
		clientSocket.setSoTimeout(timeout);
		try {
			System.out.println("Receiving the response from the server ...");
			clientSocket.receive(clientPacket);

		}
		catch(Exception ex) {
			System.out.print("Client socket timeout! Exception message: ");
			System.out.println(ex.getMessage());
			if(timeout>4000) {
				System.exit(0);
			}
		}
		
		System.out.println("DACK received:");
		for(int i = 0; i<3; i++) {
			System.out.print(" "+ receivedDACK[i]);
		}
		if(receivedDACK[2]==seqNumber) {
			System.out.println("TRUEE");
			return true;
			
		}
		else {
			return false;
		}
		
	}
	
	public boolean checkRACK(int timeout) throws Exception{
		
		clientPacket = new DatagramPacket(receivedRACK,1);
		clientSocket.setSoTimeout(timeout);
		System.out.println("Timeout");
		try {
			System.out.println("Receiving the response from the server ...");
			clientSocket.receive(clientPacket);

		}
		catch(Exception ex) {
			System.out.print("Client socket timeout! Exception message: ");
			System.out.println(ex.getMessage());
			if(timeout>4000) {
				System.exit(0);
			}
		}
		
		if(receivedRACK[0]==3) {
			System.out.println("\nRACK TRUE");
			return true;
		}
		else {
			System.out.println("\nRACK FALSE");
			return false;
		}
		
	}

	
	public void download() {
		byte[] receivedCLUS = new byte[ClusPacketSize];
		try {
			DatagramPacket DLpacket = new  DatagramPacket(receivedCLUS, ClusPacketSize);
			clientSocket.setSoTimeout(30 * 1000);
			clientSocket.receive(DLpacket);
			
			/*for(int i = 0; i < receivedCLUS.length; i++) {
				System.out.print(receivedCLUS[i] + " ");
			}*/
		}
		catch(Exception ex) {
			System.out.println("failed to download data from server");
			System.out.println(ex.getMessage());
			System.exit(0);
		}
		//header check
		byte head = 4;
		if(head != receivedCLUS[0]) {
			System.out.println("wrong header");
			System.exit(0);
		}
		byte[] pt1 = new byte[8];
		byte[] pt2 = new byte[8];
		System.arraycopy(receivedCLUS, 1, pt1, 0, 8);
		System.arraycopy(receivedCLUS, 9, pt2, 0, 8);
		double[] c1 = extractData(pt1);
		System.out.printf("the first point is %f,%f\n", c1[0], c1[1]);
		double[] c2 = extractData(pt2); 
		System.out.printf("the second point is %f,%f\n", c2[0], c2[1]);
	}
	
	public void sentCACK(InetAddress setServerAddress, int portNumber) {
		byte[] CACK = {5};
		clientPacket = new DatagramPacket(CACK, 1, setServerAddress, portNumber);
		try {
			clientSocket.send(clientPacket);
		}
		catch(Exception ex) {
			System.out.println("fail to send CACK packet");
			System.out.println(ex.getMessage());
		}
	}
	
	public void close() {
		//free resources especially the port
		clientSocket.close();
	}
	
	public static byte[] intToByteArray(int a) {
		
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	
	private int byte2int(byte [] bArr) {
		//the length of bArr should be 4
		int value = 0;
		ByteBuffer wrapped = ByteBuffer.wrap(bArr);
		value = wrapped.getInt();
		return value;
	}
	
	private double[] extractData(byte[] bVector) {
		//the length of bVector should be 8
		double[] rslt = new double[2];
		byte[] xByte = new byte[4];
		byte[] yByte = new byte[4];
		System.arraycopy(bVector, 0, xByte, 0, 4);
		System.arraycopy(bVector, 4, yByte, 0, 4);
		double xvalue = (double)byte2int(xByte);
		double yvalue = (double)byte2int(yByte);
		xvalue = xvalue / 100;
		yvalue = yvalue / 100;
		rslt[0] = xvalue;
		rslt[1] = yvalue;
		return rslt;
	}
	
}
