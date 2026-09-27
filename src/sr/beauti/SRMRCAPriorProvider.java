package sr.beauti;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.alignment.TaxonSet;
import beast.base.evolution.tree.Tree;
import beast.base.inference.Distribution;
import beast.base.inference.Logger;
import beast.base.inference.State;
import beast.base.inference.StateNode;
import beast.base.inference.distribution.OneOnX;
import beastfx.app.beauti.PriorListInputEditor;
import beastfx.app.beauti.PriorProvider;
import beastfx.app.inputeditor.BEASTObjectPanel;
import beastfx.app.inputeditor.BeautiDoc;
import beastfx.app.util.Alert;

import beast.base.core.BEASTInterface;
import beast.base.core.BEASTObject;

import sr.math.distributions.SRMRCAPrior;
import sr.evolution.sranges.StratigraphicRange;
import sr.evolution.tree.SRTree;
import sr.evolution.tree.RandomSRangeTree;

public class SRMRCAPriorProvider implements PriorProvider {

	@Override
	public List<Distribution> createDistribution(BeautiDoc doc) {
    	SRMRCAPrior prior = new SRMRCAPrior();
        try {
            prior.treeInput.setValue(getTree(doc), prior);

            ArrayList<StratigraphicRange> stratigraphicRanges = deepcopy(getStratigraphicRanges(prior));

            StratigraphicRangeDialog dlg = new StratigraphicRangeDialog(stratigraphicRanges, getTaxa(prior, doc), doc);
            if (!dlg.showDialog()) {
                return null;
            }

            setStratigraphicRanges(stratigraphicRanges, prior);

            //FIXME
            setStratigraphicRanges_RANDOMTREE(stratigraphicRanges, doc);

            /*
            // this sets up the type
            prior.distInput.setValue(new OneOnX(), prior);
            // this removes the parametric distribution
            prior.distInput.setValue(null, prior);

            Logger logger = (Logger) doc.pluginmap.get("tracelog");
            logger.loggersInput.setValue(prior, logger);
            */
        } catch (Exception e) {
            //FIXME
            System.out.println("mydeb444 => "+e);
        }

        List<Distribution> selectedPlugins = new ArrayList<>();
        selectedPlugins.add(prior);
        PriorListInputEditor.addCollapsedID(prior.getID());
        return selectedPlugins;
    }
	
	@Override
	public String getDescription() {
		return "Stratigraphic Ranges MRCA prior";
	}

    private Set<Taxon> getTaxa(SRMRCAPrior prior, BeautiDoc doc) {
        Set<Taxon> tset = new HashSet<>();
        Tree tree = prior.treeInput.get();
        String [] taxa = null;
        if (tree.m_taxonset.get() != null) {
        	try {
            	TaxonSet set = tree.m_taxonset.get();
        		set.initAndValidate();
            	taxa = set.asStringList().toArray(new String[0]);
        	} catch (Exception e) {
            	taxa = prior.treeInput.get().getTaxaNames();
			}
        } else {
        	taxa = prior.treeInput.get().getTaxaNames();
        }
        
        for (String taxon : taxa) {
            tset.add(doc.getTaxon(taxon));
        }
        return tset;
    }

    private ArrayList<StratigraphicRange> getStratigraphicRanges(SRMRCAPrior prior) {
        SRTree srtree = (SRTree) prior.treeInput.get();
        ArrayList<StratigraphicRange> stratigraphicRanges = (ArrayList<StratigraphicRange>) srtree.stratigraphicRangeInput.get();

        //debug
        /*
        for (StratigraphicRange s : stratigraphicRanges) {
            System.out.println("mydeb101 => "+s);
            System.out.println("mydeb102 => "+s.getID());
            System.out.println("mydeb104 => "+s.taxonFirstOccurrenceInput); 
            System.out.println("mydeb105 => "+s.taxonFirstOccurrenceInput.get());

            Taxon tf = s.taxonFirstOccurrenceInput.get();
            Taxon tl = s.taxonLastOccurrenceInput.get();

            System.out.println("mydeb106 => "+tf);
            System.out.println("mydeb107 => "+tl);
        }
        */

        return stratigraphicRanges;
    }

    SRTree getTree(BeautiDoc doc) {

        //way1
        /*
        List<Tree> trees = new ArrayList<>();
        doc.scrubAll(true, false);
        State state = (State) doc.pluginmap.get("state");
        for (StateNode node : state.stateNodeInput.get()) {
            if (node instanceof Tree) {
                trees.add((Tree) node);
            }
        }
        assert trees.size() == 1;
        return trees.get(0);
        */

        //way2
        SRTree srtree = null;
        for (BEASTInterface bi : doc.pluginmap.values()) {
            BEASTObject beastObject = (BEASTObject) bi;


            //debug
            /*
            String name = beastObject.getClass().getName();
            String ID = beastObject.getID();
            if (name.toUpperCase().contains("TREE")) {
                System.out.println("mydeb503 - name="+name+", ID="+ID);
            }
            */

            if (beastObject instanceof sr.evolution.tree.SRTree) {
                String id = beastObject.getID();
                if (id.equals("Tree.t:penguins_both")) { //FIXME: specific name
                    srtree = (SRTree) beastObject;
                }
            }
        }
        return srtree;
    }

    private void setStratigraphicRanges(ArrayList<StratigraphicRange> stratigraphicRanges, SRMRCAPrior prior) {
        SRTree srtree = (SRTree) prior.treeInput.get();

        srtree.stratigraphicRangeInput.get().clear();

        for (StratigraphicRange sr : stratigraphicRanges) {
            srtree.stratigraphicRangeInput.get().add(sr);
        }
    }

    private void setStratigraphicRanges_RANDOMTREE(ArrayList<StratigraphicRange> stratigraphicRanges, BeautiDoc doc) {
        RandomSRangeTree randomtree = getRandomTree(doc);

        randomtree.stratigraphicRangeInput.get().clear();

        for (StratigraphicRange sr : stratigraphicRanges) {
            randomtree.stratigraphicRangeInput.get().add(sr);
        }
    }

    RandomSRangeTree getRandomTree(BeautiDoc doc) {
        RandomSRangeTree rtree = null;
        for (BEASTInterface bi : doc.pluginmap.values()) {
            BEASTObject beastObject = (BEASTObject) bi;

            if (beastObject instanceof sr.evolution.tree.RandomSRangeTree) {
                String id = beastObject.getID();
                if (id.equals("RandomTree.t:penguins_both")) { //FIXME: specific name
                    rtree = (RandomSRangeTree) beastObject;
                }
            }
        }
        return rtree;
    }

    ArrayList<StratigraphicRange> deepcopy(ArrayList<StratigraphicRange> SRs) {

        ArrayList<StratigraphicRange> stratigraphicRanges = new ArrayList<StratigraphicRange>();

        for (StratigraphicRange sr : SRs) {

            //note: null pointer exception when using this
            //Taxon tf = new Taxon(sr.taxonFirstOccurrenceInput.get().getID());
            //Taxon tl = new Taxon(sr.taxonLastOccurrenceInput.get().getID());

            Taxon tf = new Taxon(sr.getFirstOccurrenceID());
            Taxon tl = new Taxon(sr.getLastOccurrenceID());

            StratigraphicRange srCpy = new StratigraphicRange(sr.getID(), tf, tl);
            srCpy.initAndValidate();

            stratigraphicRanges.add(srCpy);
        }

        return stratigraphicRanges;
    }
}
