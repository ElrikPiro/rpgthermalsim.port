package rpgthermalsim.port.classes;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BuildingTest {

	Building b;
	PipedInputStream in;
	PipedOutputStream pr;
	PrintStream ps;
	InputStream aux;
	
	
	@Before
	public final void setUp() throws IOException {
		pr = new PipedOutputStream();
		ps = new PrintStream(pr);
		in = new PipedInputStream(pr);
		aux = System.in;
		System.setIn(in);
		b = new Building();
	}
	
	@After
	public final void tearDown() throws IOException {
		b = null;
		System.setIn(aux);
		ps.close();
		pr.close();
		in.close();
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

}
