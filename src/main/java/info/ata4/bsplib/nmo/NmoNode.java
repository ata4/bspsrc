package info.ata4.bsplib.nmo;

import info.ata4.io.DataReader;

import java.io.IOException;

public class NmoNode {

	public int id;
	public String name;

	public NmoNode(DataReader reader) throws IOException {
		id = reader.readInt();
		name = reader.readStringNull();
	}
}
