package to.us.peeters.committeegenerator;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

/**
 * Created by noah on 5/4/17.
 * The code belongs to it's creator. Please don't steal it!
 */
public class Group {
    private final SimpleStringProperty name;
    private final IntegerProperty maxAttendees;
    private final IntegerProperty confirmedAttendees;
    private Integer atLeastOneAttendee = 0;

    public Group(String name, Integer maxAttendees, ChangeListener<? super Number> listener) {
        this.name = new SimpleStringProperty(name);
        this.maxAttendees = new SimpleIntegerProperty(maxAttendees);
        this.maxAttendees.addListener(listener);
        this.confirmedAttendees = new SimpleIntegerProperty(0);
    }

    public String getName() {
        return name.getValue();
    }

    public IntegerProperty getMaxAttendees() {
        return maxAttendees;
    }

    public IntegerProperty getConfirmedAttendees() {
        return confirmedAttendees;
    }

    public void resetConfirmedAttendees() {
        confirmedAttendees.set(0);
    }

    public void confirmAttendee() {
        confirmedAttendees.set(getConfirmedAttendees().getValue() + 1);
    }

    public void resetAttendees() {
        atLeastOneAttendee = 0;
    }

    public void foundAttendee() {
        atLeastOneAttendee += 1;
    }

    public void removeAttendee() {
        atLeastOneAttendee -= 1;
    }

    public Boolean hasAtLeastOneAttendee() {
        return atLeastOneAttendee > 0;
    }

    public Boolean hasNoAttendees() {
        return atLeastOneAttendee == 0;
    }
}
