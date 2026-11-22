
async function searchBooking() {

    const pnr = document.getElementById("pnrInput").value.trim();
    const message = document.getElementById("bookingMessage");
    const bookingDetails = document.getElementById("bookingResult");

    if (!/^\d{10}$/.test(pnr)) {
        message.textContent = "Please enter a valid 10-digit PNR.";
        message.style.color = "red";
        bookingDetails.innerHTML = "";
        return;
    }

    message.textContent = "Searching your tickets...";
    message.style.color = "blue";
    bookingDetails.innerHTML = "";

    try {

        const response = await fetch(`/api/bookings/${pnr}/group`);

        const bookings = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(
                bookings?.message ||
                bookings?.error ||
                "Booking not found. Please check your PNR."
            );
        }

        if (!Array.isArray(bookings) || bookings.length === 0) {
            throw new Error("No tickets found for this PNR.");
        }

        displayBookings(bookings);

        message.textContent = "Ticket details found successfully!";
        message.style.color = "green";

    } catch (error) {

        message.textContent = error.message;
        message.style.color = "red";
    }
}


// DISPLAY ALL PASSENGERS UNDER ONE PNR
function displayBookings(bookings) {

    const bookingDetails = document.getElementById("bookingResult");
    bookingDetails.innerHTML = "";

    const card = document.createElement("div");
    card.className = "booking-card";

    const heading = document.createElement("h2");
    heading.textContent = "Group Ticket Details";
    card.appendChild(heading);

    const firstBooking = bookings[0];

    const classNames = {
        "1A": "First AC",
        "2A": "Second AC",
        "3A": "Third AC",
        "3E": "AC 3 Economy",
        "SL": "Sleeper"
    };

    function addDetail(label, value) {

        const paragraph = document.createElement("p");
        const strong = document.createElement("strong");

        strong.textContent = label + ": ";

        paragraph.appendChild(strong);
        paragraph.appendChild(
            document.createTextNode(value ?? "N/A")
        );

        card.appendChild(paragraph);
    }

    addDetail("PNR Number", firstBooking.pnr);
    addDetail("Train Name", firstBooking.trainName);
    addDetail("Train Number", firstBooking.trainNumber);

    const travelClass = firstBooking.travelClass;

    addDetail(
        "Travel Class",
        travelClass
            ? `${travelClass} - ${classNames[travelClass] || "Unknown Class"}`
            : "Not available"
    );

    addDetail(
        "Booking Date",
        firstBooking.bookingDate
            ? new Date(firstBooking.bookingDate).toLocaleString()
            : "N/A"
    );

    const passengerHeading = document.createElement("h3");

    passengerHeading.textContent =
        `Passengers (${bookings.length})`;

    card.appendChild(passengerHeading);

    bookings.forEach((booking, index) => {

        const passengerBox = document.createElement("div");
        passengerBox.className = "passenger-ticket";

        const name = document.createElement("h4");

        name.textContent =
            `${index + 1}. ${booking.passengerName ?? "Passenger"}`;

        passengerBox.appendChild(name);

        function addPassengerDetail(label, value) {

            const paragraph = document.createElement("p");

            paragraph.textContent =
                `${label}: ${value ?? "N/A"}`;

            passengerBox.appendChild(paragraph);
        }

        addPassengerDetail("Seat Number", booking.seatNumber);
        addPassengerDetail("RAC Number", booking.racNumber);
        addPassengerDetail("Waiting List Number", booking.waitingNumber);
        addPassengerDetail("Travel Class", booking.travelClass);
        addPassengerDetail("Booking Status", booking.status);

        card.appendChild(passengerBox);
    });


    // CANCELLATION BUTTON
    const activeBookings = bookings.filter(
        booking =>
            booking.status === "CONFIRMED" ||
            booking.status === "RAC" ||
            booking.status === "WAITING"
    );

    const cancelButton = document.createElement("button");

    cancelButton.id = "cancelButton";

    if (activeBookings.length === 0) {

        cancelButton.textContent = "All Tickets Cancelled";
        cancelButton.disabled = true;

    } else {

        cancelButton.textContent = "Cancel Entire Booking";
        cancelButton.onclick = cancelBooking;
    }

    card.appendChild(cancelButton);

    bookingDetails.appendChild(card);
}


// CANCEL ALL TICKETS UNDER THE PNR
async function cancelBooking() {

    const pnr = document.getElementById("pnrInput").value.trim();

    const confirmCancel = confirm(
        "Are you sure you want to cancel ALL tickets under this PNR?"
    );

    if (!confirmCancel) {
        return;
    }

    const message = document.getElementById("bookingMessage");
    const cancelButton = document.getElementById("cancelButton");

    cancelButton.disabled = true;

    message.textContent = "Cancelling all tickets...";
    message.style.color = "blue";

    try {

        const response = await fetch(
            `/api/bookings/${pnr}/cancel`,
            { method: "POST" }
        );

        const result = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(
                result?.message ||
                result?.error ||
                "Unable to cancel tickets."
            );
        }

        if (!Array.isArray(result)) {
            throw new Error("Unexpected cancellation response.");
        }

        displayBookings(result);

        message.textContent =
            "Cancellation request completed successfully.";

        message.style.color = "green";

    } catch (error) {

        message.textContent = error.message;
        message.style.color = "red";

        cancelButton.disabled = false;
    }
}
