package sr.beauti;
//package beastfx.app.inputeditor;

import sr.util.Tools;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.control.Separator;

import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableView.TableViewSelectionModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import javafx.geometry.Orientation;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import beastfx.app.beauti.ThemeProvider;
import beastfx.app.util.Alert;
import beastfx.app.util.FXUtils;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import beastfx.app.inputeditor.BeautiDoc;

import javax.swing.UIManager;

import beast.base.evolution.alignment.Taxon;
import beast.base.evolution.alignment.TaxonSet;
import sr.evolution.tree.SRTree;

import beast.base.core.BEASTInterface;
import beast.base.core.BEASTObject;

import sr.evolution.sranges.StratigraphicRange;


public class StratigraphicRangeDialog extends DialogPane {
    public boolean isOK = false;
    String id;
    TextField idEntry;

    List<Taxon> _taxa;
    ArrayList<StratigraphicRange> _stratigraphicRanges;

    //details section
    TextField rangeID;
    TextField firstOccurrenceID;
    TextField lastOccurrenceID;


    TextField filterEntry;

    ListView<Taxon> listOfTaxon;
    ListView<StratigraphicRange> listOfSR;

    String selectedTaxon = ""; // used for clipboard management

    VBox box;
    BeautiDoc doc;

    public StratigraphicRangeDialog(ArrayList<StratigraphicRange> stratigraphicRanges, Set<Taxon> tset, BeautiDoc doc) {

        _stratigraphicRanges = stratigraphicRanges;
        this.doc = doc;

        // create components
        box = FXUtils.newVBox();
        setBoxStyle(box);




        //FIXME: remove _taxa
        _taxa = new ArrayList<>();
        _taxa.addAll(tset);
        Comparator<Taxon> comparator = (o1, o2) -> o1.getID().compareTo(o2.getID());
        Collections.sort(_taxa, comparator);

        listOfTaxon = new ListView<>();
        listOfTaxon.getItems().addAll(_taxa);

        listOfSR = new ListView<StratigraphicRange>();

        for (StratigraphicRange sr: stratigraphicRanges) {
            listOfSR.getItems().add(sr);
        }

        box.getChildren().add(createLayout1());
        box.getChildren().add(createBtnsBar());
        box.getChildren().add(createTaxonSelectorPanel());

        //box.getChildren().add(new Separator());
        //box.add(createCancelOKButtons());

        getChildren().add(box);
        int size = UIManager.getFont("Label.font").getSize();
        setPrefSize(600 * size / 13, 600 * size / 13);
        //setPrefSize(400 * size / 13, 600 * size / 13);
        //setModal(true);
    }

    public boolean showDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(this);
        dialog.setResizable(true);
 
        getButtonTypes().addAll(Alert.OK_CANCEL_OPTION);
        dialog.setTitle("Stratigraphic range editor");      

        ThemeProvider.loadStyleSheet(this.getScene());

        Optional<ButtonType> result = dialog.showAndWait();

        isOK = result.get() == Alert.OK_OPTION;
        if (isOK) {
            _stratigraphicRanges.clear();
            for (StratigraphicRange sr: listOfSR.getItems()) {
                _stratigraphicRanges.add(sr);
            }
        }
        return isOK;
    }

    ListView createListOfSR () {
        listOfSR.setId("listOfSR");
        listOfSR.setMinSize(250,100);
        //listOfSR.setPrefSize(200,100);

        //debug
        //listOfSR.getItems().add(new StratigraphicRange("a_b_range", new Taxon("a_b_9.9"), new Taxon("a_b_9.9")));
        //listOfSR.getItems().add(new StratigraphicRange("c_d_range", new Taxon("c_d_0.9"), new Taxon("c_d_0.9")));

        listOfSR.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<StratigraphicRange>() {

            @Override
            public void changed(ObservableValue<? extends StratigraphicRange> observable, StratigraphicRange oldValue, StratigraphicRange newValue) {

                if(newValue == null) {
                    //we come here when we click the 'clear' button.
                    //nothing to do as the clearButton handler already handle the situation.

                    return;
                }

                setDetails(newValue);
            }
        });

        listOfSR.setCellFactory(new Callback<ListView<StratigraphicRange>, ListCell<StratigraphicRange>>() {
            @Override
            public ListCell<StratigraphicRange> call(ListView<StratigraphicRange> param) {
                return new ListCell<StratigraphicRange>() {
                    @Override
                    public void updateItem(StratigraphicRange sr, boolean empty) {
                        super.updateItem(sr, empty);
                        if (empty || sr == null) {
                            setText(null);
                        } else {
                            setText(sr.getID());
                        }
                    }
                };
            }
        });

        VBox.setVgrow(listOfSR, Priority.ALWAYS);

        return listOfSR;
    }

    Pane createListOfSRPanel () {
        VBox myvbox = FXUtils.newVBox();
        setBoxStyle(myvbox);

        Label label = new Label("List of stratigraphic range");
        label.setMinSize(200,20);
        myvbox.getChildren().add(label);        

        /*
        Separator separator1 = new Separator();
        myvbox.getChildren().add(separator1);
        */

        myvbox.getChildren().add(createListOfSR());

        return myvbox;
    }

    public void setDetails(StratigraphicRange sr) {
        rangeID.setText(sr.getID());
        firstOccurrenceID.setText(sr.getFirstOccurrenceID());
        lastOccurrenceID.setText(sr.getLastOccurrenceID());
    }

    public void clearDetails() {
        rangeID.setText("");
        firstOccurrenceID.setText("");
        lastOccurrenceID.setText("");
    }

    void createSR() {
        if( (rangeID.getText().length()<1) || (firstOccurrenceID.getText().length()<1) || (lastOccurrenceID.getText().length()<1) ) {
            return;
        }

        if (!Tools.checkRangeConsistency(firstOccurrenceID.getText(), lastOccurrenceID.getText())) {

            //FIXME (log or exception)
            //"Age inconsistency detected. First occurrence is older than last occurrence ("+rangeID.getText()+")"

            return;
        }

        listOfSR.getItems().add(new StratigraphicRange(rangeID.getText(), new Taxon(firstOccurrenceID.getText()), new Taxon(lastOccurrenceID.getText())));
    }

    void editSR() {
        if( (rangeID.getText().length()<1) || (firstOccurrenceID.getText().length()<1) || (lastOccurrenceID.getText().length()<1) ) {
            return;
        }
        
        if (!Tools.checkRangeConsistency(firstOccurrenceID.getText(), lastOccurrenceID.getText())) {

            //FIXME (log or exception)
            //"Age inconsistency detected. First occurrence is older than last occurrence ("+rangeID.getText()+")"

            return;
        }

        int idx = listOfSR.getSelectionModel().getSelectedIndex();

        if(idx<0) {
            //nothing selected

            return;
        }

        StratigraphicRange sr = listOfSR.getItems().get(idx);

        sr.setID(rangeID.getText());
        sr.taxonFirstOccurrenceInput.get().setID(firstOccurrenceID.getText());
        sr.taxonLastOccurrenceInput.get().setID(lastOccurrenceID.getText());
    }

    Pane createDetailsBox() {
        HBox hbox10 = FXUtils.newHBox();
        setBoxStyle(hbox10);

        Label label22 = new Label("Range details");
        label22.setMinSize(200,20);
        String cssLayout = """
            -fx-font-weight: bold;
            """;
        label22.setStyle(cssLayout);
        //hbox10.getChildren().add(label22);        


        VBox vbox44 = FXUtils.newVBox();
        setBoxStyle(vbox44);

        Label rangeIDLabel = new Label("Name");
        rangeIDLabel.setMinSize(200,20);
        vbox44.getChildren().add(rangeIDLabel);        

        rangeID = new TextField();
        rangeID.setMinSize(250,30);
        rangeID.setId("rangeID");
        rangeID.setText("");
        //rangeID.setOnKeyReleased(e->{id = idEntry.getText();});
        vbox44.getChildren().add(rangeID);

        Label rangeTopLabel = new Label("First occurrence ID");
        rangeTopLabel.setMinSize(200,20);
        vbox44.getChildren().add(rangeTopLabel);        

        firstOccurrenceID = new TextField();
        firstOccurrenceID.setMinSize(250,30);
        firstOccurrenceID.setId("firstOccurrenceID");
        firstOccurrenceID.setText("");
        //firstOccurrenceID.setOnKeyReleased(e->{id = idEntry.getText();});
        vbox44.getChildren().add(firstOccurrenceID);

        Label rangeBottomLabel = new Label("Last occurrence ID");
        rangeBottomLabel.setMinSize(200,20);
        vbox44.getChildren().add(rangeBottomLabel);        

        lastOccurrenceID = new TextField();
        lastOccurrenceID.setMinSize(250,30);
        lastOccurrenceID.setId("lastOccurrenceID");
        lastOccurrenceID.setText("");
        //lastOccurrenceID.setOnKeyReleased(e->{id = idEntry.getText();});
        vbox44.getChildren().add(lastOccurrenceID);

        VBox vbox45 = FXUtils.newVBox();
        setBoxStyle(vbox45);

        vbox45.getChildren().add(hbox10);        
        vbox45.getChildren().add(vbox44);        

        return vbox45;
    }

    void setBoxStyle(Pane box) {

        // from stackoverflow.com/questions/33626858/add-border-around-vbox-in-javafx
        String cssLayout = """
                            -fx-border-color: black;
                            -fx-border-insets: 5;
                            -fx-border-width: 1;
                            -fx-border-style: dashed;
            """;

        //box.setStyle(cssLayout);
    }

    Pane createLayout1 () {
        HBox hbox2 = FXUtils.newHBox();
        setBoxStyle(hbox2);

        hbox2.getChildren().add(createListOfSRPanel());

        Separator separator2 = new Separator();
        separator2.setOrientation(Orientation.VERTICAL);
        hbox2.getChildren().add(separator2);

        hbox2.getChildren().add(createDetailsBox());
        //hbox2.getChildren().add(createFilterBox());

        return hbox2;
    }

    ListView<Taxon> createTaxonSelector() {

        listOfTaxon.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listOfTaxon.setId("listOfTaxon");
        listOfTaxon.setMinSize(300,100);
        listOfTaxon.setPrefSize(300,100);
        listOfTaxon.setCellFactory(new Callback<ListView<Taxon>, ListCell<Taxon>>() {
            @Override
            public ListCell<Taxon> call(ListView<Taxon> param) {
                return new ListCell<Taxon>() {
                    @Override
                    public void updateItem(Taxon taxon, boolean empty) {
                        super.updateItem(taxon, empty);
                        if (empty || taxon == null) {
                            setText(null);
                        } else {
                            setText(taxon.getID());
                        }
                    }
                };
            }
        });

        listOfTaxon.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Taxon>() {

            @Override
            public void changed(ObservableValue<? extends Taxon> observable, Taxon oldValue, Taxon newValue) {

                selectedTaxon = newValue.getID();
            }
        });

        final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        listOfTaxon.setOnKeyPressed(event -> {
            if (keyCodeCopy.match(event)) {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(selectedTaxon);
                clipboard.setContent(content);
            }
        });

        return listOfTaxon;
    }

    Pane createTaxonSelectorPanel() {
        HBox hbox = FXUtils.newHBox();
        setBoxStyle(hbox);

        VBox box = FXUtils.newVBox();
        setBoxStyle(box);

        Label rangeTopLabel = new Label("List of taxon");
        rangeTopLabel.setMinSize(100,20);
        box.getChildren().add(rangeTopLabel);        

        box.getChildren().add(createTaxonSelector());
        //ScrollPane scroller = new ScrollPane();
        //scroller.setContent(listOfTaxon);
        //box.getChildren().add(scroller);

        hbox.getChildren().add(box);        

        return hbox;
    }

    Pane createBtnsBar() {
        VBox vbox = FXUtils.newVBox();
        setBoxStyle(vbox);

        HBox hbox12 = FXUtils.newHBox();
        setBoxStyle(hbox12);
        hbox12.setMinSize(400,30);

        Button createNewRangeBtn = new Button("Create");
        createNewRangeBtn.setId("btnCreate");
        //createNewRangeBtn.setMinSize(150,30);
        //createNewRangeBtn.setPrefSize(150,30);
        createNewRangeBtn.setOnAction(e -> { createSR(); });
        hbox12.getChildren().add(createNewRangeBtn);

        Button rangeEditBtn = new Button("Update");
        rangeEditBtn.setId("rangeEditBtn");
        rangeEditBtn.setOnAction(e -> { editSR(); });
        hbox12.getChildren().add(rangeEditBtn);

        Button deleteSelectedRangeBtn = new Button("Delete");
        deleteSelectedRangeBtn.setId("deleteSelectedRangeBtn");
        deleteSelectedRangeBtn.setOnAction(e -> {
                int idx = listOfSR.getSelectionModel().getSelectedIndex();

                if(idx<0) {
                    //nothing selected

                    return;
                }

                listOfSR.getItems().remove(idx);
            });
        hbox12.getChildren().add(deleteSelectedRangeBtn);

        Button clearRangeBtn = new Button("Clear");
        clearRangeBtn.setId("clearRangeBtn");
        clearRangeBtn.setOnAction(e -> {
                listOfSR.getItems().clear();
                clearDetails();
            });
        hbox12.getChildren().add(clearRangeBtn);

        Button importRangeBtn = new Button("Import");
        importRangeBtn.setId("importRangeBtn");
        //importRangeBtn.setMinSize(150,30);
        //importRangeBtn.setPrefSize(150,30);
        importRangeBtn.setOnAction(e -> {

                List<StratigraphicRange> sranges = null;
                try {
                    SRImport sri = new SRImport();
                    sranges = sri.importFile();

                    for (StratigraphicRange sr: sranges) {
                        listOfSR.getItems().add(sr);
                    }
                } catch (Exception ex) {
                    //FIXME
                    System.out.println("IO Error: "+ex);
                }

            });
        hbox12.getChildren().add(importRangeBtn);


        vbox.getChildren().add(hbox12);

        return vbox;
    }

}
