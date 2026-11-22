
const passengers = JSON.parse(
    sessionStorage.getItem("passengerDetails") || "[]"
);

const trainId = sessionStorage.getItem("selectedTrainId");

const selectedClass = (
    sessionStorage.getItem("selectedClass") || ""
).toUpperCase();

const bookingDetails = document.getElementById("bookingDetails");
const confirmationMessage = document.getElementById("confirmationMessage");

let selectedTrain = null;
let bookingCompleted = false;
let farePerPassenger = 0;


// CLASS DETAILS
function getSelectedClassDetails(train) {

    const classes = {
        "1A": {
            name: "First AC",
            fare: train.firstAcFare
        },
        "2A": {
            name: "Second AC",
            fare: train.secondAcFare
        },
        "3A": {
            name: "Third AC",
            fare: train.thirdAcFare
        },
        "3E": {
            name: "AC 3 Economy",
            fare: train.acEconomyFare
        },
        "SL": {
            name: "Sleeper",
            fare: train.sleeperFare
        }
    };

    return classes[selectedClass] || null;
}


// FORMAT FARE
function formatFare(amount) {
    return "\u20B9" + Number(amount).toFixed(2);
}


// LOAD TRAIN DETAILS
async function loadTrainDetails() {

    try {

        if (!trainId) {
            throw new Error("Please select a train first.");
        }

        if (!selectedClass) {
            throw new Error("Please select a travel class first.");
        }

        if (!Array.isArray(passengers) || passengers.length === 0) {
            throw new Error("No passenger information found.");
        }

        const response = await fetch("/api/trains");

        if (!response.ok) {
            throw new Error("Unable to load train details.");
        }

        const trains = await response.json();

        selectedTrain = trains.find(
            train => String(train.id) === String(trainId)
        );

        if (!selectedTrain) {
            throw new Error("Selected train not found.");
        }

        const classDetails = getSelectedClassDetails(selectedTrain);

        if (!classDetails) {
            throw new Error("Invalid travel class selected.");
        }

        farePerPassenger = Number(classDetails.fare);

        if (!Number.isFinite(farePerPassenger) ||
            farePerPassenger <= 0) {

            throw new Error(
                "Fare is not available for the selected class."
            );
        }

        displayBookingReview();

    } catch (error) {

        bookingDetails.textContent = error.message;
        console.error(error);
    }
}


// DISPLAY BOOKING REVIEW
function displayBookingReview() {

    bookingDetails.innerHTML = "";

    if (!selectedTrain) {
        bookingDetails.textContent =
            "Loading train and fare details...";
        return;
    }

    const classDetails = getSelectedClassDetails(selectedTrain);
    const totalFare = farePerPassenger * passengers.length;

    const card = document.createElement("div");
    card.className = "train-card";

    const heading = document.createElement("h3");
    heading.textContent = "Group Booking Information";

    card.appendChild(heading);


    // TRAIN DETAILS
    const trainDetails = [
        ["Train Name", selectedTrain.trainName],
        ["Train Number", selectedTrain.trainNumber],
        [
            "Route",
            `${selectedTrain.source} \u2192 ${selectedTrain.destination}`
        ],
        [
            "Travel Class",
            `${selectedClass} - ${classDetails.name}`
        ],
        ["Total Passengers", passengers.length]
    ];

    trainDetails.forEach(([label, value]) => {

        const paragraph = document.createElement("p");

        paragraph.textContent =
            `${label}: ${value ?? "N/A"}`;

        card.appendChild(paragraph);
    });


    // FARE SUMMARY
    const fareHeading = document.createElement("h3");
    fareHeading.textContent = "Booking Fare Summary";

    card.appendChild(fareHeading);

    const fareDetails = [
        ["Fare Per Passenger", formatFare(farePerPassenger)],
        ["Number of Passengers", passengers.length]
    ];

    fareDetails.forEach(([label, value]) => {

        const paragraph = document.createElement("p");
        paragraph.textContent = `${label}: ${value}`;

        card.appendChild(paragraph);
    });

    const total = document.createElement("h2");
    total.textContent =
        `Total Amount: ${formatFare(totalFare)}`;

    total.style.color = "#198754";

    card.appendChild(total);


    // PASSENGER DETAILS
    passengers.forEach((passenger, index) => {

        const section = document.createElement("div");

        const title = document.createElement("h3");
        title.textContent = `Passenger ${index + 1}`;

        section.appendChild(title);

        const details = [
            ["Name", passenger.passengerName],
            ["Age", passenger.age],
            ["Gender", passenger.gender],
            ["Email", passenger.email],
            ["Phone", passenger.phone],
            [
                "Requested Seat",
                passenger.seatNumber || "Automatic allocation"
            ]
        ];

        details.forEach(([label, value]) => {

            const paragraph = document.createElement("p");

            paragraph.textContent =
                `${label}: ${value ?? "N/A"}`;

            section.appendChild(paragraph);
        });

        card.appendChild(section);
    });

    bookingDetails.appendChild(card);
}


// CONFIRM GROUP BOOKING
async function confirmBooking() {

    if (bookingCompleted) {
        return;
    }

    if (!Array.isArray(passengers) ||
        passengers.length === 0 ||
        !trainId) {

        alert("Please enter passenger details first.");
        return;
    }

    if (!selectedTrain || farePerPassenger <= 0) {
        alert("Train or fare details are not available.");
        return;
    }

    if (!selectedClass) {
        alert("Please select a travel class.");
        return;
    }

    const button = document.querySelector(
        'button[onclick="confirmBooking()"]'
    );

    if (button) {
        button.disabled = true;
        button.textContent = "Booking...";
    }

    const allPassengersHaveSeats = passengers.every(
        passenger =>
            passenger.seatNumber &&
            String(passenger.seatNumber).trim() !== ""
    );

    const requestedSeats = allPassengersHaveSeats
        ? passengers.map(passenger => passenger.seatNumber)
        : [];

    const bookingData = {

        trainId: Number(trainId),

        travelClass: selectedClass,

        passengers: passengers.map(passenger => ({
            passengerName: passenger.passengerName,
            age: Number(passenger.age),
            gender: passenger.gender,
            email: passenger.email,
            phone: passenger.phone
        })),

        seatNumbers: requestedSeats
    };

    try {

        const response = await fetch("/api/bookings/group", {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(bookingData)
        });

        const result = await response.json().catch(() => null);

        if (!response.ok) {
            throw new Error(
                result?.message ||
                result?.error ||
                "Group booking failed. Please try again."
            );
        }

        if (!Array.isArray(result) || result.length === 0) {
            throw new Error(
                "Invalid booking response from server."
            );
        }

        const pnr = result[0].pnr;

        if (!pnr) {
            throw new Error("PNR was not returned by the server.");
        }

        const totalFare = farePerPassenger * result.length;

        sessionStorage.setItem("bookingPNR", pnr);

        sessionStorage.setItem(
            "bookingResults",
            JSON.stringify(result)
        );

        bookingCompleted = true;


        // DISPLAY BOOKING RESULT
        confirmationMessage.innerHTML = "";

        const card = document.createElement("div");
        card.className = "train-card";

        const hasConfirmed = result.some(
            booking => booking.status === "CONFIRMED"
        );

        const hasRac = result.some(
            booking => booking.status === "RAC"
        );

        const hasWaiting = result.some(
            booking => booking.status === "WAITING"
        );

        let bookingHeading = "Group Booking Successful!";

        if (hasConfirmed && !hasRac && !hasWaiting) {
            bookingHeading = "Booking Confirmed!";
        } else if (!hasConfirmed && hasRac && !hasWaiting) {
            bookingHeading = "RAC Booking Successful!";
        } else if (!hasConfirmed && !hasRac && hasWaiting) {
            bookingHeading = "Waiting List Booking Successful!";
        }

        const heading = document.createElement("h2");
        heading.textContent = bookingHeading;

        card.appendChild(heading);

        const pnrText = document.createElement("h3");
        pnrText.textContent = `Common PNR: ${pnr}`;

        card.appendChild(pnrText);

        const classText = document.createElement("p");
        classText.textContent = `Travel Class: ${selectedClass}`;

        card.appendChild(classText);

        const count = document.createElement("p");
        count.textContent = `Total Passengers: ${result.length}`;

        card.appendChild(count);


        // PAYMENT SUMMARY
        const fareHeading = document.createElement("h3");
        fareHeading.textContent = "Payment Summary";

        card.appendChild(fareHeading);

        const fareText = document.createElement("p");
        fareText.textContent =
            `Fare Per Passenger: ${formatFare(farePerPassenger)}`;

        card.appendChild(fareText);

        const totalText = document.createElement("h2");
        totalText.textContent =
            `Total Booking Amount: ${formatFare(totalFare)}`;

        totalText.style.color = "#198754";

        card.appendChild(totalText);


        // PASSENGER BOOKING STATUS
        result.forEach((booking, index) => {

            const section = document.createElement("div");

            const title = document.createElement("h3");
            title.textContent = `Passenger ${index + 1}`;

            section.appendChild(title);

            const details = [
                ["Passenger", booking.passengerName],
                ["Train", booking.trainName],
                ["Train Number", booking.trainNumber],
                ["Travel Class", booking.travelClass],
                ["Seat Number", booking.seatNumber],
                ["Booking Status", booking.status],
                ["RAC Number", booking.racNumber],
                ["Waiting List Number", booking.waitingNumber]
            ];

            details.forEach(([label, value]) => {

                if (
                    (label === "RAC Number" ||
                     label === "Waiting List Number") &&
                    (value === null || value === undefined || value === "")
                ) {
                    return;
                }

                const paragraph = document.createElement("p");

                paragraph.textContent =
                    `${label}: ${value ?? "N/A"}`;

                section.appendChild(paragraph);
            });

            card.appendChild(section);
        });

        const message = document.createElement("p");

        message.textContent =
            "All passenger booking details have been saved successfully!";

        card.appendChild(message);

        confirmationMessage.appendChild(card);

        if (button) {
            button.textContent = "Booking Completed";
        }

    } catch (error) {

        confirmationMessage.textContent = error.message;

        if (button) {
            button.disabled = false;
            button.textContent = "Confirm Booking";
        }

        console.error(error);
    }
}


// START
loadTrainDetails();
