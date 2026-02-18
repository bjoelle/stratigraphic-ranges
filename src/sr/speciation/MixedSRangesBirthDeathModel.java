package sr.speciation;

import beast.base.core.Input;
import beast.base.evolution.tree.Node;
import beast.base.inference.parameter.RealParameter;
import sr.evolution.tree.SRTree;

public class MixedSRangesBirthDeathModel extends SRangesBirthDeathModel {
	public Input<RealParameter> anagenesisRateInput =
			new Input<RealParameter>("anagenesisRate", "Anagenesis rate, default 0", new RealParameter("0.0"));
	public Input<RealParameter> symProportionInput =
			new Input<RealParameter>("symProportion", "Proportion of symmetric birth, default 0.0", new RealParameter("0.0"));

	private RealParameter anagenesisRate, symProportion;

	@Override
	public void initAndValidate() {
		super.initAndValidate();

		anagenesisRate = anagenesisRateInput.get();
		if(anagenesisRate.getValue() < 0) throw new IllegalArgumentException("Anagenesis rate must be positive");
		anagenesisRate.setLower(0.0);

		symProportion = symProportionInput.get();
		if(symProportion.getValue() < 0.0 || symProportion.getValue() > 1.0) 
			throw new IllegalArgumentException("Asymmetric proportion must be between 0 and 1");
		symProportion.setBounds(0.0, 1.0);
	}

	@Override
	protected double log_q_tilde(double t) {
		double logq = (1 - symProportion.getValue()) * super.log_q_tilde(t); 
		logq -= t * (anagenesisRate.getValue() + symProportion.getValue() * (lambda + mu + psi));
		return logq;
	}
	
	@Override
	protected double birthNodeContribution(Node node) {
		if(((SRTree) combinedTree.getTree()).isAsymmetric(node.getNr())) return Math.log(lambda) + Math.log(1 - symProportion.getValue());
		return Math.log(lambda) + Math.log(symProportion.getValue());
	}
	
}
