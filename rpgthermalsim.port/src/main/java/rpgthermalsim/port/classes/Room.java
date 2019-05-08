package rpgthermalsim.port.classes;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import rpgthermalsim.port.exceptions.CellException;
import rpgthermalsim.port.exceptions.RoomException;

public class Room implements Digestable{
	ArrayList<Cell> layout;
	int w,h;
	private String desc;

	public Room(int w, int h, String desc) {
		layout = new ArrayList<Cell>();
		this.w = w;
		this.h = h;
		this.desc = desc;
		
		for(int i = 0;i<w*h;i++) {
			Cell c;
			c = new Cell();
			layout.add(i, c);
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
		StringBuilder oss = new StringBuilder();
		oss.append(this.desc + System.lineSeparator());
		for(int ih = this.h-1;ih>=0;ih--) {
			for(int jw = 0 ; jw < this.w ; jw++) {
				oss.append(this.layout.get(ih*this.w+jw).toString());
			}
			oss.append(System.lineSeparator());
		}
		return oss.toString();
	}

	public void iterate() {
		for(int i = 0;i<layout.size();i++) layout.get(i).spread();
		for(int i = 0;i<layout.size();i++) layout.get(i).commitStatus();
		for(int i = 0;i<layout.size();i++) layout.get(i).checkFlashpoint();
		for(int i = 0;i<layout.size();i++) layout.get(i).dissipateHeat();
	}

	public Cell getCellXY(int x, int y) throws RoomException {
		if(x >= w || y >= h || x < 0 || y < 0) throw new RoomException("The requested cell does not exist.");
		return layout.get(w*y+x);
	}

	public String getDesc() {
		return this.desc;
	}

	@Override
	public String digest() throws NoSuchAlgorithmException {
		StringBuilder oss = new StringBuilder();
		oss.append(w);
		oss.append(h);
		oss.append(desc);
		for(int i = 0; i<layout.size();i++) {
			oss.append(layout.get(i).digest());
		}
		return digest(oss.toString());
	}
}
