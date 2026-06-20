from dataclasses import dataclass


@dataclass
class Guest:
    guest_id: int
    name: str
    email: str
    phone: str = ""

    def __str__(self):
        return f"{self.name} <{self.email}>"
