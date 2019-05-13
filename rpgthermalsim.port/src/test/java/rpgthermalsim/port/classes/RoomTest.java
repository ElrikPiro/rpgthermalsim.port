package rpgthermalsim.port.classes;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import rpgthermalsim.port.exceptions.RoomException;

public class RoomTest {

	Room r;
	
	@Before
	public final void setUp() {
		r = new Room((int) Math.random()*100+5,(int) Math.random()*100+5,"TEST ROOM");
	}
	
	@After
	public final void tearDown() {
		r = null;
		System.gc();
	}

	@Test
	public final void testToString() throws RoomException {
		for(int i = 1; i<=101;i++) {
			tearDown();
			r = new Room(i,i,"");
			assertNotNull(r.toString());
		}
	}

	@Test
	public final void testIterate() throws RoomException {
		for(int i = 0; i<r.h;i++) {
			for(int j = 0; j<r.w;j++) {
				r.getCellXY(j, i).setStatus(0, 1, 200);
			}
		}
		r.iterate();
		for(int i = 0; i<r.h;i++) {
			for(int j = 0; j<r.w;j++) {
				assertTrue(r.getCellXY(j, i).flame==1);
				assertTrue(r.getCellXY(j, i).ignition==-10);
				assertTrue(r.getCellXY(j, i).temp_counters>200);
				assertTrue(r.getCellXY(j, i).aux_counters==0);
			}
		}
	}

	@Test
	public final void testGetCellXY() throws RoomException {
		assertNotNull(r.getCellXY((int) Math.random()*r.w+1, (int) Math.random()*r.h+1));
	}
	

	@Ignore
	public final void testGetDesc() {
		fail("No need");
	}

}
