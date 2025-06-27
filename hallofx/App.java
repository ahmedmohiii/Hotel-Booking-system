/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hallofx;

// App.java

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

// import java.util.Comparator; // REMOVED - No longer needed

public class App extends Application {

    private Hotel hotelManager;
    private TableView<ARoom> roomTableView;
    private ObservableList<ARoom> roomObservableList;
    private Guest currentGuest;

    private DatePicker bookingCheckInDatePicker;
    private DatePicker bookingCheckOutDatePicker;

    @Override
    public void start(Stage primaryStage) {
        hotelManager = new Hotel();
        roomObservableList = FXCollections.observableArrayList();

        primaryStage.setTitle("HotelFX - Hotel Room Booking");
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // --- Top Pane (Title only) ---
        VBox topPane = new VBox(10); // Spacing if you add more elements later
        topPane.setAlignment(Pos.CENTER); // Center the title
        // topPane.setPadding(new Insets(0, 0, 10, 0)); // Padding below title

        Label titleLabel = new Label("Hotel Room Management");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        topPane.getChildren().add(titleLabel); // Only add title
        mainLayout.setTop(topPane);

        // --- Center Pane (Room Table) ---
        roomTableView = new TableView<>();
        setupRoomTableColumns();
        roomTableView.setItems(roomObservableList);
        mainLayout.setCenter(roomTableView);

        // --- Bottom Pane (Date Pickers and Action Buttons) ---
        BorderPane bottomAreaPane = new BorderPane();
        bottomAreaPane.setPadding(new Insets(10, 0, 0, 0));

        Label checkInLabel = new Label("Check-in:");
        bookingCheckInDatePicker = new DatePicker();
        bookingCheckInDatePicker.setPromptText("Select Check-in");
        bookingCheckInDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> roomTableView.refresh());

        Label checkOutLabel = new Label("Check-out:");
        bookingCheckOutDatePicker = new DatePicker();
        bookingCheckOutDatePicker.setPromptText("Select Check-out");
        bookingCheckOutDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> roomTableView.refresh());

        HBox datePickerBox = new HBox(5, checkInLabel, bookingCheckInDatePicker, checkOutLabel, bookingCheckOutDatePicker);
        datePickerBox.setAlignment(Pos.CENTER_LEFT);
        bottomAreaPane.setLeft(datePickerBox);

        Button bookSelectedButton = new Button("Book Selected Room");
        bookSelectedButton.setOnAction(e -> handleBookSelectedRoom());

        Button manageBookingsButton = new Button("Manage Bookings");
        manageBookingsButton.setOnAction(e -> handleManageBookings(primaryStage));

        HBox actionButtonBox = new HBox(10, bookSelectedButton, manageBookingsButton);
        actionButtonBox.setAlignment(Pos.CENTER_RIGHT);
        bottomAreaPane.setRight(actionButtonBox);

        mainLayout.setBottom(bottomAreaPane);

        populateInitialData();
        refreshRoomList(); // This will populate and sort the list by Room ID

        Scene scene = new Scene(mainLayout, 800, 550); // Height can be reduced as sort controls are gone
        primaryStage.setScene(scene);
        primaryStage.show();

        currentGuest = hotelManager.registerGuest("Default User", "default@example.com");
        System.out.println("Default guest " + currentGuest.getName() + " (ID: " + currentGuest.getGuestId() + ") ready for bookings.");
    }

    private void populateInitialData() {
        hotelManager.addRoom(new StandardRoom("S101", 75.00, 2));
        hotelManager.addRoom(new DeluxeRoom("D201", 120.00, 2));
        hotelManager.addRoom(new SuiteRoom("U301", 250.00, 4));
        // Add rooms out of order to see sorting work
        hotelManager.addRoom(new StandardRoom("S103", 75.00, 2));
        hotelManager.addRoom(new StandardRoom("S102", 80.00, 1));
        hotelManager.addRoom(new DeluxeRoom("D202", 125.00, 3));
    }

    private void setupRoomTableColumns() {
        TableColumn<ARoom, String> roomIdCol = new TableColumn<>("Room ID");
        roomIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoomId()));

        TableColumn<ARoom, RoomType> roomTypeCol = new TableColumn<>("Type");
        roomTypeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getRoomType()));

        TableColumn<ARoom, Double> priceCol = new TableColumn<>("Price/Night ($)");
        priceCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPricePerNight()).asObject());
        priceCol.setCellFactory(column -> new TableCell<ARoom, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        TableColumn<ARoom, Integer> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCapacity()).asObject());

        TableColumn<ARoom, String> availableForDatesCol = new TableColumn<>("Available for Dates");
        availableForDatesCol.setCellValueFactory(cellData -> {
            ARoom room = cellData.getValue();
            LocalDate checkIn = bookingCheckInDatePicker.getValue();
            LocalDate checkOut = bookingCheckOutDatePicker.getValue();
            String availabilityStatus;

            if (checkIn != null && checkOut != null) {
                if (!checkOut.isAfter(checkIn)) {
                    availabilityStatus = "Invalid Dates";
                } else {
                    boolean isAvailable = hotelManager.isRoomAvailable(room, checkIn, checkOut);
                    availabilityStatus = isAvailable ? "Yes" : "No";
                }
            } else {
                availabilityStatus = "Select Dates";
            }
            return new SimpleStringProperty(availabilityStatus);
        });

        roomTableView.getColumns().setAll(roomIdCol, roomTypeCol, priceCol, capacityCol, availableForDatesCol);
        roomTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // REMOVED sortRoomList method

    private void refreshRoomList() {
        roomObservableList.setAll(hotelManager.getAllRooms()); // Get all rooms
        // Always sort by the natural order defined in ARoom.compareTo() (which is Room ID)
        FXCollections.sort(roomObservableList);
        roomTableView.refresh(); // Ensure visual update for all cells
    }

    private void handleBookSelectedRoom() {
        ARoom selectedRoom = roomTableView.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a room to book.");
            return;
        }

        LocalDate desiredCheckIn = bookingCheckInDatePicker.getValue();
        LocalDate desiredCheckOut = bookingCheckOutDatePicker.getValue();

        if (desiredCheckIn == null || desiredCheckOut == null) {
            showAlert(Alert.AlertType.ERROR, "Date Missing", "Please select both check-in and check-out dates.");
            return;
        }
        if (!desiredCheckOut.isAfter(desiredCheckIn)) {
            showAlert(Alert.AlertType.ERROR, "Date Error", "Check-out date must be after check-in date.");
            return;
        }

        if (!hotelManager.isRoomAvailable(selectedRoom, desiredCheckIn, desiredCheckOut)) {
            showAlert(Alert.AlertType.ERROR, "Room Unavailable",
                    "Room " + selectedRoom.getRoomId() + " is not available for the selected dates: " +
                    desiredCheckIn + " to " + desiredCheckOut);
            return;
        }

        try {
            Booking booking = hotelManager.createBooking(currentGuest, selectedRoom.getRoomId(), desiredCheckIn, desiredCheckOut);
            showAlert(Alert.AlertType.INFORMATION, "Booking Successful",
                    "Booking " + booking.getBookingId() + " confirmed for room " + selectedRoom.getRoomId() +
                    " from " + desiredCheckIn + " to " + desiredCheckOut);
            refreshRoomList(); // Refresh and re-sort
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showAlert(Alert.AlertType.ERROR, "Booking Failed", ex.getMessage());
        }
    }

    private void handleManageBookings(Stage ownerStage) {
        Stage bookingsStage = new Stage();
        bookingsStage.initModality(Modality.WINDOW_MODAL);
        bookingsStage.initOwner(ownerStage);
        bookingsStage.setTitle("Manage All Bookings");

        BorderPane bookingsLayout = new BorderPane();
        bookingsLayout.setPadding(new Insets(10));

        TableView<Booking> bookingsTableView = new TableView<>();
        // Bookings don't have a natural sort order in this example, displayed as added
        ObservableList<Booking> bookingsObservableList = FXCollections.observableArrayList(hotelManager.getAllBookings());
        bookingsTableView.setItems(bookingsObservableList);

        TableColumn<Booking, String> bIdCol = new TableColumn<>("Booking ID");
        bIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookingId()));
        TableColumn<Booking, String> bGuestCol = new TableColumn<>("Guest Name");
        bGuestCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGuest().getName()));
        TableColumn<Booking, String> bRoomCol = new TableColumn<>("Room ID");
        bRoomCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoom().getRoomId()));
        TableColumn<Booking, LocalDate> bCheckInCol = new TableColumn<>("Check-in");
        bCheckInCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCheckInDate()));
        TableColumn<Booking, LocalDate> bCheckOutCol = new TableColumn<>("Check-out");
        bCheckOutCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCheckOutDate()));
        TableColumn<Booking, BookingStatus> bStatusCol = new TableColumn<>("Status");
        bStatusCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus()));

        bookingsTableView.getColumns().setAll(bIdCol, bGuestCol, bRoomCol, bCheckInCol, bCheckOutCol, bStatusCol);
        bookingsTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        bookingsLayout.setCenter(bookingsTableView);

        Button cancelSelectedBookingButton = new Button("Cancel Selected Booking");
        cancelSelectedBookingButton.setOnAction(e -> {
            Booking selectedBooking = bookingsTableView.getSelectionModel().getSelectedItem();
            if (selectedBooking == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to cancel.");
                return;
            }
            if (selectedBooking.getStatus() != BookingStatus.CANCELLED) {
                try {
                    hotelManager.cancelBooking(selectedBooking.getBookingId());
                    showAlert(Alert.AlertType.INFORMATION, "Cancellation Successful", "Booking " + selectedBooking.getBookingId() + " has been cancelled.");
                    bookingsObservableList.setAll(hotelManager.getAllBookings());
                    refreshRoomList(); // Refresh main list as availability might change and to re-sort
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Cancellation Failed", ex.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Already Cancelled", "Booking " + selectedBooking.getBookingId() + " was already cancelled.");
            }
        });

        HBox bottomButtonBox = new HBox(10, cancelSelectedBookingButton);
        bottomButtonBox.setAlignment(Pos.CENTER_RIGHT);
        bottomButtonBox.setPadding(new Insets(10, 0, 0, 0));
        bookingsLayout.setBottom(bottomButtonBox);

        Scene bookingsScene = new Scene(bookingsLayout, 700, 450);
        bookingsStage.setScene(bookingsScene);
        bookingsStage.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
