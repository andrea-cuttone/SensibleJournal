package dk.dtu.imm.sensiblejournal2013.utilities;

public class TwoStringListObject {

	private String string1 = "";
	private String string2 = "";
	
	public TwoStringListObject(String string1, String string2){
		this.string1 = string1;
		this.string2 = string2;			
	}
	
	public TwoStringListObject(){		
	}

	public String getString1() {
		return string1;
	}

	public void setString1(String string1) {
		this.string1 = string1;
	}

	public String getString2() {
		return string2.substring(0, 2).replace(" ", "") + string2.substring(2);
	}

	public void setString2(String string2) {
		this.string2 = string2;
	}
}
