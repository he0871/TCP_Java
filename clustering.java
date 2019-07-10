import java.util.*;


public class clustering {
	//long start;
	public clustering() {
		//start = System.currentTimeMillis();
	}
	
	public vector[] run(List<vector> ListData) {
		double[] range = DTrange(ListData);//{Xmax, Xmin, Ymax, Ymin}
		double xdiff = range[0] - range[1];
		double ydiff = range[2] - range[3];
		double xbias = (xdiff/2) - ((range[0] + range[1])/2);
		double ybias = (ydiff/2) - ((range[2] + range[3])/2);
		//System.out.println(xdiff);
		//System.out.println(xbias);
		// K-means & k = 2
		Random rand = new Random();
		vector m1 = new vector(xdiff*rand.nextDouble()-xbias, ydiff*rand.nextDouble()-ybias); //random centroid
		vector m2 = new vector(xdiff*rand.nextDouble()-xbias, ydiff*rand.nextDouble()-ybias); //random centroid
		vector c1 = new vector(m1); //redirection centroid
		vector c2 = new vector(m2); //redirection centroid
		List<vector> g1 = new ArrayList<vector>(); 
		List<vector> g2 = new ArrayList<vector>();
		int nc1 = 0;
		int nc2 =0;
		double threshold = 0.00000000001;//Threshold to determine the "change" 
		do{
			m1.copy(c1);
			m2.copy(c2);
			nc1 = 0;
			nc2 =0;
			//System.out.println(c1.toString());
			//System.out.println(c2.toString());
			
			for(int i = 0; i < ListData.size(); i++) {
				if (ListData.get(i).distance(m1) < ListData.get(i).distance(m2)) {
					//g1.add(ListData.get(i));
					c1.add(ListData.get(i));
					nc1++;
				}
				else {
					//g2.add(ListData.get(i));
					c2.add(ListData.get(i));
					nc2++;
				}
			}
		//replace centroid by mean
		c1.divide(nc1);
		c2.divide(nc2);
		}
		while((m1.distance(c1) + m2.distance(c2)) > threshold);
		System.out.println(c1.toString());
		System.out.println(c2.toString());
		vector[] rslt = new vector[2];
		rslt[0] = new vector(c1);
		rslt[1] = new vector(c2);
		return rslt;
	}
	

	private double [] DTrange(List<vector> ListData) {
		//measures the range of data, and decides the 
		//range of random number according to the data range.  
		double [] range = {0,0,0,0};//{Xmax, Xmin, Ymax, Ymin}
		for(int i = 0; i < ListData.size(); i++) {
			if(ListData.get(i).x > range[0]) {
				range[0] = ListData.get(i).x;
			}
			if(ListData.get(i).x < range[1]) {
				range[1] = ListData.get(i).x;
			}
			if(ListData.get(i).y > range[2]) {
				range[2] = ListData.get(i).y;
			}
			if(ListData.get(i).y < range[3]) {
				range[3] = ListData.get(i).y;
			}
		}
		return range;
	}
}
