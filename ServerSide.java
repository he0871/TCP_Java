import java.net.InetAddress;
import java.util.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.*;

public class ServerSide {

	byte[] receivedData;
	DatagramPacket receivedPacket;
	DatagramPacket sentPacket;
	DatagramSocket serverSocket;
	int MAX_DATA_SIZE = 405;
	boolean openSocket = false;
	int PortNum = 2016;
	int PacketNum = 0;
	int vectorNums = 0;// how many vectors are contained in packer 
	
	byte[] keepData;
	byte[] totalData;
	
	//--------a list in need to store the data------//
	List<vector> Vdata;
	vector[] rslt;
	//-------------data structure of the header---------//
	int HeaderOffset = 5; //the length of header is 5
	int Dimension = 2;//the vector contains 2 elements
	//-------------state machine---------//
	// state = 0 initialization
	// state = 1 waiting for first data packet
	// state = 2 waiting for more data packet or REQ
	// state = 3 sent RACK packet after receiving REQ and waiting for multiple REQ
	// state = 4 sending CLUS
	// state = 5 waiting for CACK
	int state = 0;
	
	
	
	byte seqNumber;
	public ServerSide() {
		seqNumber = 0;
		keepData = new byte[MAX_DATA_SIZE];
		Vdata = new ArrayList<vector>();
		receivedData = new byte[MAX_DATA_SIZE];	
		receivedPacket = new DatagramPacket(receivedData,MAX_DATA_SIZE);
		try {
			serverSocket = new DatagramSocket(PortNum);
			state = 1;
		}	
		catch (Exception ex) {
			state = 0;
			System.out.println("error occurs during initialization period");
			System.out.println(ex.getMessage());
		}

	}
	
	//listen to the socket
	
	
	// run the state machine with incoming message
	public void run() {
		byte type = receivedData[0];
		// data packet arrived
		if((type == 0) && (state == 1)){
			receivedDataPacket();
			sentDACK();
		}
		
		if((type == 0) && (state == 2)){
			receivedDataPacket();
			state = 2;
			sentDACK();
		}
		
		//REQ packet arrived
		if((type == 2) && (state == 2)) {
			System.out.println("computing kmeans");
			sentRACK();
			kmeans();
			state = 3;
		}
		
		if(state == 3) { //Waiting for more REQ
			WatchDog4REQ();
		}
		
		if(state == 4) { 
			sentCLUS();	// state -> 5 if sends message successfully
		}
		
		if(state == 5) {
			WatchDog4CACK();
		}
	}
	
	private void receivedDataPacket()  {
		System.out.println("Receiving a data packet from the client...");
		
		vectorNums = (int)receivedData[4];
		System.out.printf("contains %d vectors\n\n", vectorNums);
		//received Data should be converted into vector
		Byte2Vector();
		//System.out.printf("the length of vectors list is %d\n", Vdata.size());
		
		if(seqNumber == receivedData[2]) {
			System.out.println("Sequence Number: "+ seqNumber);
			if(receivedData[2]==0) {
				state = 2;
				System.arraycopy(receivedData, 5, keepData, 0, receivedData.length-5);
			}
			else {
				totalData = new byte[keepData.length+receivedData.length-5];
				System.arraycopy(keepData,0,totalData,0,keepData.length);
				System.arraycopy(receivedData, 5, totalData, keepData.length, receivedData.length-5);
				keepData = totalData;
			}
		}
		else {
			state = 1;
			System.out.println("data packet is out of order");
		}
		
	}

	//listen to the socket
	public void listen()  {
		//System.out.printf("state is %d\n", state);
		try {
			if ((state == 1) || (state == 2)) {
				//listen to the socket patiently, no need a timeout here
				serverSocket.receive(receivedPacket);
				System.out.println("new packet is arriving");
				System.out.printf("type is %x\n", receivedData[0]);
			}
			else {
				System.out.println("Server is not ready for listen!");
			}
		}
		catch(Exception ex) {
			System.out.printf("port num is %d\n", receivedPacket.getPort());
			System.out.printf("Addr is %s\n", receivedPacket.getAddress().toString());
			System.out.println("listening is interrupted");
			return;
		}	
	}
	// especially for multi-REQ
	private void WatchDog4REQ()  {
		try {
			if (state == 3) {
				//listen to the socket patiently, no need a timeout here
				//receivedPacket = new DatagramPacket(receivedData,MAX_DATA_SIZE);
				serverSocket.receive(receivedPacket);
				serverSocket.setSoTimeout(3000);
				if(2 == receivedData[0]) {
					System.out.println("multiple REQ occurs!");
					state = 3; //keep
				}
			}
			else {
				System.out.println("No need this Watch Dog!");
			}
		}
		catch(Exception ex) {
			System.out.println("No more REQ, great!");
			state = 4;
			return;
		}	
	}
	
	private void WatchDog4CACK()  {
		int tmot = 1;
		int base = 1000;
		System.out.println("WatchDog4CACK");
		while(tmot <= 4) {
			try{
				//listen to the socket patiently, no need a timeout here
				//receivedPacket = new DatagramPacket(receivedData,MAX_DATA_SIZE);
				serverSocket.receive(receivedPacket);
				serverSocket.setSoTimeout(tmot * base);
				state = 1;//mission completed, reset the server
				if(5 == receivedData[0]) {
					System.out.println("session completed");
				}
				else {
					System.out.println("worng header of CLUS");
				}
				System.exit(0);
				return;
			}
			catch(Exception ex) {
				tmot *= 2;
				System.out.printf("Timeout for waiting CACK, new time out is %d seconds\n", tmot);
			}	
		}
		System.out.println("communication failure");
		System.exit(0);// assume the client is lost, reset the server
		return;
	}
	
	public void sentDACK()  {
		byte[] sentData = {1, 0, seqNumber};
		//System.out.print("\n\n sentData: "+ seqNumber);
		
		//receivedPacket = new DatagramPacket(receivedData,MAX_DATA_SIZE);
		sentPacket = new DatagramPacket(sentData, 3, receivedPacket.getAddress(), receivedPacket.getPort());
		try {
			serverSocket.send(sentPacket);
		}
		catch(Exception ex) {	
			state = 0;
			System.out.println("error occurs during sending DACK");
			System.out.println(ex.getMessage());
		}
		seqNumber++;
		//return sentData;
	}
	
	public boolean receivedREQ() throws Exception {
		byte[] reqPacket = new byte[1];
		//receivedPacket = new DatagramPacket(reqPacket,1);
		
		//serverSocket.receive(receivedPacket);
		//reqPacket = receivedPacket.getData();
		reqPacket = receivedData;
		System.out.println("reqPacket ==receivedData");
		if(reqPacket[0]==2) {
			System.out.println("reqPacket ==2");
			return true;
		}
		else {
			return false;
		}
		
	}
	
	private void sentRACK()  {
		
		byte[] sentData = {3};
		sentPacket = new DatagramPacket(sentData, 1, receivedPacket.getAddress(), receivedPacket.getPort());
		System.out.println("\n sent RACK");
		try {
			serverSocket.send(sentPacket);
		}
		catch(Exception ex) {	
			state = 0;
			System.out.println("error occurs during sending RACK");
			System.out.println(ex.getMessage());
		}
	}	
	private void sentCLUS(){
		byte[] sentData = new byte[17];
		sentData[0] = 4;
		byte[] pt1 = rslt[0].toByte();
		byte[] pt2 = rslt[1].toByte();
		System.arraycopy(pt1, 0, sentData, 1, 8);
		System.arraycopy(pt2, 0, sentData, 9, 8);
		//receivedPacket = new DatagramPacket(receivedData,MAX_DATA_SIZE);
		//System.out.printf("port num is %d\n", receivedPacket.getPort());
		//System.out.printf("Addr is %s\n", receivedPacket.getAddress().toString());
		DatagramPacket CLUSpacket = new DatagramPacket(sentData, 17, receivedPacket.getAddress(), receivedPacket.getPort());
		System.out.println("\n sent CLUS");
		for(int i = 0; i < sentData.length; i++) {
			System.out.print(sentData[i] + " ");
		}
		System.out.println("");
		try {
			serverSocket.send(CLUSpacket);
			state = 5;
		}
		catch(Exception ex) {
			state = 4; //go back to state 2
			System.out.println("error occurs when sends CLUS");
			System.out.println(ex.getMessage());
		}
	}
	
	public void close() {
		serverSocket.close();	
	}
	
	private int byte2int(byte [] bArr) {
		//the length of bArr should be 4
		int value = 0;
		ByteBuffer wrapped = ByteBuffer.wrap(bArr);
		value = wrapped.getInt();
		return value;
	}
			
	private void Byte2Vector() {
		//assume the message body is integrated 
		int offset = HeaderOffset;
		int pt = HeaderOffset; 
		byte [] buffer = new byte[4];
		double xValue = 0;
		double yValue = 0;
		vector temp;
		
		
		while (pt < vectorNums*8 + 5) {
			//System.out.printf("The pt is %d\n", pt);
			buffer = Arrays.copyOfRange(receivedData, pt, pt + 4);
			xValue = (double)byte2int(buffer);
			xValue = xValue / 100;
			//System.out.printf("x is %f ", xValue);
			buffer = Arrays.copyOfRange(receivedData, pt + 4, pt + 8);
			yValue = (double)byte2int(buffer);
			yValue = yValue / 100;
			//System.out.printf("y is %f \n", yValue);
			temp = new vector(xValue, yValue);
			Vdata.add(temp);
			pt += 8;
		}
	}
	
	private void kmeans() {
		clustering cluster = new clustering();
		rslt = cluster.run(Vdata);	
		return;
	}
	
	//after recieved REQ, wait 3 seconds to guarantee that client received RACK 
	
	
}
