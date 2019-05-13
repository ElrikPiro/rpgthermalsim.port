/**
 * 
 */
package rpgthermalsim.port.classes;

import rpgthermalsim.port.exceptions.CellException;

/**
 * 
 * This cell has a fixed temperature and can be useful to represent the effect of a
 * constant source of heat or, for example, a window pointing outside.
 * 
 * @author David Baselga
 * @since 1.1
 */
public class FixedTempCell extends Cell {

	int fixedTemp;
	
	/**
	 * 
	 */
	public FixedTempCell(int fixedTemp) {
		super();
		this.fixedTemp = fixedTemp;
		this.temp_counters = fixedTemp;
	}
	
	/*@Override
	public void setStatus(int intValue, int intValue2, int intValue3) {
		temp_counters = intValue3;
		fixedTemp = intValue3;
	}*/
	
	@Override
	public void spread() {
		return;
	}
	
	@Override
	public void commitStatus() {
		temp_counters = fixedTemp;
		return;
	}
	
	public void checkFlashpoint() {
		return;
	}

}
