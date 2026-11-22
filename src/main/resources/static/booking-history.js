
async function loadBookings() {

    const bookingList = document.getElementById("bookingList");
    const message = document.getElementById("message");

    message.textContent = "Loading bookings...";
    bookingList.innerHTML = "";

    try {

        const response = await fetch("/api/bookings");

        if (!response.ok) {
            throw new Error("Unable to load bookings");
        }

        const bookings = await response.json();

        if (bookings.length === 0) {
            message.textContent = "No bookings found.";
            return;
        }

        message.textContent = "Booking history loaded successfully!";

        bookings.forEach(booking => {

            const card = document.createElement("div");

            card.className = "history-card";

            const statusClass =
                booking.status === "CONFIRMED"
                ? "confirmed"
                : "cancelled";

            card.innerHTML = `
                <h3>PNR: ${booking.pnr}</h3>

                <p><strong>Passenger:</strong>
                    ${booking.passengerName}</p>

                <p><strong>Train:</strong>
                    ${booking.trainName}</p>

                <p><strong>Train Number:</strong>
                    ${booking.trainNumber}</p>

                <p><strong>Booking Date:</strong>
                    ${new Date(booking.bookingDate).toLocaleString()}</p>

                <p><strong>Status:</strong>
                    <span class="status ${statusClass}">
                        ${booking.status}
                    </span>
                </p>
            `;

            bookingList.appendChild(card);

        });

    } catch (error) {

        message.textContent = error.message;
        message.style.color = "red";

    }
}

window.onload = loadBookings;
