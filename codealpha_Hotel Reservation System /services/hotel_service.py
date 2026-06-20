from datetime import date
from typing import List, Optional

from models import Room, RoomCategory, Guest, Reservation, ReservationStatus, PaymentStatus
from storage import Database
from services.payment import PaymentSimulator


class ReservationError(Exception):
    pass


class HotelService:
    """Facade coordinating rooms, guests, reservations, and payments."""

    def __init__(self, db: Database):
        self.db = db

    
    def add_room(self, room_number: str, category: RoomCategory, capacity: int,
                 price_per_night: Optional[float] = None) -> Room:
        price = price_per_night if price_per_night is not None else category.base_price
        room_id = self.db.add_room(room_number, category, capacity, price)
        return self.db.get_room(room_id)

    def list_rooms(self) -> List[Room]:
        return self.db.get_all_rooms()

    def search_available_rooms(self, check_in: date, check_out: date,
                                category: Optional[RoomCategory] = None) -> List[Room]:
        if check_in >= check_out:
            raise ReservationError("check_in must be before check_out")

        rooms = self.db.get_all_rooms()
        if category:
            rooms = [r for r in rooms if r.category == category]

        available = [r for r in rooms if self._is_room_free(r.room_id, check_in, check_out)]
        return available

    def _is_room_free(self, room_id: int, check_in: date, check_out: date) -> bool:
        existing = self.db.get_reservations_for_room(room_id)
        for res in existing:
            if check_in < res.check_out and check_out > res.check_in:
                return False  # overlap
        return True

   
    def register_or_get_guest(self, name: str, email: str, phone: str = "") -> Guest:
        existing = self.db.get_guest_by_email(email)
        if existing:
            return existing
        guest_id = self.db.add_guest(name, email, phone)
        return self.db.get_guest(guest_id)

    
    def book_room(self, guest: Guest, room: Room, check_in: date, check_out: date,
                  card_number: str = "4111111111111111") -> Reservation:
        if not self._is_room_free(room.room_id, check_in, check_out):
            raise ReservationError(f"Room {room.room_number} is not available for the selected dates.")

        nights = (check_out - check_in).days
        total_price = round(nights * room.price_per_night, 2)

        reservation_id = self.db.add_reservation(
            guest.guest_id, room.room_id, check_in, check_out,
            total_price, ReservationStatus.CONFIRMED, PaymentStatus.PENDING,
        )

        payment_result = PaymentSimulator.charge(total_price, card_number)
        if payment_result["success"]:
            self.db.update_reservation_status(reservation_id, payment_status=PaymentStatus.PAID)
        else:
            self.db.update_reservation_status(
                reservation_id,
                status=ReservationStatus.CANCELLED,
                payment_status=PaymentStatus.FAILED,
            )
            raise ReservationError(
                f"Booking failed: {payment_result['message']} (txn {payment_result['transaction_id']})"
            )

        return self.db.get_reservation(reservation_id)

    
    def cancel_reservation(self, reservation_id: int) -> Reservation:
        reservation = self.db.get_reservation(reservation_id)
        if not reservation:
            raise ReservationError("Reservation not found.")
        if reservation.status == ReservationStatus.CANCELLED:
            raise ReservationError("Reservation already cancelled.")

        if reservation.payment_status == PaymentStatus.PAID:
            PaymentSimulator.refund(reservation.total_price, transaction_id="N/A")
            self.db.update_reservation_status(
                reservation_id, status=ReservationStatus.CANCELLED, payment_status=PaymentStatus.REFUNDED
            )
        else:
            self.db.update_reservation_status(reservation_id, status=ReservationStatus.CANCELLED)

        return self.db.get_reservation(reservation_id)

    
    def get_booking_details(self, reservation_id: int) -> dict:
        reservation = self.db.get_reservation(reservation_id)
        if not reservation:
            raise ReservationError("Reservation not found.")
        room = self.db.get_room(reservation.room_id)
        guest = self.db.get_guest(reservation.guest_id)
        return {"reservation": reservation, "room": room, "guest": guest}

    def get_guest_reservations(self, guest_id: int) -> List[Reservation]:
        return self.db.get_reservations_for_guest(guest_id)

    def get_all_reservations(self) -> List[Reservation]:
        return self.db.get_all_reservations()
