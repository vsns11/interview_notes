package lld.ch15.moviebooking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

enum SeatType { REGULAR, PREMIUM, ACCESSIBLE, EMERGENCY_EXIT, OTHER }

record City(String name, String state) {}

/** The physical seat bolted to the floor. It has NO booking state: that depends on which show you ask about. */
record HallSeat(String id, SeatType type) {}

final class CinemaHall {
    private final String name;
    private final List<HallSeat> seats;
    private final List<Show> shows = new ArrayList<>();

    CinemaHall(String name, List<HallSeat> seats) { this.name = name; this.seats = List.copyOf(seats); }
    String name()            { return name; }
    List<HallSeat> seats()   { return seats; }

    /** A hall runs one show at a time, so an overlapping show is refused. */
    synchronized boolean addShow(Show show) {
        for (Show s : shows) if (s.overlaps(show)) return false;
        shows.add(show);
        return true;
    }
    synchronized List<Show> shows() { return List.copyOf(shows); }
}

final class Cinema {
    private final String name;
    private final City city;                       // association: the city exists without the cinema
    private final List<CinemaHall> halls = new ArrayList<>();   // composition: halls die with the cinema
    Cinema(String name, City city) { this.name = name; this.city = city; }
    String name()                 { return name; }
    City city()                   { return city; }
    void addHall(CinemaHall h)    { halls.add(h); }
    List<CinemaHall> halls()      { return Collections.unmodifiableList(halls); }
}

record Movie(String id, String title, String language, String genre, LocalDate releaseDate, int durationMinutes) {}

/** One screening. Owns one ShowSeat per physical seat, so the same seat can be free at 6pm and taken at 9pm. */
final class Show {
    private final String id;
    private final Movie movie;
    private final Cinema cinema;
    private final CinemaHall hall;
    private final LocalDateTime start;
    private final Map<String, ShowSeat> seats = new LinkedHashMap<>();

    Show(String id, Movie movie, Cinema cinema, CinemaHall hall, LocalDateTime start, Map<SeatType, BigDecimal> prices) {
        this.id = id; this.movie = movie; this.cinema = cinema; this.hall = hall; this.start = start;
        for (HallSeat hs : hall.seats())
            seats.put(hs.id(), new ShowSeat(hs, prices.getOrDefault(hs.type(), BigDecimal.ZERO)));
    }
    String id()                 { return id; }
    Movie movie()               { return movie; }
    Cinema cinema()             { return cinema; }
    CinemaHall hall()           { return hall; }
    LocalDateTime start()       { return start; }
    LocalDateTime end()         { return start.plusMinutes(movie.durationMinutes()); }
    Map<String, ShowSeat> seats() { return Collections.unmodifiableMap(seats); }

    ShowSeat seat(String seatId) {
        ShowSeat s = seats.get(seatId);
        if (s == null) throw new IllegalArgumentException("no seat " + seatId + " in show " + id);
        return s;
    }
    boolean overlaps(Show o) { return start.isBefore(o.end()) && o.start().isBefore(end()); }
}

/** A seat for one show: its own price and its booked flag. Only SeatLockProvider flips `booked`. */
final class ShowSeat {
    private final HallSeat seat;
    private final BigDecimal price;          // money: BigDecimal, never double
    private volatile boolean booked;
    ShowSeat(HallSeat seat, BigDecimal price) { this.seat = seat; this.price = price; }
    HallSeat seat()        { return seat; }
    BigDecimal price()     { return price; }
    boolean isBooked()     { return booked; }
    void setBooked(boolean b) { booked = b; }
}
