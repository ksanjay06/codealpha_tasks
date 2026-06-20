 🏨 Hotel Reservation System

A command-line hotel reservation system built with Python, demonstrating clean **OOP design** and **SQLite-backed persistence**. Search rooms, make bookings, simulate payments, and manage cancellations — all from the terminal.

## Features

- **Room categorization** — Standard, Deluxe, Suite (each with capacity & default pricing)
- **Search & availability** — filter by date range and category; double-booking is prevented
- **Booking & cancellation** — full reservation lifecycle with status tracking
- **Simulated payments** — randomized success/failure, refunds on cancellation
- **Booking details view** — see room, guest, and reservation info together
- **Persistent storage** — SQLite database (`data/hotel.db`), survives restarts

## Architecture


hotel-reservation-system/
├── main.py                  # CLI entry point
├── models/                  # Domain entities (OOP)
│   ├── room.py               # Room + RoomCategory enum
│   ├── guest.py               # Guest
│   └── reservation.py          # Reservation + status enums
├── services/                # Business logic
│   ├── hotel_service.py        # Search/book/cancel orchestration
│   └── payment.py              # Payment simulator
├── storage/                 # Persistence layer
│   └── db.py                   # SQLite wrapper (schema + CRUD)
└── data/                     # SQLite database file lives here
```

The system follows a layered design:

- **Models** — plain dataclasses representing core entities
- **Storage** — a `Database` class that owns all SQL, returning model objects
- **Services** — a `HotelService` facade containing all business rules (availability checks, pricing, payment orchestration)
- **CLI** — a thin presentation layer calling into `HotelService`

## Getting Started

```bash
git clone https://github.com/yourusername/hotel-reservation-system.git
cd hotel-reservation-system
python main.py
```

No external dependencies — uses only the Python standard library (`sqlite3`, `dataclasses`, `enum`).

On first run, five sample rooms are seeded automatically across all three categories.

## Usage

```
==================== HOTEL RESERVATION SYSTEM ====================
1. Search available rooms
2. Book a room
3. Cancel a reservation
4. View booking details
5. View my reservations
6. List all rooms
7. List all reservations (admin)
0. Exit
====================================================================
```

### Example flow

1. **Search** for rooms between two dates, optionally filtered by category.
2. **Book** a room using its ID from the search results — a guest profile is created (or matched by email), and payment is simulated.
3. **View details** of any reservation by ID.
4. **Cancel** a reservation — if it was paid, a simulated refund is issued automatically.

## Data Model

| Entity | Fields |
|---|---|
| `Room` | id, number, category, capacity, price/night, active flag |
| `Guest` | id, name, email, phone |
| `Reservation` | id, guest, room, check-in/out, total price, status, payment status |

**Reservation status:** `Confirmed → Checked-In → Checked-Out` or `Cancelled`
**Payment status:** `Pending → Paid → Refunded`, or `Failed`

## Extending

- Swap `services/payment.py` for a real gateway (Stripe, etc.) — the interface (`charge`/`refund`) is already gateway-agnostic.
- Swap `storage/db.py` for PostgreSQL/MySQL by changing the connection layer; the rest of the app talks only to `Database`, not to SQL directly.
- Add a web layer (Flask/FastAPI) on top of `HotelService` without touching business logic.

## License

MIT


