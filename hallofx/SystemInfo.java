/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package hallofx;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
// No java.util.Comparator import needed here
import java.util.List;
import java.util.Objects;

// -------------- ENUMERATIONS --------------
enum RoomType {
    STANDARD, DELUXE, SUITE
}

enum BookingStatus {
    CONFIRMED, CANCELLED
}

// -------------- ABSTRACT ROOM CLASS --------------
abstract class ARoom implements Comparable<ARoom> {
    protected String roomId;
    protected RoomType roomType;
    protected double pricePerNight;
    protected int capacity;
    protected boolean isInService;

    public ARoom(String roomId, RoomType roomType, double pricePerNight, int capacity) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.isInService = true;
    }

    public String getRoomId() { return roomId; }
    public RoomType getRoomType() { return roomType; }
    public double getPricePerNight() { return pricePerNight; }
    public int getCapacity() { return capacity; }
    public boolean isInService() { return isInService; }
    public void setInService(boolean inService) { this.isInService = inService; }

    public abstract String getRoomSpecificDetails();

    @Override
    public int compareTo(ARoom other) {
        // Natural sort order is by Room ID
        if (other == null) return 1; // Basic null check
        return this.roomId.compareTo(other.roomId);
    }

    @Override
    public String toString() {
        return "Room " + roomId + " - " + roomType + " (Service: " + (isInService ? "Yes" : "No") +
               ", $" + String.format("%.2f", pricePerNight) + "/night, Capacity: " + capacity + ")";
    }
}

// -------------- CONCRETE ROOM SUBCLASSES --------------
class StandardRoom extends ARoom {
    public StandardRoom(String roomId, double pricePerNight, int capacity) {
        super(roomId, RoomType.STANDARD, pricePerNight, capacity);
    }
    @Override 
    public String getRoomSpecificDetails() { 
        return "Standard Room features: Basic and comfortable accommodation.";
    }
}

class DeluxeRoom extends ARoom {
    public DeluxeRoom(String roomId, double pricePerNight, int capacity) { super(roomId, RoomType.DELUXE, pricePerNight, capacity); }
    @Override 
    public String getRoomSpecificDetails() { 
        return "Deluxe Room features: Enhanced amenities and more spacious.";
    }
}

class SuiteRoom extends ARoom {
    public SuiteRoom(String roomId, double pricePerNight, int capacity) { super(roomId, RoomType.SUITE, pricePerNight, capacity); }
    @Override 
    public String getRoomSpecificDetails() { 
        return "Suite features: Luxurious with separate living area and premium services.";
    }
}

// -------------- GUEST CLASS --------------
class Guest {
    private String guestId;
    private String name;
    public Guest(String guestId, String name, String email) {
        this.guestId = guestId;
        this.name = name;
    }
    public String getGuestId() { return guestId; }
    public String getName() { return name; }
    @Override public String toString() { return "Guest: " + name + " (ID: " + guestId + ")"; }
    @Override public boolean equals(Object o) { 
        if (this == o) 
            return true; 
        if (o == null || getClass() != o.getClass()) 
            return false; 
        Guest guest = (Guest) o;
        return Objects.equals(guestId, guest.guestId); 
    }
    @Override public int hashCode() { 
        return Objects.hash(guestId); 
    }
}

// -------------- BOOKING CLASS --------------
class Booking {
    private String bookingId;
    private Guest guest;
    private ARoom room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;

    public Booking(String bookingId, Guest guest, ARoom room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = BookingStatus.CONFIRMED;
        long numberOfNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (numberOfNights <= 0) throw new IllegalArgumentException("Check-out date must be after check-in date.");
    }
    public String getBookingId() { return bookingId; }
    public Guest getGuest() { return guest; }
    public ARoom getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public BookingStatus getStatus() { return status; }
    public void setBookingStatus(BookingStatus status) { this.status = status; }
    @Override public String toString() { return "Booking ID: " + bookingId + ", Guest: " + guest.getName() + ", Room: " + room.getRoomId() + ", Dates: " + checkInDate + " to " + checkOutDate + ", Status: " + status; }
}

// -------------- HOTEL MANAGEMENT CLASS --------------
class Hotel {
    private List<ARoom> rooms;
    private List<Guest> guests;
    private List<Booking> bookings;
    private int nextGuestIdSuffix = 1;
    private int nextBookingIdSuffix = 1;

    public Hotel() {
        this.rooms = new ArrayList<>();
        this.guests = new ArrayList<>();
        this.bookings = new ArrayList<>();
    }

    public ARoom findRoom(String roomId) {
        if (roomId == null) {
            return null;
        }
        for (ARoom room : this.rooms) {
            if (room.getRoomId().equals(roomId)) {
                return room;
            }
        }
        return null;
    }

    public Booking findBooking(String bookingId) {
        if (bookingId == null) {
            return null;
        }
        for (Booking booking : this.bookings) {
            if (booking.getBookingId().equals(bookingId)) {
                return booking;
            }
        }
        return null;
    }

    public void addRoom(ARoom room) {
        if (room == null || room.getRoomId() == null) {
            System.out.println("Error: Room or Room ID cannot be null.");
            return;
        }
        if (findRoom(room.getRoomId()) != null) {
            System.out.println("Error: Room with ID " + room.getRoomId() + " already exists.");
            return;
        }
        this.rooms.add(room);
        System.out.println(room.getRoomType() + " Room " + room.getRoomId() + " added. Features: " + room.getRoomSpecificDetails());
    }

    public List<ARoom> getAllRooms() {
        return new ArrayList<>(this.rooms);
    }

    public boolean isRoomAvailable(ARoom room, LocalDate desiredCheckIn, LocalDate desiredCheckOut) {
        if (room == null || desiredCheckIn == null || desiredCheckOut == null || !desiredCheckOut.isAfter(desiredCheckIn)) {
            System.out.println("Debug: Invalid parameters for isRoomAvailable or dates not logical.");
            return false;
        }
        if (!room.isInService()) {
            System.out.println("Debug: Room " + room.getRoomId() + " is not in service.");
            return false;
        }
        for (Booking booking : this.bookings) {
            if (booking.getRoom() != null && booking.getRoom().getRoomId() != null &&
                booking.getRoom().getRoomId().equals(room.getRoomId()) &&
                booking.getStatus() == BookingStatus.CONFIRMED) {
                if (desiredCheckIn.isBefore(booking.getCheckOutDate()) &&
                    desiredCheckOut.isAfter(booking.getCheckInDate())) {
                    System.out.println("Debug: Room " + room.getRoomId() + " conflicts with existing booking " + booking.getBookingId());
                    return false;
                }
            }
        }
        return true;
    }

    public Guest registerGuest(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Guest name cannot be empty.");
        }
        String guestId = "G" + nextGuestIdSuffix++;
        Guest newGuest = new Guest(guestId, name, email);
        this.guests.add(newGuest);
        System.out.println("Guest " + name + " registered with ID " + guestId);
        return newGuest;
    }


    public Booking createBooking(Guest guest, String roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        if (guest == null || roomId == null || checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException("All parameters for booking must be provided.");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }

        ARoom roomToBook = findRoom(roomId);
        if (roomToBook == null) {
            throw new IllegalArgumentException("Booking failed: Room ID " + roomId + " not found.");
        }

        if (!isRoomAvailable(roomToBook, checkInDate, checkOutDate)) {
            throw new IllegalStateException("Booking failed: Room " + roomToBook.getRoomId() + " is not available for selected dates " + checkInDate + " to " + checkOutDate + ".");
        }

        String bookingId = "B" + nextBookingIdSuffix++;
        Booking newBooking = new Booking(bookingId, guest, roomToBook, checkInDate, checkOutDate);
        this.bookings.add(newBooking);
        System.out.println("Booking " + bookingId + " created for " + guest.getName() + " in room " + roomToBook.getRoomId() +
                           " from " + checkInDate + " to " + checkOutDate);
        return newBooking;
    }

    public List<Booking> getAllBookings() {
        return new ArrayList<>(this.bookings);
    }

    public void cancelBooking(String bookingId) {
        Booking bookingToCancel = findBooking(bookingId);
        if (bookingToCancel == null) {
            throw new IllegalArgumentException("Error: Booking ID " + bookingId + " not found for cancellation.");
        }

        if (bookingToCancel.getStatus() == BookingStatus.CANCELLED) {
            System.out.println("Info: Booking " + bookingId + " is already cancelled.");
            return;
        }
        bookingToCancel.setBookingStatus(BookingStatus.CANCELLED);
        System.out.println("Booking " + bookingId + " cancelled.");
    }
}
// -------------- MAIN CLASS FOR (Minimal) PHASE 1 TESTING --------------
public class SystemInfo {
    public static void main(String[] args) {
    }
}