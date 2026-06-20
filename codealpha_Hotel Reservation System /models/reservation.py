from enum import Enum
from dataclasses import dataclass
from datetime import date


class ReservationStatus(Enum):
    CONFIRMED = "Confirmed"
    CANCELLED = "Cancelled"
    CHECKED_IN = "Checked-In"
    CHECKED_OUT = "Checked-Out"


class PaymentStatus(Enum):
    PENDING = "Pending"
    PAID = "Paid"
    REFUNDED = "Refunded"
    FAILED = "Failed"


@dataclass
class Reservation:
    reservation_id: int
    guest_id: int
    room_id: int
    check_in: date
    check_out: date
    total_price: float
    status: ReservationStatus = ReservationStatus.CONFIRMED
    payment_status: PaymentStatus = PaymentStatus.PENDING

    @property
    def nights(self) -> int:
        return (self.check_out - self.check_in).days

    def __str__(self):
        return (f"Reservation #{self.reservation_id} | Room {self.room_id} | "
                f"{self.check_in} -> {self.check_out} ({self.nights} nights) | "
                f"${self.total_price:.2f} | {self.status.value} | Payment: {self.payment_status.value}")
