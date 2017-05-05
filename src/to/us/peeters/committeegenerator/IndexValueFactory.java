package to.us.peeters.committeegenerator;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Created by noah on 5/4/17.
 * The code belongs to it's creator. Please don't steal it!
 */
public class IndexValueFactory<S, T> implements Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> {
    private Integer index;

    public IndexValueFactory(Integer index) {
        this.index = index;
    }

    @Override
    public ObservableValue<T> call(TableColumn.CellDataFeatures<S, T> p) {
        Attendee rowData = (Attendee) p.getValue();

        return (ObservableValue<T>)rowData.getChoiceAtIndex(index);
    }
}

