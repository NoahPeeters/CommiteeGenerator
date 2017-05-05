package to.us.peeters.committeegenerator;

import com.opencsv.CSVReader;
import com.sun.tools.javac.main.OptionHelper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import javafx.util.converter.NumberStringConverter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by noah on 5/3/17.
 * The code belongs to it's creator. Please don't steal it!
 */
public class Controller implements javafx.fxml.Initializable {
    public AnchorPane window;
    public TableView<Attendee> attendeesTableView;
    public TableView groupsTableView;
    public TextField textFiledNumberOfChoices;
    public TextArea statusConsole;

    private final ObservableList<Attendee> attendees;
    private final ObservableList<Group> groups;
    private int numberOfChoices = 2;
    private int maxNumberOfGroupsPerAttendee = 2;

    public Controller() {
        attendees = FXCollections.observableArrayList();
        groups = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textFiledNumberOfChoices.setText("" + numberOfChoices);
        attendeesTableView.setItems(attendees);
        attendeesTableView.setEditable(true);

        TableColumn<Attendee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setEditable(false);

        TableColumn<Group, Number> maxAttCol = new TableColumn<>("Max. Attendees");
        maxAttCol.setCellValueFactory(cellData -> cellData.getValue().getMaxAttendees());

        maxAttCol.setEditable(true);
        maxAttCol.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));

        TableColumn<Group, Number> calAttCol = new TableColumn<>("Calc. Attendees");
        calAttCol.setCellValueFactory(cellData -> cellData.getValue().getConfirmedAttendees());

        groupsTableView.getColumns().setAll(nameCol, maxAttCol, calAttCol);
        groupsTableView.setItems(groups);
        groupsTableView.setEditable(true);

        resetAttendeesTableView();
    }

    public void calculate() {
        HashMap<String, Group> groupHashMap = new HashMap<>();

        for (Group group: groups) {
            group.resetConfirmedAttendees();
            groupHashMap.put(group.getName(), group);
        }

        for (Attendee attendee: attendees) {
            attendee.resetGroups();
        }

        List<HashMap<String, List<Attendee>>> attendeesPerGroup = new ArrayList<>();
        for (int i = 0; i < numberOfChoices; i++) {
            HashMap<String, List<Attendee>> emptyHashMap = new HashMap<>();
            for (Group group: groups) {
                emptyHashMap.put(group.getName(), new ArrayList<>());
            }
            attendeesPerGroup.add(emptyHashMap);
        }

        for (Attendee attendee: attendees) {
            for (int i = 0; i < numberOfChoices; i++) {
                String name = attendee.getChoiceAtIndex(i).get();
                attendeesPerGroup.get(i).get(name).add(attendee);
            }
        }

        for (int i = 0; i < attendeesPerGroup.size(); i++) {
            for (Map.Entry<String, List<Attendee>> entry : attendeesPerGroup.get(i).entrySet()) {
                Group group = groupHashMap.get(entry.getKey());
                List<Attendee> groupAttendees = entry.getValue();
                int maxAttendees = group.getMaxAttendees().get();
                int calcAttendees = group.getConfirmedAttendees().get();

                if (calcAttendees + groupAttendees.size() <= maxAttendees) {
                    for (Attendee attendee: groupAttendees) {
                        System.out.println(attendee.getName());
                        attendee.confirmGroup(group.getName(), i);
                        group.confirmAttendee();
                    }
                } else {
                    List<Pair<Attendee, Integer>> scoredAttendees = new ArrayList<>();

                    for (Attendee attendee: groupAttendees) {
                        int score = attendee.getConfirmationScore();
                        scoredAttendees.add(new Pair<>(attendee, score));
                    }

                    scoredAttendees.sort(Comparator.comparing(Pair::getValue));

                    for (int j = 0; j < maxAttendees - calcAttendees; j++) {
                        scoredAttendees.get(j).getKey().confirmGroup(group.getName(), i);
                        group.confirmAttendee();
                    }

                }
            }
        }

        groupsTableView.refresh();
        attendeesTableView.refresh();
    }

    public void startCSVImport(ActionEvent actionEvent) {
        // generate extension filters
        FileChooser.ExtensionFilter csvExtentionFilter = new FileChooser.ExtensionFilter("CSV File", "*.csv");
        FileChooser.ExtensionFilter txtExtentionFilter = new FileChooser.ExtensionFilter("TXT File", "*.txt");
        FileChooser.ExtensionFilter anyExtentionFilter = new FileChooser.ExtensionFilter("Any File", "*.*");

        // generate file chooser
        FileChooser csvFileChooser = new FileChooser();
        csvFileChooser.getExtensionFilters().setAll(csvExtentionFilter, txtExtentionFilter, anyExtentionFilter);
        csvFileChooser.setSelectedExtensionFilter(csvExtentionFilter);
        csvFileChooser.setTitle("Choose a file to import");
        File file = csvFileChooser.showOpenDialog(window.getScene().getWindow());
        if (file != null) {
            if (!file.exists()) {
                Main.showError("Import Error", "No such file");
            } else if (!file.isFile()) {
                Main.showError("Import Error", "Not a file");
            } else if (!file.isFile()) {
                Main.showError("Import Error", "No read permission");
            } else {
                System.out.println(file.getAbsolutePath());
                try {
                    readCSVFile(file);
                } catch (IOException e) {
                    Main.showError("Import Error", "Error while reading file");
                }
            }
        }
    }

    private void addMessage(String message) {
        statusConsole.appendText("\n" + message);
        statusConsole.setScrollTop(Double.MAX_VALUE);
    }

    private Group getGroupByName(String name) {
        for (Group group: groups) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }

    private void addGroupIfRequired(String name) {
        for (Group group: groups) {
            if (group.getName().equals(name)) {
                group.foundAttendee();
                return;
            }
        }

        Group group = new Group(name, 1, (observable, oldValue, newValue) -> calculate());
        group.foundAttendee();
        groups.add(group);
    }

    private Attendee addAttendee(String name, List<String> choices) {
        for (String choice: choices) {
            addGroupIfRequired(choice);
        }

        Attendee attendee = new Attendee(name, choices, (observable, oldValue, newValue) -> {
            for (Group group: groups) {
                if (group.getName().equals(oldValue)) {
                    group.removeAttendee();
                    if (group.hasNoAttendees()) {
                        groups.remove(group);
                    }
                    break;
                }
            }

            addGroupIfRequired(newValue);
            calculate();
        });
        attendees.add(attendee);
        return attendee;
    }

    private void readCSVFile(File file) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(file.getAbsoluteFile()));

        String [] nextLine;
        addMessage("Start import.");
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine.length == 0) {
                addMessage("Found empty line.");
            } else if (nextLine.length != numberOfChoices + 1) {
                addMessage("Mismatch of numbers of choices for attendee '" + nextLine[0] + "'. Found " + (nextLine.length - 1) + " but expected " + numberOfChoices + ".");
            } else {
                 addAttendee(nextLine[0], Arrays.asList(nextLine).subList(1, nextLine.length));
            }
        }
        calculate();
        addMessage("Finished import.");
    }

    public void exit(ActionEvent actionEvent) {
        Main.askToExit();
    }

    public void createNewAttendee(ActionEvent actionEvent) {
        List<String> choices = new ArrayList<>();

        for (int i = 0; i < numberOfChoices; i++) {
            choices.add("");
        }

        addAttendee("", choices);
        calculate();
    }

    public void onlyAllowNumbers(KeyEvent keyEvent) {
        String characters = keyEvent.getCharacter();
        if (characters.length() > 0) {
            int character = (int) characters.charAt(0);
            if (character < 48 || character > 57) {
                keyEvent.consume();
            }
        }
    }

    private void resetAttendeesTableView() {
        attendees.clear();
        groups.clear();

        TableColumn<Attendee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setEditable(true);
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Attendee, String> selectedCol = new TableColumn<>("Selected Group(s)");
        selectedCol.setCellValueFactory(new PropertyValueFactory<>("selected"));


        attendeesTableView.getColumns().setAll(nameCol, selectedCol);

        for (int i = 0; i < numberOfChoices; i++) {
            TableColumn<Attendee, String> col = new TableColumn<>((i + 1) + ". Choice");
            col.setCellValueFactory(new IndexValueFactory<Attendee, String>(i));
            col.setEditable(true);
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            attendeesTableView.getColumns().add(col);
        }

        attendeesTableView.setEditable(true);
    }

    public void numberOfChoicesChanged(ActionEvent actionEvent) {
        int newNumberOfChoices = 0;
        try {
            newNumberOfChoices = Integer.parseInt(textFiledNumberOfChoices.getText());
        } catch (Exception e) {
            addMessage("Cannot convert '" + textFiledNumberOfChoices.getText() + "' to a number.");
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Confirm Change");
        alert.setHeaderText("Do you want to continue? All your data will be lost.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES){
            numberOfChoices = newNumberOfChoices;
            resetAttendeesTableView();
        }
    }
}
