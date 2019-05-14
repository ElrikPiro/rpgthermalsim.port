package rpgthermalsim.port.classes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import rpgthermalsim.port.exceptions.CellException;


/**
 * 
 * Unitary agent that manages it's own status based on the status of it's neightbours.
 * 
 * @author David Baselga
 * @since 0.1
 */
public class Cell implements Digestable{
	final char[] RESET = {0x1b,'[','3','9',';','4','9','m','\0'};
	final char[] FIRE = {0x1b,'[','4','1','m','\0'};
	final char[] HEAT = {0x1b,'[','1','0','0','m','\0'};
	final char[] INFLAMMABLE = {0x1b,'[','4','4','m','\0'};
	final char[] MAPPER1 = {0x1b,'[','4','8',';','5',';'};
	final char[] MAPPER2 = {'m','\0'};
	final int[] LERP = { 16, 17, 18, 19, 20, 21, 27, 26, 25, 24, 23, 22, 58, 94, 130, 166, 202, 203,204,205,206,207,201,200,199,198,197,196,160,124,88,52};
	
	int flame;
	int ignition;
	float temp_counters;
	int spreadable; //boolean

	float aux_counters = 0;
	float insulation = 1.0f; //by default perfect heat conductivity is assumed

	HashSet<Cell> neightbours;

	/**
	 * Builds a default empty cell
	 * 
	 * @author David Baselga
	 * @since 1.1
	 */
	public Cell() {
		neightbours = new HashSet<Cell>();
		flame = 0;
		ignition = 0;
		temp_counters = 0;
		spreadable = 1;
		aux_counters = 0;
	}
	
	/**
	 * Builds a cell from a string
	 * 
	 * @param string 4 comma separated values, representing flame, ignition, temp_counters and spreadable behavior.
	 * @deprecated
	 * @author David Baselga
	 * @since 0.1
	 */
	public Cell(String string) throws CellException {
		neightbours = new HashSet<Cell>();
		String[] aux = string.split(",");
		if(aux.length != 4) throw new CellException("Cell cannot be created, wrong parameter '"+string+"'.");
		flame = Integer.parseInt(aux[0]);
		ignition = Integer.parseInt(aux[1]);
		temp_counters = Integer.parseInt(aux[2]);
		spreadable = Integer.parseInt(aux[3]);
		aux_counters = 0;
	}

	/**
	 * Overrides the cell status with new values.
	 * 
	 * @param intValue defines if the cell is ignited
	 * @param intValue2 defines ignition temperature and iterations left until set off
	 * @param tc defines cell temperature
	 * @author David Baselga
	 * @since 0.1
	 */
	public void setStatus(int intValue, int intValue2, float tc) {
		flame = intValue;
		ignition = intValue2;
		temp_counters = tc;
	}

	/**
	 * Links the current cell with the cell passes as a parameter.
	 * 
	 * @param cellXY cell to link this object to.
	 * @author David Baselga
	 * @since 0.1
	 */
	public void linkCells(Cell cellXY) {
		this.addNeightbour(cellXY);
		cellXY.addNeightbour(this);
	}

	/**
	 * Sets a cell on fire, if the cell wasn't ignitable it will burn for 2 iterations, if the cell was
	 * already ignited, it will expand 1 iteration it's burning period. 
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	public void ignite() {
		if(this.ignition > 0) {
			this.setStatus(1,this.ignition*-10,this.temp_counters);
		}else if(this.flame==1) {
			this.setStatus(1, this.ignition-1, this.temp_counters);
		}else {
			this.setStatus(1, -2, this.temp_counters);
		}
		
	}

	/**
	 * Getter for the neightbours list 
	 * 
	 * @return Set containing all cells linked to this object
	 * @author David Baselga
	 * @since 0.1
	 */
	public Set<Cell> getNeightbourhood() {
		return neightbours;
	}

	/**
	 * Checks if a cell is not blocked 
	 * 
	 * @return boolean indicating if the Cell is not blocked
	 * @author David Baselga
	 * @since 0.1
	 */
	public boolean isSpreadable() {
		return this.spreadable==1;
	}

	/**
	 * Sets a cell as unreachable/insulated 
	 * 
	 * @param istn float number representing the Cell's insulation (0.0, perfectly insulated, 1.0 totally conductive)
	 * @author David Baselga
	 * @since 1.1
	 */
	public void setUnreachable(float istn) {
		this.spreadable = 0;
		this.insulation = istn;
	}

	/**
	 * Sets a cell as reachable/uninsulated 
	 * 
	 * @author David Baselga
	 * @since 1.1
	 */
	public void setReachable() {
		this.spreadable = 1;
		this.insulation = 1.0f;
	}

	/**
	 * Calculates Cell's temperature regarding it's neightbouring Cell's temperature. 
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	public void spread() {
		//if(!isSpreadable()) return;
		float accumulate = temp_counters;
		float avg = 0;
		float flanders = 1.0f;
		Iterator<Cell> it = this.neightbours.iterator();
		while(it.hasNext()) {
			Cell c = it.next();
			accumulate += c.temp_counters * c.insulation * this.insulation;
			flanders+=c.insulation*this.insulation;
		}
		
		avg = (accumulate/flanders);
		addCounters(avg-temp_counters);
	}

	/**
	 * Modifies the auxiliary counters
	 * 
	 * @param i value to add to the auxiliary counters.
	 * @author David Baselga
	 * @since 0.1
	 */
	private void addCounters(float i) {
		this.aux_counters += i;
	}

	/**
	 * Adds the auxiliary counters to the Cell temperature amd resets to 0. 
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	public void commitStatus() {
		//if(aux_counters==0 && this.temp_counters>0) aux_counters--;
		this.temp_counters += aux_counters;
		this.aux_counters = 0;
	}

	/**
	 * Checks if the cell has enough temperature to lit up and if ignited, increases the
	 * Cell temperature or sets it off if the iteration counter reaches zero.
	 * 
	 * @author David Baselga
	 * @since 0.1
	 */
	public void checkFlashpoint() {
		if((this.temp_counters > this.ignition*100) && (this.ignition > 0)) {
			this.flame = 1;
			this.ignition *= -10;
		}
		else if(this.ignition <= -1){
			if(++this.ignition==0) {
				this.flame = 0;
				this.setReachable();
			}
		}

		if(this.flame==1) this.temp_counters += 200;
	}

	/**
	 * Does nothing 
	 * 
	 * @author David Baselga
	 * @since 0.1
	 * @deprecated
	 */
	public void dissipateHeat() {
		/*
		int flanders = 0;
		int count = 0;
		
		Iterator<Cell> it = this.neightbours.iterator();
		while(it.hasNext()) {
			Cell c = it.next();
			if(c.isSpreadable()) flanders++;
		}
		
		if(this.flame <= 0 && this.temp_counters >= 10) this.temp_counters -= this.temp_counters/10;
		
		it = this.neightbours.iterator();
		while(it.hasNext()) {
			Cell c = it.next();
			if(this.temp_counters<=0) break;
			if(c.isSpreadable() && c.temp_counters == 0) {
				++count;
				if(count>=flanders/2) {
					this.temp_counters--;
					break;
				}
			}
		}
		*/
	}
	
	public String toString() {
		StringBuilder oss = new StringBuilder();
		int lerp = 0;
		oss.append("[");
		oss.append(MAPPER1);
		if(this.temp_counters<=-273) lerp = 0;
		else if(this.temp_counters > 500) lerp = LERP.length-1;
		else {
			float tempmap = (float) (this.temp_counters+273)/773;
			lerp = (int) Math.floor(tempmap*(LERP.length-1));
		}
		oss.append(LERP[lerp]);
		oss.append(MAPPER2);
		if(this.flame==1) {
			oss.append(FIRE);
			oss.append(" * ");
		}else if(!this.isSpreadable()) {
			oss.append("###");
		}else if(this.temp_counters>20) {
			if(this.temp_counters < 50) oss.append("   ");
			else if(this.temp_counters < 100) oss.append(" "+(int) this.temp_counters);
			else if(this.temp_counters < 1000) oss.append((int) this.temp_counters);
			else if(this.temp_counters < 10000) oss.append(" "+(int) this.temp_counters/1000+"k");
			else if(this.temp_counters < 100000) oss.append((int) this.temp_counters/1000+"k");
			else if(this.temp_counters < 1000000) oss.append("."+(int) this.temp_counters/100000+"M");
			else oss.append("***");
		}else if(this.ignition>0) {
			oss.append(INFLAMMABLE);
			if(this.ignition<10) oss.append(" "+this.ignition+" ");
			else if(this.ignition<100) oss.append(" "+this.ignition);
			else if(this.ignition<1000) oss.append(this.ignition);
			else oss.append("^^^");
		}else {
			oss.append("   ");
		}
		oss.append(RESET);
		oss.append("]");
		return oss.toString();
	}

	/**
	 * Adds an unidirectional neighbour.
	 * 
	 * @param cell Cell to link this object to.
	 * @author David Baselga
	 * @since 0.1
	 */
	public void addNeightbour(Cell cell) {
		if(cell != this) this.neightbours.add(cell);
	}
	
	/**
	 * @author David Baselga
	 * @since 1.1
	 */
	@Override
	public String digest() throws NoSuchAlgorithmException {
		StringBuilder oss = new StringBuilder();
		oss.append(flame);
		oss.append(ignition);
		oss.append(temp_counters);
		oss.append(spreadable);
		oss.append(aux_counters);
		oss.append(insulation);
		Iterator<Cell> it = this.neightbours.iterator();
		while(it.hasNext()) {
			oss.append(it.next().toString());
		}
		oss.append(neightbours.size());
		return digest(oss.toString());
	}

}
