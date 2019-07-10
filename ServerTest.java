import java.net.InetAddress;
import java.util.Random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
public class ServerTest {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//1 initialized the server and open the socket
		ServerSide setServer = new ServerSide();	
		int forever = 0;
		while(forever==0) {
			setServer.listen();
			setServer.run();
		}
	}
}
