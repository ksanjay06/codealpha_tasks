"""
Hotel Reservation System - CLI
Run with: python main.py
"""
from datetime import date, datetime

from models import RoomCategory
from storage import Database
from services import HotelService, ReservationError


def parse_date(s: str) -> date:
    return datetime.strptime(s.strip(), "%Y-%m-%d").date()


def seed_rooms_if_empty(service: HotelService):
    if service.list_rooms():
        return
    print("Seeding initial rooms...")
    layout = [
        ("101", RoomCategory.STANDARD, 2),
        ("102", RoomCategory.STANDARD, 2),
        ("201", RoomCategory.DELUXE, 3),
        ("202", RoomCategory.DELUXE, 3),
        ("301", RoomCategory.SUITE, 4),
    ]
    for number, category, capacity in layout:
        service.add_room(number, category, capacity)


def print_menu():
    print("""
==================== HOTEL RESERVATION SYSTEM ====================
1. Search available rooms
2. Book a room
3. Cancel a reservation
4. View booking details
5. View my reservations
6. List all rooms
7. List all reservations (admin)
0. Exit
====================================================================""")


def action_search(service: HotelService):
    try:
        check_in = parse_date(input("Check-in (YYYY-MM-DD): "))
        check_out = parse_date(input("Check-out (YYYY-MM-DD): "))
        print("Category filter - 1) Standard 2) Deluxe 3) Suite 4) Any")
        choice = input("Choice [4]: ").strip() or "4"
        category_map = {"1": RoomCategory.STANDARD, "2": RoomCategory.DELUXE, "3": RoomCategory.SUITE}
        category = category_map.get(choice)

        rooms = service.search_available_rooms(check_in, check_out, category)
        if not rooms:
            print("No rooms available for those dates/category.")
            return
        print(f"\nAvailable rooms ({check_in} -> {check_out}):")
        for r in rooms:
            print(f"  [{r.room_id}] {r}")
    except (ValueError, ReservationError) as e:
        print(f"Error: {e}")


def action_book(service: HotelService):
    try:
        name = input("Your name: ").strip()
        email = input("Your email: ").strip()
        phone = input("Phone (optional): ").strip()
        guest = service.register_or_get_guest(name, email, phone)

        room_id = int(input("Room ID to book (use 'Search' first): ").strip())
        check_in = parse_date(input("Check-in (YYYY-MM-DD): "))
        check_out = parse_date(input("Check-out (YYYY-MM-DD): "))

        room = service.db.get_room(room_id)
        if not room:
            print("Invalid room ID.")
            return

        card = input("Card number (simulated, any digits) [4111111111111111]: ").strip() or "4111111111111111"

        reservation = service.book_room(guest, room, check_in, check_out, card)
        print("\n✅ Booking confirmed!")
        print(reservation)
        print(f"Guest: {guest}")
    except (ValueError, ReservationError) as e:
        print(f"❌ Booking failed: {e}")


def action_cancel(service: HotelService):
    try:
        reservation_id = int(input("Reservation ID to cancel: ").strip())
        reservation = service.cancel_reservation(reservation_id)
        print("Reservation cancelled.")
        print(reservation)
    except (ValueError, ReservationError) as e:
        print(f"Error: {e}")


def action_view_details(service: HotelService):
    try:
        reservation_id = int(input("Reservation ID: ").strip())
        details = service.get_booking_details(reservation_id)
        print("\n----- Booking Details -----")
        print(details["reservation"])
        print(details["room"])
        print(details["guest"])
    except (ValueError, ReservationError) as e:
        print(f"Error: {e}")


def action_my_reservations(service: HotelService):
    email = input("Your email: ").strip()
    guest = service.db.get_guest_by_email(email)
    if not guest:
        print("No guest found with that email.")
        return
    reservations = service.get_guest_reservations(guest.guest_id)
    if not reservations:
        print("No reservations found.")
        return
    for r in reservations:
        print(r)


def action_list_rooms(service: HotelService):
    for r in service.list_rooms():
        print(f"  [{r.room_id}] {r}")


def action_list_all_reservations(service: HotelService):
    reservations = service.get_all_reservations()
    if not reservations:
        print("No reservations yet.")
        return
    for r in reservations:
        print(r)


def main():
    db = Database()
    service = HotelService(db)
    seed_rooms_if_empty(service)

    actions = {
        "1": action_search,
        "2": action_book,
        "3": action_cancel,
        "4": action_view_details,
        "5": action_my_reservations,
        "6": action_list_rooms,
        "7": action_list_all_reservations,
    }

    while True:
        print_menu()
        choice = input("Select an option: ").strip()
        if choice == "0":
            print("Goodbye!")
            break
        action = actions.get(choice)
        if action:
            action(service)
        else:
            print("Invalid option.")


if __name__ == "__main__":
    main()
