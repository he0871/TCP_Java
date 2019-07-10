

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientTest {

	public static void main(String[] args) throws Exception{
		// Get file From
		String fileFrom = "D:\\class\\ENTS640\\project\\data01.txt";
		
		// Set server address and port number
		InetAddress setServerAddress;
		byte[] serverAddress = {(byte)192,(byte)168,(byte)0,(byte)74};
		setServerAddress = InetAddress.getByAddress(serverAddress);
		int portNumber = 2028;
		ClientSide setClient = new ClientSide(serverAddress ,portNumber);
		
		// get value from file
		setClient.getValue(fileFrom);
		
		// total number of data in file
		int totalData = setClient.totalNumberOfData();
		
		// convert data to byte array
		byte[] dataSet = setClient.convertToByte();
		
		// calculate number of packet to send
		int numberOfPacket = setClient.numberOfDataPacket();
		
		// send packet
		
		int seqNumber = 0;
		byte[] dataPacket = setClient.dataPacket(seqNumber);
		int packetSize = setClient.dataPacketSize(seqNumber);
		byte[] sentData = new byte[packetSize];
		sentData = dataPacket;
		
		// Sent DataPacket
		setClient.sentDataPacket(sentData, packetSize, setServerAddress, portNumber);
		int timeout = 1000;			
		// receive DACK and Check
		boolean seqDACK = setClient.checkDACK(seqNumber, timeout);
		System.out.println("ONEEEE");
		
		boolean reqPacketBoo = false;
		
		while(seqNumber<numberOfPacket) {
			
			if(seqDACK == true && seqNumber<(numberOfPacket-1)) {
				System.out.println("TWOO");
				seqNumber++;
				dataPacket = setClient.dataPacket(seqNumber);
				packetSize = setClient.dataPacketSize(seqNumber);
				sentData = new byte[packetSize];
				sentData = dataPacket;
				setClient.sentDataPacket(sentData, packetSize, setServerAddress, portNumber);
				seqDACK = setClient.checkDACK(seqNumber, timeout);
			}
			else if (seqDACK == false){
				setClient.sentDataPacket(sentData, packetSize, setServerAddress, portNumber);
				timeout = timeout*2;
				seqDACK = setClient.checkDACK(seqNumber,timeout);
			}
			else if (seqDACK==true && seqNumber == (numberOfPacket-1)) {
				reqPacketBoo = true;
				System.out.println("reqPacketBoo");
				seqNumber++;
			}
			
		}
		// req
		timeout =1000;
		boolean checkRack;
		while(seqNumber == (numberOfPacket)&& reqPacketBoo == true) {
			byte[] reqPacket = {2};
			sentData = new byte[1];
			sentData = reqPacket;
			// sent REQUEST
			setClient.sentDataPacket(sentData, 1, setServerAddress, portNumber);
			// check RACK
			System.out.println("\nCheck RACK");
			
			checkRack = setClient.checkRACK(timeout);
			if(checkRack ==true) {
				System.out.println("\nreceived RACK");
				seqNumber++;
			}
			else {
				timeout = timeout*2;
				System.out.println("Timeout = "+ timeout);
			}
		}
		
		
		
		//int ackNumber = 0;
		
		//
		
		setClient.download(); // process will exit when download is failure
		setClient.sentCACK(setServerAddress, portNumber);
		
		
		
	}

}
