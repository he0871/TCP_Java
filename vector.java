
public class vector {
	double x;
	double y;

	
	public vector(double v1, double v2){
		x = v1;
		y = v2;
	}
	
	public vector(vector another){
		x = another.x;
		y = another.y;
	}
	
	public double distance(double v1, double v2) {
		double dst = 0;
		dst = Math.pow((v1 - x), 2) + Math.pow((v2 - y), 2);
		dst = Math.sqrt(dst);
		return dst;
	}
	public double distance(vector another) {
		double dst = 0;
		dst = Math.pow((another.x - x), 2) + Math.pow((another.y - y), 2);
		dst = Math.sqrt(dst);
		return dst;
	}
	public void add(vector another) {
		x += another.x;
		y += another.y;
	}
	public void divide(int num) {
		x = x / num;
		y = y / num;
	}
	
	public void copy(vector another) {
		x = another.x;
		y = another.y;
	}
	
	public String toString() {
		String rslt = String.format("x = %f and y = %f", x, y);
		return rslt;
	}
	
	public byte[] toByte() {
		byte[] rslt = new byte[8];
		byte[] xbt = intToByteArray((int)(x*100));
		byte[] ybt = intToByteArray((int)(y*100));
		System.arraycopy(xbt, 0, rslt, 0, 4);
		System.arraycopy(ybt, 0, rslt, 4, 4);
		return rslt;
	}
	

	public static byte[] intToByteArray(int a) {
		
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	


}
