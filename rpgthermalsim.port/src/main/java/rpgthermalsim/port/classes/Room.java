package rpgthermalsim.port.classes;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONObject;

import rpgthermalsim.port.exceptions.CellException;
import rpgthermalsim.port.exceptions.RoomException;

/**
 * 
 * Creates and manages a set of related cells that can be represented in a grid.
 * 
 * @author David Baselga
 * @since 0.1
 *
 */
public class Room implements Digestable{
	ArrayList<Cell> layout;
	int w,h;
	private String desc;

	/**
	 * 
	 * Builds a room with the defined width and height and assigns it a description. The room will contain
	 * width*height Cells linked with their neightbours.
	 * 
	 * @param w room's width
	 * @param h room's height
	 * @param desc room's description
	 */
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

	/**
	 * For every Cell in the room, that cell is linked to it's neighbouring cells.
	 */
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
			if(ih<10) oss.append("00"+ih);
			else if(ih<100) oss.append("0"+ih);
			else oss.append(ih);
			for(int jw = 0 ; jw < this.w ; jw++) {
				oss.append(this.layout.get(ih*this.w+jw).toString());
			}
			oss.append(System.lineSeparator());
		}
		oss.append("   ");
		for(int jw = 0;jw < this.w ; jw++) {
			oss.append(" ");
			if(jw<10) oss.append("00"+jw);
			else if(jw<100) oss.append("0"+jw);
			else oss.append(jw);
			oss.append(" ");
		}
		oss.append(System.lineSeparator());
		return oss.toString();
	}

	/**
	 * Runs on each cell every step of the iteration process.
	 */
	public void iterate() {
		for(int i = 0;i<layout.size();i++) layout.get(i).spread();
		for(int i = 0;i<layout.size();i++) layout.get(i).commitStatus();
		for(int i = 0;i<layout.size();i++) layout.get(i).checkFlashpoint();
		for(int i = 0;i<layout.size();i++) layout.get(i).dissipateHeat();
	}

	/**
	 * 
	 * Gets the specified Cell
	 * 
	 * @param x x-axis coordinate of the cell
	 * @param y y-axis coordinate of the cell
	 * @return A Cell object reference
	 * @throws RoomException when the Cell does not exist.
	 */
	public Cell getCellXY(int x, int y) throws RoomException {
		if(x >= w || y >= h || x < 0 || y < 0) throw new RoomException("The requested cell does not exist.");
		return layout.get(w*y+x);
	}

	/**
	 * Getter for description.
	 * 
	 * @return description of the room.
	 */
	public String getDesc() {
		return this.desc;
	}

	/**
	 * 
	 */
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

	/**
	 * 
	 * @return JSON representation of object
	 */
	public JSONObject toJSON(String id) {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("desc", desc);
		json.put("w", w);
		json.put("h", h);
		
		for(int i = 0; i<layout.size();i++) {
			json.accumulate("cells", layout.get(i).toJSON());
		}
		
		return json;
	}
}
