import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task {
    private final StringProperty name;
    private final StringProperty date;
    private final StringProperty priority;
    private final StringProperty status;
    private final StringProperty category;

    public Task(String name, String date, String priority, String status, String category) {
        this.name     = new SimpleStringProperty(name);
        this.date     = new SimpleStringProperty(date);
        this.priority = new SimpleStringProperty(priority);
        this.status   = new SimpleStringProperty(status);
        this.category = new SimpleStringProperty(category);
    }

    // Name
    public String getName()              { return name.get(); }
    public void   setName(String value)  { name.set(value); }
    public StringProperty nameProperty(){ return name; }

    // Date
    public String getDate()               { return date.get(); }
    public void   setDate(String value)   { date.set(value); }
    public StringProperty dateProperty()  { return date; }

    // Priority
    public String getPriority()                  { return priority.get(); }
    public void   setPriority(String value)      { priority.set(value); }
    public StringProperty priorityProperty()     { return priority; }

    // Status
    public String getStatus()                { return status.get(); }
    public void   setStatus(String value)    { status.set(value); }
    public StringProperty statusProperty()   { return status; }

    // Category
    public String getCategory()                { return category.get(); }
    public void   setCategory(String value)    { category.set(value); }
    public StringProperty categoryProperty()   { return category; }
}
public class Task {

}
