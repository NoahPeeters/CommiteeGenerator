package to.us.peeters.committeegenerator;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by noah on 5/3/17.
 * The code belongs to it's creator. Please don't steal it!
 */
public class Attendee {
    private final SimpleStringProperty name;
    private final SimpleStringProperty selected;
    private final List<SimpleStringProperty> choices;

    private int confirmationScore = 0;

    Attendee(String name, List<String> choices, ChangeListener<? super String> listener) {
        this.name = new SimpleStringProperty(name);
        this.selected = new SimpleStringProperty("");

        this.choices = new ArrayList<>();

        for (String choice: choices) {
            SimpleStringProperty c = new SimpleStringProperty(choice);
            c.addListener(listener);
            this.choices.add(c);
        }
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void resetGroups() {
        this.selected.set("");
        confirmationScore = 0;
    }

    public void confirmGroup(String name, int index) {
        System.out.println("confirmed: " + name);
        String current = this.selected.get();
        if (current.length() == 0) {
            this.selected.set(name);
        } else {
            this.selected.set(current + ", " + name);
        }
        confirmationScore += 1.0/index + index/14.4;
    }

    public String getName() {
        return this.name.get();
    }

    public int getConfirmationScore() {
        return confirmationScore;
    }

    public String getSelected() {
        return this.selected.get();
    }

    SimpleStringProperty getChoiceAtIndex(Integer index) {
        return choices.get(index);
    }
}
