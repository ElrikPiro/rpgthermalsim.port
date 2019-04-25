package rpgthermalsim.port.classes;

import java.util.ArrayList;

public class Room {
	ArrayList<Cell> layout;
	int w,h;
	private String desc;

	public Room(int w, int h, String desc) {
		this.w = w;
		this.h = h;
		this.desc = desc;
		
		for(int i = 0;i<w*h;i++) {
			Cell c;
			try {
				c = new Cell("0,0,0,1");
				layout.add(i, c);
			} catch (CellException e) {
				e.printStackTrace();
				System.err.println("This shouldn't ever happened.");
				System.exit(-1);
			}
		}
		
		setNeigthbours();
	}

	private void setNeigthbours() {
		for(int i = 0; i < w*h; i++) {
			int jw = (i%w);
			int jh = (i/w);
			
			jh--; jw--;
			if((jw)>=0 && (jw<w) && jh>=0 && jh<h) layout.get(i).addNeightbour(layout.get(jw+jh*w));
			jw++;
			if((jw)>=0 && (jw<w) && jh>=0 && jh<h) layout.get(i).addNeightbour(layout.get(jw+jh*w));
			jw++;
			if((jw)>=0 && (jw<w) && jh>=0 && jh<h) layout.get(i).addNeightbour(layout.get(jw+jh*w));
			
			jh++;
			if((jw)>=0 && (jw<w) && jh>=0 && jh<h) layout.get(i).addNeightbour(layout.get(jw+jh*w));
			jw-=2;
			if((jw)>=0 && (jw<w) && jh>=0 && jh<h) layout.get(i).addNeightbour(layout.get(jw+jh*w));
			
			jh++;
			if((jw)>=0 && (jw<w) && jh>=0 && jh<h) layout.get(i).addNeightbour(layout.get(jw+jh*w));
			jw++;
			if((jw)>=0 && (jw<w) && jh>=0 && jh<h) layout.get(i).addNeightbour(layout.get(jw+jh*w));
			jw++;
			if((jw)>=0 && (jw<w) && jh>=0 && jh<h) layout.get(i).addNeightbour(layout.get(jw+jh*w));
		}
		
	}

	public String toString(){
		String str = "";
		str.concat(this.desc + System.lineSeparator());
		for(int ih = this.h-1;ih>=0;ih--) {
			for(int jw = 0 ; jw < this.w ; jw++) {
				str.concat(this.layout.get(ih*this.w+jw).toString());
			}
			str.concat(System.lineSeparator());
		}
		return str;
	}

	public void iterate() {
		for(int i = 0;i<w*h;i++) layout.get(i).spread();
		for(int i = 0;i<w*h;i++) layout.get(i).commitStatus();
		for(int i = 0;i<w*h;i++) layout.get(i).checkFlashpoint();
		for(int i = 0;i<w*h;i++) layout.get(i).dissipateHeat();
	}

	public Cell getCellXY(int x, int y) throws RoomException {
		if(x >= w || y >= h || x < 0 || y < 0) throw new RoomException("The requested cell does not exist.");
		return layout.get(w*y+x);
	}

	public String getDesc() {
		return this.desc;
	}
}
