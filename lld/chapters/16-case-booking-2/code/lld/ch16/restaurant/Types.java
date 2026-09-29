package lld.ch16.restaurant;

import java.math.BigDecimal;

enum TableStatus { FREE, RESERVED, OCCUPIED }
enum SeatType { REGULAR, KID, ACCESSIBLE }
enum ReservationStatus { CONFIRMED, CHECKED_IN, CANCELLED, NO_SHOW }
enum OrderStatus { RECEIVED, PREPARING, READY, SERVED, CANCELLED }
enum PaymentStatus { COMPLETED, PENDING, DECLINED }

record MenuItem(String id, String title, BigDecimal price) {}
record Customer(String name, String phone) {}
record TableSeat(int number, SeatType type) {}
