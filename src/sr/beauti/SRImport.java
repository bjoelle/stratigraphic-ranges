package sr.beauti;

import javafx.stage.FileChooser;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import beast.base.evolution.alignment.Taxon;
import sr.evolution.sranges.StratigraphicRange;

import sr.util.Tools;

public class SRImport {

    public enum SRImportFileFormat {
      ONECOL, MULTICOL
    };

    List<StratigraphicRange> importFile () throws Exception {
        String file_ = null;

        try {
            file_ = selectFile();
        } catch (Exception e) {
            throw e;
        }

        List<StratigraphicRange> sranges = null;

        SRImportFileFormat fmt = determineSRImportFileFormat(file_);

        if(fmt==SRImportFileFormat.ONECOL) {
            sranges = importSRFromOneColumnFile(file_);
        } else if (fmt==SRImportFileFormat.MULTICOL) {
            sranges = importSRFromMultiColumnFile(file_);
        }

        for (StratigraphicRange sr:sranges) {
            if (!Tools.checkRangeConsistency(sr.getFirstOccurrenceID(), sr.getLastOccurrenceID())) {
                throw new Exception("Age inconsistency detected. First occurrence is older than last occurrence ("+sr.getID()+")");
            }
        }

        return sranges;
    }

    List<StratigraphicRange> importSRFromOneColumnFile(String file_) throws Exception {
        HashMap<String, ArrayList> firstSRanges = new HashMap<String, ArrayList>();
        HashMap<String, ArrayList> lastSRanges = new HashMap<String, ArrayList>();
        List<StratigraphicRange> SRs = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file_));

        try {
            String line = br.readLine();

            while (line != null) {

                //line format
                //
                // <range_id>_<position>
                // Spheniscus_muizoni_9.1_first

                String s = line;

                //first split to extract position (first or last)
                int i = s.lastIndexOf("_");
                String[] a = {s.substring(0, i), s.substring(i+1)};
                String position = a[1];

                s = a[0];

                //second split to extract "taxonName" and "age"
                i = s.lastIndexOf("_");
                String taxonName = s.substring(0, i);
                String age = s.substring(i+1);

                ArrayList al;
                if(position.equals("first")) {
                    if(firstSRanges.containsKey(taxonName)) {
                        al = (ArrayList) firstSRanges.get(taxonName);
                        al.add(age);
                    } else {
                        al = new ArrayList();
                        al.add(age);
                        firstSRanges.put(taxonName, al);
                    }
                } else if(position.equals("last")) {
                    if(lastSRanges.containsKey(taxonName)) {
                        al = (ArrayList) lastSRanges.get(taxonName);
                        al.add(age);
                    } else {
                        al = new ArrayList();
                        al.add(age);
                        lastSRanges.put(taxonName, al);
                    }
                }

                line = br.readLine();
            }
        } finally {
            br.close();
        }

        SRs = buildSRFromOneColumnFormat(firstSRanges,lastSRanges);

        //debug
        //System.out.println("mydeb101 => "+firstSRanges.toString());
        //System.out.println("mydeb101 => "+lastSRanges.toString());

        return SRs;
    }

    List<StratigraphicRange> buildSRFromOneColumnFormat(HashMap<String, ArrayList> firstSRanges, HashMap<String, ArrayList> lastSRanges) throws Exception {
        List<StratigraphicRange> SRs = new ArrayList<>();

        for (Map.Entry<String, ArrayList> entry : firstSRanges.entrySet()) {
            String taxon = entry.getKey();
            ArrayList ageListFirst = entry.getValue();

            if (lastSRanges.containsKey(taxon)) {

                ArrayList ageListLast = lastSRanges.get(taxon);

                if( (ageListFirst.size()<1) || (ageListLast.size()<1) ) {
                    throw new Exception("Incorrect stratigraphic range ("+taxon+")");
                }

                String minAge = minAge(ageListFirst);
                String maxAge = maxAge(ageListLast);

                StratigraphicRange sr = new StratigraphicRange(taxon+"_range", new Taxon(taxon+"_"+minAge), new Taxon(taxon+"_"+maxAge));
                SRs.add(sr);
            }
        }

        return SRs;
    }

    String maxAge(ArrayList ageList) {
        Collections.sort(ageList);
        String maxAge = (String) ageList.get(ageList.size() - 1);
        return maxAge;
    }

    String minAge(ArrayList ageList) {
        Collections.sort(ageList);
        String minAge = (String) ageList.get(0);
        return minAge;
    }

    List<StratigraphicRange> importSRFromMultiColumnFile(String file_) throws Exception {
        HashMap firstSRanges = new HashMap();
        HashMap lastSRanges = new HashMap();
        List<StratigraphicRange> SRs = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file_));

        try {
            String line = br.readLine();

            while (line != null) {
                String[] strs = line.split("\\s+");
                assert strs.length==3;

                //line format
                //
                // <range_name>             <first>                <last>
                // Spheniscus_muizoni_range Spheniscus_muizoni_9.1 Spheniscus_muizoni_9.1

                SRs.add(new StratigraphicRange(strs[0],new Taxon(strs[1]),new Taxon(strs[2])));

                line = br.readLine();
            }
        } finally {
            br.close();
        }

        //debug
        //System.out.println("mydeb102 => "+SRs.toString());

        return SRs;
    }

    String selectFile() throws Exception {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select file to import");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("BEAST input files", "*.txt"),
                new FileChooser.ExtensionFilter("All files", "*.*"));

        File file = fc.showOpenDialog(null);

        if (file == null)
            throw new Exception();

        return file.getPath();
    }

    SRImportFileFormat determineSRImportFileFormat(String file_) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(file_));

        try {
            String line = br.readLine();
            String[] strs = line.split("\\s+");
            if(strs.length==1) {
                return SRImportFileFormat.ONECOL;
            } else if(strs.length==3) {
                return SRImportFileFormat.MULTICOL;
            } else {
                throw new Exception("Invalid format");
            }
        } finally {
            br.close();
        }
    }
}
