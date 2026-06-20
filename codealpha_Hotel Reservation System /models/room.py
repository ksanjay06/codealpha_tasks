from enum import Enum
from dataclasses import dataclass


class RoomCategory(Enum):
    STANDARD = "Standard"
    DELUXE = "Deluxe"
    SUITE = "Suite"

    @property
    def base_price(self) -> float:
        prices = {
            RoomCategory.STANDARD: 79.99,
            RoomCategory.DELUXE: 149.99,
            RoomCategory.SUITE: 299.99,
        }
        return prices[self]


@dataclass
class Room:
    room_id: int
    room_number: str
    category: RoomCategory
    capacity: int
    price_per_night: float
    is_active: bool = True

    def __str__(self):
        return (f"Room {self.room_number} [{self.category.value}] "
                f"- Capacity: {self.capacity} - ${self.price_per_night:.2f}/night")
