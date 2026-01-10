package cu.edu.cujae.daf.knime.nodes;

import java.util.LinkedHashMap;
import cu.edu.cujae.daf.context.dataset.AttributeType;

class CandidateAttribute {
	
	final public String name;
	final AttributeType vtype;
	public boolean skip = false;
	public boolean target = false;
	public int position = -1;
	final public double minimum;
    final double maximum;
    final LinkedHashMap<String, Integer> values;
    final String censor;

	public CandidateAttribute(
			String name,
			AttributeType vtype,
            boolean skip,
            boolean target,
            int position,
            double minimum,
            double maximum,
            LinkedHashMap<String, Integer> values,
            String censor) {
		
			this.name = name;
			this.vtype = vtype;
			this.skip = skip;
			this.target = target;
			this.position = position;
			this.minimum = minimum;
			this.maximum = maximum;
			this.values = values;
			this.censor = censor;
		
	}
	
}