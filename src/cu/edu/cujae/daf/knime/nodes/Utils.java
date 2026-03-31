package cu.edu.cujae.daf.knime.nodes;

import java.util.Random;

import org.knime.core.data.def.BooleanCell;

public class Utils {

	public static final Random random = new Random();
	
	private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	public static BooleanCell getRandomBooleanCell() {

		BooleanCell answer = null;
		
		var value = random.nextBoolean();
		if(value == true)
			answer = BooleanCell.TRUE;
		else
			answer = BooleanCell.FALSE;
		
		return answer;
	}
	
	public static String randomString(int maxLength) {
	    int length = random.nextInt(maxLength) + 1; // Ensure at least 1 char
	    StringBuilder builder = new StringBuilder(length);
	    for (int count = 0; count < length; count++) {
	        int index = random.nextInt(ALPHANUMERIC.length());
	        builder.append(ALPHANUMERIC.charAt(index));
	    }
	    return builder.toString();
	}
}
