package rpgthermalsim.port.classes;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import rpgthermalsim.port.exceptions.RoomException;

public class BuildingTest {

	Building b;
	PipedInputStream in;
	PipedOutputStream pr;
	PrintStream ps;
	InputStream aux;
	File test;
	
	@Before
	public final void setUp() throws IOException {
		pr = new PipedOutputStream();
		ps = new PrintStream(pr);
		in = new PipedInputStream(pr);
		aux = System.in;
		System.setIn(in);
		b = new Building();
		test = new File("test.txt");
	}
	
	@After
	public final void tearDown() throws IOException {
		b = null;
		System.setIn(aux);
		ps.close();
		pr.close();
		in.close();
		test.delete();
		test = null;
		System.gc();
	}
	
	@Test
	public final void testBuilding() {
		ps.println("help");
		
		ps.println("build A 5 5 room A");
		ps.println("build B 1 5 hallway");
		ps.println("build C 5 5 room C");
		
		ps.println("link A 0 2 B 0 0");
		ps.println("link C 2 4 B 0 4");
		
		ps.println("block A 0 2");
		ps.println("block B 0 2");
		ps.println("unblock A 0 2");
		ps.println("put B 0 2 3");
		
		ps.println("save test.txt");
		ps.println("save");
		
		ps.println();
		ps.println("#");
		ps.println("refresh A");
		ps.println("refresh A B");
		ps.println("refresh A B C");
		ps.println("refresh");
		ps.println("iterate");
		ps.println("iterate 5");
		ps.println("build D 1 1");
		ps.println("set A 2 2 1 -1 1");
		ps.println("list");
		ps.println("ignite B 0 2");
		ps.println("deflagrate B 0 2 3");
		ps.println("iterate");
		ps.println("clear B 0 2");
		ps.println("save test2.txt");
		ps.println("load");
		ps.println("load test.txt");
		ps.println("reset");
		
		ps.println("exit");
		b.loop();
		
		b = null;
		System.gc();
		b = new Building("test.txt");
	}
	
	@Test
	public final void failsWhenIncorrectLine() throws IOException {
		FileWriter fw = new FileWriter("test.txt");
		fw.append("bild A 5 5");
		fw.flush();
		fw.close();
		b = null;
		System.gc();
		b = new Building("test.txt");
		ps.println("load test.txt");
		ps.println("exit");
		b.loop();
	}
	
	@Test
	public final void failsWhenInvalidCellReferenced() throws IOException {
		FileWriter fw = new FileWriter("test.txt");
		fw.append("build A 5 5"+System.lineSeparator()+"set A 10 4 0 0 0"+System.lineSeparator());
		fw.flush();
		fw.close();
		ps.println("load test.txt");
		ps.println("build A 5 5"+System.lineSeparator()+"set A 10 4 0 0 0"+System.lineSeparator());
		ps.println("exit");
		b.loop();
		b = null;
		System.setIn(aux);
		b = new Building("test.txt");
	}
	
	@Test
	public final void sameBuildingSameDigest() throws NoSuchAlgorithmException {
		String caseA, caseB;
		
		ps.println("build A 5 5");
		ps.println("exit");
		b.loop();
		caseA = b.digest();
		caseB = b.digest();
		assertTrue(caseA.compareTo(caseB)==0);
	}
	
	@Test
	public final void differentBuildingDifferentDigest() throws NoSuchAlgorithmException {
		String caseA, caseB, caseC;
		ps.println("build A 5 5");
		ps.println("exit");
		b.loop();
		caseA = b.digest();
		ps.println("put A 4 4 1");
		ps.println("exit");
		b.loop();
		caseB = b.digest();
		ps.println("link A 4 4 A 0 0");
		ps.println("exit");
		b.loop();
		caseC = b.digest();
		assertTrue(caseA.compareTo(caseB)!=0);
		assertTrue(caseB.compareTo(caseC)!=0);
	}
	
	
	@Test
	public final void temperatureMustConverge() throws NoSuchAlgorithmException, RoomException {
		ps.println("build a 10 10");
		ps.println("sink a 4 4 1000");
		ps.println("iterate 10000");
		ps.println("unsink a 4 4");
		ps.println("iterate 10000");
		ps.println("exit");
		b.loop();
		
		assertTrue((int) b.buildingLayout.get("a").getCellXY(4, 4).temp_counters > 0 &&
				(int) b.buildingLayout.get("a").getCellXY(0, 0).temp_counters == (int) b.buildingLayout.get("a").getCellXY(9, 9).temp_counters);
	}
	
	@Test
	public final void partialInsulationSlowsTemperatureTransfer() throws RoomException {
		ps.println("build a 1 3");
		ps.println("block a 0 1 0.1");
		ps.println("set a 0 0 0 0 1000");
		ps.println("iterate 1");
		ps.println("exit");
		b.loop();
		assertTrue(b.buildingLayout.get("a").getCellXY(0, 1).temp_counters < 100);
		
	}
	
	@Test
	public final void unblockingStopsInsulation() throws RoomException {
		ps.println("build a 1 3");
		ps.println("block a 0 1 0.1");
		ps.println("set a 0 0 0 0 1000");
		ps.println("iterate 1");
		ps.println("set a 0 0 0 0 1000");
		ps.println("set a 0 1 0 0 0");
		ps.println("unblock a 0 1");
		ps.println("iterate 1");
		ps.println("exit");
		b.loop();
		
		assertTrue(b.buildingLayout.get("a").getCellXY(0, 1).temp_counters > 100);
	}
	
	@Test
	public final void stoppingTheFireDestroysTheBlocking() throws RoomException {
		ps.println("build a 1 3");
		ps.println("put a 0 1 1 0.1");
		ps.println("ignite a 0 1");
		ps.println("iterate 11");
		ps.println("exit");
		b.loop();
		
		assertTrue(b.buildingLayout.get("a").getCellXY(0, 1).spreadable==1);
	}

}
