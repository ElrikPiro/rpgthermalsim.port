package rpgthermalsim.port.classes;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CellTest {

	Cell underTest;
	
	@Before
	public final void setUp() throws CellException{
		underTest = new Cell("0,0,0,1");
	}
	
	@After
	public final void tearDown() {
		underTest = null;
		System.gc();
	}
	
	@Test
	public final void testSetStatus() {
		underTest.setStatus(1, -5, 100);
		assertTrue(underTest.flame == 1);
		assertTrue(underTest.ignition == -5);
		assertTrue(underTest.temp_counters == 100);
	}

	@Test
	public final void testLinkCells() throws CellException {
		Cell b = new Cell("0,0,0,1");
		underTest.linkCells(b);
		assertTrue(underTest.neightbours.contains(b));
		assertTrue(b.neightbours.contains(underTest));
	}

	@Test
	public final void testIgnite() {
		for(int i = 0; i<10; i++) whenThereIsIgnitionPoint((int) Math.random()*Integer.MAX_VALUE);
		whenAlreadyIgnited();
		whenCellIsFlat();
	}
	
	private final void whenThereIsIgnitionPoint(int ign) {
		underTest.setStatus(0, ign, 0);
		underTest.ignite();
		assertTrue(underTest.flame==1);
		assertTrue(underTest.ignition<0);
	}
	
	private final void whenAlreadyIgnited() {
		int aux = underTest.ignition;
		underTest.ignite();
		assertTrue(underTest.ignition==(aux-1));
	}
	
	private final void whenCellIsFlat() {
		underTest.setStatus(0, 0, 0);
		underTest.ignite();
		assertTrue(underTest.flame==1 && underTest.ignition == -2);
	}

	@Test
	public final void testGetNeightbourhood() throws CellException {
		testLinkCells();
		assertFalse(underTest.getNeightbourhood().isEmpty());
	}

	@Test
	public final void testIsSpreadable() {
		assertTrue(underTest.isSpreadable() == true && underTest.spreadable == 1 || underTest.isSpreadable() == false && underTest.spreadable == 0);
	}

	@Test
	public final void testSetUnreachable() {
		underTest.setUnreachable();
		testIsSpreadable();
		assertFalse(underTest.isSpreadable());
	}

	@Test
	public final void testSetReachable() {
		testSetUnreachable();
		underTest.setReachable();
		testIsSpreadable();
		assertTrue(underTest.isSpreadable());
	}

	@Test
	public final void testSpread() throws CellException {
		underTest.setStatus(0, 0, 1000);
		
		testSetUnreachable();
		whenNotSpreadable();
		testSetReachable();
		
		whenNoNeighbours();
		
		for(int i = 1;i <= 10;i++) {
			underTest.neightbours.clear();
			System.gc();
			for(int j = 0;j<10;j++) underTest.linkCells(new Cell("0,0,0,0"));
			whenNumNeighbours(i);
		}
	}

	private final void whenNotSpreadable() {
		underTest.spread();
		assertTrue(underTest.aux_counters==0);
	}
	
	private final void whenNoNeighbours() {
		underTest.spread();
		assertTrue(underTest.aux_counters==0);
	}
	
	private final void whenNumNeighbours(int numneighbours) {
		assert(underTest.neightbours.size()==10);
		Iterator<Cell> it = underTest.neightbours.iterator();
		Cell c;
		for(int i = 0; i<numneighbours;i++) {
			c = it.next();
			c.setReachable();
		}
		underTest.spread();
		assertTrue(underTest.aux_counters == underTest.temp_counters/(numneighbours+1));
	}
	
	@Test
	public final void testCommitStatus() throws CellException {
		whenTemperatureReachEquilibrium();
		whenTemperatureUnstable();
	}

	private final void whenTemperatureReachEquilibrium() {
		underTest.setStatus(0, 0, 1001);
		underTest.commitStatus();
		assertTrue(underTest.temp_counters==1000);
	}
	
	private final void whenTemperatureUnstable() throws CellException {
		underTest.linkCells(new Cell("0,0,0,1"));
		underTest.spread();
		underTest.commitStatus();
		assertTrue(underTest.temp_counters==500);
	}
	
	@Test
	public final void testCheckFlashpoint() {
		whenCellIsIgnitable();
		whenFireDies();
	}

	private final void whenFireDies() {
		underTest.setStatus(1, -3, 200);
		underTest.checkFlashpoint();
		assertTrue(underTest.flame==1);
		underTest.checkFlashpoint();
		assertTrue(underTest.flame==1);
		underTest.checkFlashpoint();
		assertTrue(underTest.flame==0);
	}

	private final void whenCellIsIgnitable() {
		underTest.setStatus(0, 1, 200);
		underTest.checkFlashpoint();
		assertTrue(underTest.flame==1
				&& underTest.ignition==-10
				&& underTest.temp_counters == 950
				);
	}

	@Ignore
	public final void testDissipateHeat() {
		return; //TODO: Esta opción dejará de ser usada en pos de un sistema de disipación posterior
	}

	/*
	 * 	public String toString() {
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
	 */
	
	@Test
	public final void testToString() {
		underTest.setStatus(1, -1, 0);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[");
		stringBuilder.append(underTest.FIRE);
		stringBuilder.append(" * ");
		stringBuilder.append(underTest.RESET);
		stringBuilder.append("]");
		assertTrue(underTest.toString().equals(stringBuilder.toString()));
		
		underTest.setStatus(0, 0, 0);
		underTest.setUnreachable();
		stringBuilder = new StringBuilder();
		stringBuilder.append("[###");
		stringBuilder.append(underTest.RESET);
		stringBuilder.append("]");
		assertTrue(underTest.toString().equals(stringBuilder.toString()));
		
		underTest.setReachable();
		for(int i = 32;i<10000000;i*=2) {
			underTest.setStatus(0, 0, i);
			assertTrue(i<50 && underTest.toString().charAt(10)==' ' ||
					i>50 && i<1000 && underTest.toString().charAt(10)!=' ' ||
					i>999 && i<100000 && underTest.toString().charAt(10)=='k' ||
					i>99999 && i<1000000 && underTest.toString().charAt(10)=='M' ||
					i>999999 && underTest.toString().charAt(10)=='*');
		}
		for(int i = 2;i<10000;i*=2) {
			underTest.setStatus(0, i, 0);
			assertTrue(i<10 && underTest.toString().charAt(9)==' ' ||
					i>9 && i<1000 && underTest.toString().charAt(9)!=' ' ||
					i>999 && underTest.toString().charAt(9)=='^');
		}
		
		underTest.setStatus(0, 0, 0);
		stringBuilder = new StringBuilder();
		stringBuilder.append("[   ");
		stringBuilder.append(underTest.RESET);
		stringBuilder.append("]");
		assertTrue(underTest.toString().equals(stringBuilder.toString()));
		
	}

	@Ignore
	public final void testAddNeightbour() {
		return;
	}

}
