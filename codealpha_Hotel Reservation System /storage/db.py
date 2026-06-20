import os
import sqlite3
from datetime import date
from contextlib import contextmanager

from models import Room, RoomCategory, Guest, Reservation, ReservationStatus, PaymentStatus

DB_PATH = "data/hotel.db"

SCHEMA = """
CREATE TABLE IF NOT EXISTS rooms (
    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
    room_number TEXT UNIQUE NOT NULL,
    category TEXT NOT NULL,
    capacity INTEGER NOT NULL,
    price_per_night REAL NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS guests (
    guest_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INTEGER PRIMARY KEY AUTOINCREMENT,
    guest_id INTEGER NOT NULL,
    room_id INTEGER NOT NULL,
    check_in TEXT NOT NULL,
    check_out TEXT NOT NULL,
    total_price REAL NOT NULL,
    status TEXT NOT NULL,
    payment_status TEXT NOT NULL,
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);
"""


class Database:
    """Thin SQLite wrapper handling schema setup and connections."""

    def __init__(self, db_path: str = DB_PATH):
        self.db_path = db_path
        dirname = os.path.dirname(db_path)
        if dirname:
            os.makedirs(dirname, exist_ok=True)
        self._init_schema()

    @contextmanager
    def connect(self):
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA foreign_keys = ON;")
        try:
            yield conn
            conn.commit()
        finally:
            conn.close()

    def _init_schema(self):
        with self.connect() as conn:
            conn.executescript(SCHEMA)

    # ---------- Rooms ----------
    def add_room(self, room_number, category: RoomCategory, capacity, price_per_night):
        with self.connect() as conn:
            cur = conn.execute(
                "INSERT INTO rooms (room_number, category, capacity, price_per_night, is_active) "
                "VALUES (?, ?, ?, ?, 1)",
                (room_number, category.value, capacity, price_per_night),
            )
            return cur.lastrowid

    def get_all_rooms(self):
        with self.connect() as conn:
            rows = conn.execute("SELECT * FROM rooms WHERE is_active = 1").fetchall()
            return [self._row_to_room(r) for r in rows]

    def get_room(self, room_id):
        with self.connect() as conn:
            row = conn.execute("SELECT * FROM rooms WHERE room_id = ?", (room_id,)).fetchone()
            return self._row_to_room(row) if row else None

    @staticmethod
    def _row_to_room(row):
        return Room(
            room_id=row["room_id"],
            room_number=row["room_number"],
            category=RoomCategory(row["category"]),
            capacity=row["capacity"],
            price_per_night=row["price_per_night"],
            is_active=bool(row["is_active"]),
        )

    # ---------- Guests ----------
    def add_guest(self, name, email, phone=""):
        with self.connect() as conn:
            cur = conn.execute(
                "INSERT INTO guests (name, email, phone) VALUES (?, ?, ?)",
                (name, email, phone),
            )
            return cur.lastrowid

    def get_guest_by_email(self, email):
        with self.connect() as conn:
            row = conn.execute("SELECT * FROM guests WHERE email = ?", (email,)).fetchone()
            return self._row_to_guest(row) if row else None

    def get_guest(self, guest_id):
        with self.connect() as conn:
            row = conn.execute("SELECT * FROM guests WHERE guest_id = ?", (guest_id,)).fetchone()
            return self._row_to_guest(row) if row else None

    @staticmethod
    def _row_to_guest(row):
        return Guest(guest_id=row["guest_id"], name=row["name"], email=row["email"], phone=row["phone"] or "")

    # ---------- Reservations ----------
    def add_reservation(self, guest_id, room_id, check_in: date, check_out: date,
                         total_price, status: ReservationStatus, payment_status: PaymentStatus):
        with self.connect() as conn:
            cur = conn.execute(
                "INSERT INTO reservations (guest_id, room_id, check_in, check_out, total_price, status, payment_status) "
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                (guest_id, room_id, check_in.isoformat(), check_out.isoformat(),
                 total_price, status.value, payment_status.value),
            )
            return cur.lastrowid

    def update_reservation_status(self, reservation_id, status: ReservationStatus = None,
                                   payment_status: PaymentStatus = None):
        with self.connect() as conn:
            if status:
                conn.execute("UPDATE reservations SET status = ? WHERE reservation_id = ?",
                             (status.value, reservation_id))
            if payment_status:
                conn.execute("UPDATE reservations SET payment_status = ? WHERE reservation_id = ?",
                             (payment_status.value, reservation_id))

    def get_reservation(self, reservation_id):
        with self.connect() as conn:
            row = conn.execute("SELECT * FROM reservations WHERE reservation_id = ?",
                                (reservation_id,)).fetchone()
            return self._row_to_reservation(row) if row else None

    def get_reservations_for_room(self, room_id):
        with self.connect() as conn:
            rows = conn.execute(
                "SELECT * FROM reservations WHERE room_id = ? AND status != ?",
                (room_id, ReservationStatus.CANCELLED.value),
            ).fetchall()
            return [self._row_to_reservation(r) for r in rows]

    def get_reservations_for_guest(self, guest_id):
        with self.connect() as conn:
            rows = conn.execute(
                "SELECT * FROM reservations WHERE guest_id = ? ORDER BY reservation_id DESC", (guest_id,)
            ).fetchall()
            return [self._row_to_reservation(r) for r in rows]

    def get_all_reservations(self):
        with self.connect() as conn:
            rows = conn.execute("SELECT * FROM reservations ORDER BY reservation_id DESC").fetchall()
            return [self._row_to_reservation(r) for r in rows]

    @staticmethod
    def _row_to_reservation(row):
        return Reservation(
            reservation_id=row["reservation_id"],
            guest_id=row["guest_id"],
            room_id=row["room_id"],
            check_in=date.fromisoformat(row["check_in"]),
            check_out=date.fromisoformat(row["check_out"]),
            total_price=row["total_price"],
            status=ReservationStatus(row["status"]),
            payment_status=PaymentStatus(row["payment_status"]),
        )
