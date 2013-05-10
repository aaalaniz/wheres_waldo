import java.awt.Point;

import org.openimaj.image.pixel.Pixel;


public class MatchCandidate implements Comparable{
	int matches;
	int x;
	int y;
	Point topLeft;
	Point bottomRight;
	
	public MatchCandidate(int x, int y, int matches) {
		this.matches = matches;
		this.x = x;
		this.y = y;
		this.topLeft = new Point(x, y);
		this.bottomRight = new Point(x + ServerWorker.SUB_IMAGE_WIDTH, y + ServerWorker.SUB_IMAGE_HEIGHT);
	}

	@Override
	public int compareTo(Object o) {
		MatchCandidate candidate = (MatchCandidate) o;
		if (this.matches > candidate.matches) {
			return -1;
		} else if (this.matches < candidate.matches) {
			return 1;
		}
		return 0;
	}
	
	@Override
	public int hashCode() {
		return this.x + this.y + this.matches;
	}
	
	@Override
	public boolean equals(Object o) {
		MatchCandidate m = (MatchCandidate) o;
		
		return this.x == m.x && this.y == m.y; 
	}
	
	@Override
	public String toString() {
		return "[ matches=" + this.matches + ", x=" + this.x + ", y=" + this.y + " ]";
	}

	public boolean overlaps(MatchCandidate curBest) {
	      if (this.topLeft.x >= curBest.bottomRight.x 
	    		  || this.bottomRight.x <= curBest.topLeft.x 
	    		  || this.topLeft.y >= curBest.bottomRight.y 
	    		  || this.bottomRight.y <= curBest.topLeft.y)
	    		  return false;
	      
	      return true;
	}
}
