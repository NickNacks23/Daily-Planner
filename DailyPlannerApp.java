//Daily Planner JavaFX App
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;


public class DailyPlannerApp extends Application {
   private final ObservableList<Task> allTasks = FXCollections.observableArrayList();
   private final ObservableList<String> categories = FXCollections.observableArrayList("Work", "Personal", "Errands");


   private TableView<Task> taskTable;
   private ComboBox<String> categoryFilter;
   private FlowPane taskChips;


   private YearMonth currentYearMonth = YearMonth.now();
   private LocalDate currentDate = LocalDate.now();
   private GridPane monthGrid;
   private Label monthLabel;
   private TextArea agendaArea;


   private TabPane tabs;
   private VBox calendarViewContainer;


   private enum CalendarViewMode { DAY, WEEK, MONTH }
   private CalendarViewMode currentViewMode = CalendarViewMode.MONTH;


   public static void main(String[] args) {
       launch(args);
   }


   @Override
   public void start(Stage primaryStage) {
       tabs = new TabPane();
       Tab dashboardTab = new Tab("Tasks", createDashboard());
       Tab newTaskTab = new Tab("New Task", createNewTaskForm());
       Tab calendarTab = new Tab("Calendar", createCalendarView());


       tabs.getTabs().addAll(dashboardTab, newTaskTab, calendarTab);
       tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


       Scene scene = new Scene(tabs, 900, 600);
       primaryStage.setTitle("Daily Planner / To-Do List");
       primaryStage.setScene(scene);
       primaryStage.show();
       scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

   }


   private VBox createDashboard() {
       Label header = new Label("Today: " + LocalDate.now());
       header.setFont(Font.font("Arial", 20));


       Button addButton = new Button("Add New Task +");
       addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
       addButton.setOnAction(e -> tabs.getSelectionModel().select(1));

       

       categoryFilter = new ComboBox<>();
       updateCategoryFilterItems();
       categoryFilter.setValue("All");
       categoryFilter.setOnAction(e -> refreshTaskTable());


       Button addCatBtn = new Button("Add Category");
       addCatBtn.setOnAction(e -> {
           TextInputDialog dialog = new TextInputDialog();
           dialog.setTitle("Add Category");
           dialog.setHeaderText("Create a new category");
           dialog.setContentText("Category name:");
           dialog.showAndWait().ifPresent(name -> {
               if (!name.isBlank() && !categories.contains(name)) {
                   categories.add(name);
                   updateCategoryFilterItems();
                   updateChips(taskChips);
               }
           });
       });


       Button delCatBtn = new Button("Delete Category");
       delCatBtn.setOnAction(e -> {
           ChoiceDialog<String> dialog = new ChoiceDialog<>(null, categories);
           dialog.setTitle("Delete Category");
           dialog.setHeaderText("Select a category to delete");
           dialog.setContentText("Category:");
           dialog.showAndWait().ifPresent(name -> {
               categories.remove(name);
               if (categoryFilter.getValue().equals(name)) {
                   categoryFilter.setValue("All");
               }
               updateCategoryFilterItems();
               updateChips(taskChips);
           });
       });


       HBox catControls = new HBox(5, addCatBtn, delCatBtn);
       catControls.setAlignment(Pos.CENTER_LEFT);


       HBox topBar = new HBox(10, header, addButton, new Label("Category:"), categoryFilter, catControls);
       topBar.setPadding(new Insets(10));
       topBar.setAlignment(Pos.CENTER_LEFT);


       taskChips = new FlowPane(8, 8);
       updateChips(taskChips);
       taskChips.setPadding(new Insets(0, 10, 10, 10));


       taskTable = new TableView<>();
       taskTable.setPlaceholder(new Label("No tasks yet — click 'Add New Task +'"));
       
       // Done Column with checkbox, this will aslo update the status
       TableColumn<Task, Boolean> doneCol = new TableColumn<>("Done");
       doneCol.setCellValueFactory(param -> param.getValue().doneProperty());
       doneCol.setCellFactory(col -> {
           CheckBoxTableCell<Task, Boolean> cell = new CheckBoxTableCell<>();
           cell.setSelectedStateCallback(index -> {
               Task task = taskTable.getItems().get(index);
               BooleanProperty selected = task.doneProperty();
               selected.addListener((obs, wasDone, isNowDone) -> {
                   task.statusProperty().set(isNowDone ? "Done" : "Pending");
                   taskTable.refresh(); // ensures strikethrough updates visually
               });
               return selected;
           });
           return cell;
       });
       doneCol.setEditable(true);
       
       //For eeach task, wehn a user clicks on the done box, there will be a mark
       TableColumn<Task, String> nameCol = new TableColumn<>("Task");
       nameCol.setCellValueFactory(param -> param.getValue().nameProperty());
       nameCol.setCellFactory(column -> new TableCell<>() {
           @Override
           protected void updateItem(String item, boolean empty) {
               super.updateItem(item, empty);
               if (empty || item == null) {
                   setText(null);
                   setStyle("");
               } else {
                   Task task = getTableView().getItems().get(getIndex());
                   setText(item);
                   setStyle(task.doneProperty().get() ? "-fx-strikethrough: true; -fx-text-fill: gray;" : "");
       
                   task.doneProperty().addListener((obs, wasDone, isNowDone) -> {
                       setStyle(isNowDone ? "-fx-strikethrough: true; -fx-text-fill: gray;" : "");
                   });
               }
           }
       });
       
       TableColumn<Task, String> dateCol = new TableColumn<>("Due Date");
dateCol.setCellValueFactory(param -> param.getValue().dateProperty());
dateCol.setPrefWidth(130);  //width for dates of tasks

       
       TableColumn<Task, String> prioCol = new TableColumn<>("Priority");
       prioCol.setCellValueFactory(param -> param.getValue().priorityProperty());
       
       TableColumn<Task, String> statusCol = new TableColumn<>("Status");
       statusCol.setCellValueFactory(param -> param.getValue().statusProperty());
       
       taskTable.getColumns().addAll(doneCol, nameCol, dateCol, prioCol, statusCol);
       taskTable.setItems(allTasks);
       taskTable.setEditable(true);
       


       VBox dashboard = new VBox(10, topBar, taskChips, taskTable);
       dashboard.setPadding(new Insets(10));
       VBox.setVgrow(taskTable, Priority.ALWAYS);
       return dashboard;
   }


   private VBox createNewTaskForm() {
       GridPane form = new GridPane();
       form.setHgap(10); form.setVgap(12); form.setPadding(new Insets(20));


       TextField titleField = new TextField();
       TextArea descArea = new TextArea();
       DatePicker datePicker = new DatePicker(LocalDate.now());
       TextField timeField = new TextField("14:00");


       ToggleGroup prioGroup = new ToggleGroup();
       RadioButton highRB = new RadioButton("High");
       RadioButton medRB = new RadioButton("Medium");
       RadioButton lowRB = new RadioButton("Low");
       highRB.setToggleGroup(prioGroup);
       medRB.setToggleGroup(prioGroup);
       lowRB.setToggleGroup(prioGroup);
       medRB.setSelected(true);


       ComboBox<String> catBox = new ComboBox<>(categories);
       catBox.setEditable(true);
       catBox.setValue(categories.get(0));


       TextField tagsField = new TextField();


       Button cancelBtn = new Button("Cancel");
       cancelBtn.setOnAction(e -> tabs.getSelectionModel().select(0));


       Button saveBtn = new Button("Save Task ✔");
       saveBtn.setOnAction(e -> {
           String name = titleField.getText();
           String date = datePicker.getValue().toString();
           Toggle toggle = prioGroup.getSelectedToggle();
           String prio = toggle == null ? "Medium" : ((RadioButton) toggle).getText();
           String status = "Pending";
           String category = catBox.getValue();
           if (!categories.contains(category)) {
               categories.add(category);
               updateCategoryFilterItems();
               updateChips(taskChips);
           }
           Task task = new Task(name, date, prio, status);
           allTasks.add(task);
           clearForm(titleField, descArea, datePicker, timeField, prioGroup, catBox, tagsField);
           tabs.getSelectionModel().select(0);
       });


       form.add(new Label("Title:"),0,0); form.add(titleField,1,0);
       form.add(new Label("Description:"),0,1); form.add(descArea,1,1);
       form.add(new Label("Due Date:"),0,2); form.add(datePicker,1,2);
       form.add(new Label("Time:"),0,3);     form.add(timeField,1,3);
       form.add(new Label("Priority:"),0,4); form.add(new HBox(10,highRB,medRB,lowRB),1,4);
       form.add(new Label("Category:"),0,5); form.add(catBox,1,5);
       form.add(new HBox(10,cancelBtn,saveBtn),1,7);


       return new VBox(form);
   }


   private VBox createCalendarView() {
       Button prevYear = new Button("«");
       Button prevMonth = new Button("‹");
       monthLabel = new Label();
       Button nextMonth = new Button("›");
       Button nextYear = new Button("»");
       HBox nav = new HBox(10, prevYear, prevMonth, monthLabel, nextMonth, nextYear);
       nav.setAlignment(Pos.CENTER);
       nav.setPadding(new Insets(10));


       prevYear.setOnAction(e -> { currentYearMonth = currentYearMonth.minusYears(1); updateCalendarView(); });
       nextYear.setOnAction(e -> { currentYearMonth = currentYearMonth.plusYears(1); updateCalendarView(); });
       prevMonth.setOnAction(e -> { currentYearMonth = currentYearMonth.minusMonths(1); updateCalendarView(); });
       nextMonth.setOnAction(e -> { currentYearMonth = currentYearMonth.plusMonths(1); updateCalendarView(); });


       monthGrid = new GridPane();
       monthGrid.setHgap(5);
       monthGrid.setVgap(5);
       monthGrid.setPadding(new Insets(10));


       agendaArea = new TextArea();
       agendaArea.setEditable(false);
       agendaArea.setPrefHeight(120);


       ToggleGroup viewToggle = new ToggleGroup();
       RadioButton dayView = new RadioButton("Day View");
       RadioButton weekView = new RadioButton("Week View");
       RadioButton monthView = new RadioButton("Month View");
       dayView.setToggleGroup(viewToggle);
       weekView.setToggleGroup(viewToggle);
       monthView.setToggleGroup(viewToggle);
       monthView.setSelected(true);


       viewToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
           if (newVal != null) {
               String selected = ((RadioButton) newVal).getText();
               if (selected.equals("Month View")) currentViewMode = CalendarViewMode.MONTH;
               else if (selected.equals("Day View")) currentViewMode = CalendarViewMode.DAY;
               else if (selected.equals("Week View")) currentViewMode = CalendarViewMode.WEEK;
               updateCalendarView();
           }
       });


       HBox viewBox = new HBox(10, dayView, weekView, monthView);
       viewBox.setAlignment(Pos.CENTER);
       viewBox.setPadding(new Insets(10));


       calendarViewContainer = new VBox();


       VBox calendar = new VBox(10, nav, viewBox, calendarViewContainer, new Label("Agenda:"), agendaArea);
       calendar.setPadding(new Insets(10));
       VBox.setVgrow(calendarViewContainer, Priority.ALWAYS);


       updateCalendarView();
       return calendar;
   }


   private void updateCalendarView() {
       calendarViewContainer.getChildren().clear();
       switch (currentViewMode) {
           case MONTH -> buildMonthGrid();
           case DAY -> buildDayView(currentDate);
           case WEEK -> buildWeekView(currentDate);
       }
       updateAgenda(currentDate);
   }


   private void buildDayView(LocalDate date) {
       VBox dayBox = new VBox(5);
       for (int hour = 0; hour < 24; hour++) {
           String hourLabel = String.format("%02d:00", hour);
           Label label = new Label(hourLabel);
           VBox slot = new VBox(label);
           slot.setStyle("-fx-border-color: #ccc; -fx-padding: 4;");
           int finalHour = hour;
           allTasks.stream()
               .filter(t -> t.getDate().equals(date.toString()))
               .filter(t -> t.getName().contains(String.format("%02d:", finalHour)))
               .forEach(t -> slot.getChildren().add(new Label("- " + t.getName())));
           dayBox.getChildren().add(slot);
       }
       calendarViewContainer.getChildren().add(dayBox);
   }


   private void buildWeekView(LocalDate baseDate) {
       VBox weekBox = new VBox(10);
       LocalDate start = baseDate.with(DayOfWeek.MONDAY);
       for (int i = 0; i < 7; i++) {
           LocalDate date = start.plusDays(i);
           VBox dayBox = new VBox(new Label(date.getDayOfWeek() + " - " + date));
           allTasks.stream()
               .filter(t -> t.getDate().equals(date.toString()))
               .forEach(t -> dayBox.getChildren().add(new Label("- " + t.getName())));
           dayBox.setStyle("-fx-border-color: #aaa; -fx-padding: 4;");
           dayBox.setOnMouseClicked(e -> {
               currentDate = date;
               updateCalendarView();
           });
           weekBox.getChildren().add(dayBox);
       }
       calendarViewContainer.getChildren().add(weekBox);
   }


   private void buildMonthGrid() {
       monthGrid.getChildren().clear();
       monthLabel.setText(currentYearMonth.getMonth() + " " + currentYearMonth.getYear());


       String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
       for (int i = 0; i < days.length; i++) {
           Label day = new Label(days[i]);
           day.setFont(Font.font(12));
           monthGrid.add(day, i, 0);
       }


       LocalDate firstDay = currentYearMonth.atDay(1);
       int col = firstDay.getDayOfWeek().getValue() % 7;
       int row = 1;
       int daysInMonth = currentYearMonth.lengthOfMonth();


       for (int d = 1; d <= daysInMonth; d++) {
           VBox cell = new VBox();
           cell.setPadding(new Insets(4));
           cell.setStyle("-fx-border-color: #ccc;");
           Label dateLabel = new Label(String.valueOf(d));
           cell.getChildren().add(dateLabel);
           final int currentDay = d;
           LocalDate thisDate = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), currentDay);
           if (allTasks.stream().anyMatch(t -> t.getDate().equals(thisDate.toString()))) {
               cell.getChildren().add(new Label("•"));
           }
           cell.setOnMouseClicked(e -> {
               currentDate = thisDate;
               updateCalendarView();
           });
           monthGrid.add(cell, col, row);
           if (++col > 6) { col = 0; row++; }
       }
       calendarViewContainer.getChildren().add(monthGrid);
   }


   private void updateAgenda(LocalDate date) {
       StringBuilder sb = new StringBuilder("Tasks on " + date + ":\n");
       allTasks.stream()
               .filter(t -> t.getDate().equals(date.toString()))
               .forEach(t -> sb.append("- ").append(t.getName()).append("\n"));
       agendaArea.setText(sb.toString());
   }


   private void updateCategoryFilterItems() {
       if (categoryFilter != null) {
           categoryFilter.getItems().setAll("All");
           categoryFilter.getItems().addAll(categories);
       }
   }


   private void updateChips(FlowPane chips) {
       chips.getChildren().clear();
       chips.getChildren().add(createChip("All"));
       for (String cat : categories) {
           chips.getChildren().add(createChip(cat));
       }
   }


   private Label createChip(String label) {
       Label chip = new Label(label);
       chip.setStyle("-fx-border-color: gray; -fx-border-radius: 4; -fx-padding: 4 8; -fx-cursor: hand;");
       chip.setOnMouseClicked(e -> categoryFilter.setValue(label));
       return chip;
   }


   private void refreshTaskTable() {
       taskTable.refresh();
   }


   private void clearForm(TextField title, TextArea desc, DatePicker date, TextField time,
                          ToggleGroup prio, ComboBox<String> cat, TextField tags) {
       title.clear(); desc.clear(); date.setValue(LocalDate.now());
       time.setText("14:00"); prio.selectToggle(prio.getToggles().get(1));
       cat.setValue("Work");
      tags.clear();
  }




  private Label createChip(String label) {
      Label chip = new Label(label);
      chip.setStyle("-fx-border-color: gray; -fx-border-radius: 4; -fx-padding: 4 8; -fx-cursor: hand;");
      chip.setOnMouseClicked(e -> categoryFilter.setValue(label));
      return chip;
  }




  public static class Task {
      private final SimpleStringProperty name;
      private final SimpleStringProperty date;
      private final SimpleStringProperty priority;
      private final SimpleStringProperty status;
      private final SimpleBooleanProperty done;




      public Task(String name, String date, String priority, String status) {
          this.name = new SimpleStringProperty(name);
          this.date = new SimpleStringProperty(date);
          this.priority = new SimpleStringProperty(priority);
          this.status = new SimpleStringProperty(status);
          this.done = new SimpleBooleanProperty(false);
      }




      public StringProperty nameProperty() { return name; }
      public String getName() { return name.get(); }




      public StringProperty dateProperty() { return date; }
      public String getDate() { return date.get(); }




      public StringProperty priorityProperty() { return priority; }
      public String getPriority() { return priority.get(); }




      public StringProperty statusProperty() { return status; }
      public String getStatus() { return status.get(); }




      public BooleanProperty doneProperty() { return done; }
  }
}




