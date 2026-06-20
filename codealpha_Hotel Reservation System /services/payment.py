import random
import time
import uuid


class PaymentSimulator:
    """Simulates a payment gateway. No real money/network involved."""

    SUCCESS_RATE = 0.92  

    @staticmethod
    def charge(amount: float, card_number: str = "4111111111111111") -> dict:
        time.sleep(0.3)  
        transaction_id = str(uuid.uuid4())[:12]
        success = random.random() <= PaymentSimulator.SUCCESS_RATE

        return {
            "transaction_id": transaction_id,
            "amount": amount,
            "card_last4": card_number[-4:],
            "success": success,
            "message": "Payment approved" if success else "Payment declined - insufficient funds",
        }

    @staticmethod
    def refund(amount: float, transaction_id: str) -> dict:
        time.sleep(0.2)
        return {
            "refund_id": str(uuid.uuid4())[:12],
            "original_transaction": transaction_id,
            "amount": amount,
            "success": True,
            "message": "Refund processed",
        }
