package cu.edu.cujae.daf.knime.nodes;

import java.util.LinkedHashMap;

import cu.edu.cujae.daf.context.dataset.AttributeType;

public class CandidateAttributeData {
	public String name;
	public AttributeType vtype;
	public boolean skip;
	public boolean target;
	public int position;
	public double minimum;
	public double maximum;
	public LinkedHashMap<String, Integer> values;
	public String censor;

	public CandidateAttributeData(boolean skip, boolean target, int position) {
		this.skip = skip;
		this.target = target;
		this.position = position;
	}
}