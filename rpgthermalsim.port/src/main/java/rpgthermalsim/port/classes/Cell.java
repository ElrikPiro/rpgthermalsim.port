package rpgthermalsim.port.classes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import rpgthermalsim.port.exceptions.CellException;

public class Cell implements Digestable{
	final char[] RESET = {0x1b,'[','3','9',';','4','9','m','\0'};
	final char[] FIRE = {0x1b,'[','4','1','m','\0'};
	final char[] HEAT = {0x1b,'[','1','0','0','m','\0'};
	final char[] INFLAMMABLE = {0x1b,'[','4','4','m','\0'};
	
	int flame;
	int ignition;
	int temp_counters;
	int spreadable; //boolean

	int aux_counters = 0;
	float insulation = 1.0f; //by default perfect heat conductivity is assumed

	HashSet<Cell> neightbours;

	public Cell() {
		neightbours = new HashSet<Cell>();
		flame = 0;
		ignition = 0;
		temp_counters = 0;
		spreadable = 1;
		aux_counters = 0;
	}
	
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

	public void setStatus(int intValue, int intValue2, int intValue3) {
		flame = intValue;
		ignition = intValue2;
		temp_counters = intValue3;
	}

	public void linkCells(Cell cellXY) {
		this.addNeightbour(cellXY);
		cellXY.addNeightbour(this);
	}

	public void ignite() {
		if(this.ignition > 0) {
			this.setStatus(1,this.ignition*-10,this.temp_counters);
		}else if(this.flame==1) {
			this.setStatus(1, this.ignition-1, this.temp_counters);
		}else {
			this.setStatus(1, -2, this.temp_counters);
		}
		
	}

	public Set<Cell> getNeightbourhood() {
		return neightbours;
	}

	public boolean isSpreadable() {
		return this.spreadable==1;
	}

	public void setUnreachable(float istn) {
		this.spreadable = 0;
		this.insulation = istn;
	}

	public void setReachable() {
		this.spreadable = 1;
		this.insulation = 1.0f;
	}

	public void spread() {
		//if(!isSpreadable()) return;
		float accumulate = temp_counters;
		int avg = 0;
		float flanders = 1.0f;
		Iterator<Cell> it = this.neightbours.iterator();
		while(it.hasNext()) {
			Cell c = it.next();
			accumulate += c.temp_counters * c.insulation * this.insulation;
			flanders+=c.insulation*this.insulation;
		}
		
		avg = (int) (accumulate/flanders);
		addCounters(avg-temp_counters);
	}

	private void addCounters(int i) {
		this.aux_counters += i;
	}

	public void commitStatus() {
		//if(aux_counters==0 && this.temp_counters>0) aux_counters--;
		this.temp_counters += aux_counters;
		this.aux_counters = 0;
	}

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
		oss.append("[");
		if(this.flame==1) {
			oss.append(FIRE);
			oss.append(" * ");
		}else if(!this.isSpreadable()) {
			oss.append("###");
		}else if(this.temp_counters>20) {
			oss.append(HEAT);
			if(this.temp_counters < 50) oss.append("   ");
			else if(this.temp_counters < 100) oss.append(" "+this.temp_counters);
			else if(this.temp_counters < 1000) oss.append(this.temp_counters);
			else if(this.temp_counters < 10000) oss.append(" "+this.temp_counters/1000+"k");
			else if(this.temp_counters < 100000) oss.append(this.temp_counters/1000+"k");
			else if(this.temp_counters < 1000000) oss.append("."+this.temp_counters/100000+"M");
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

	public void addNeightbour(Cell cell) {
		if(cell != this) this.neightbours.add(cell);
	}
	
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
