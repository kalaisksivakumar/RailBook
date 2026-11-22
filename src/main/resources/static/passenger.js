
const passengerContainer = document.getElementById("passengerContainer");
const addPassengerButton = document.getElementById("addPassenger");
const passengerCount = document.getElementById("passengerCount");
const passengerForm = document.getElementById("passengerForm");
const formMessage = document.getElementById("formMessage");
const coachOccupancy = document.getElementById("coachOccupancy");
const selectedClassDetails = document.getElementById("selectedClassDetails");

const trainId = sessionStorage.getItem("selectedTrainId");

const selectedClass = (
    sessionStorage.getItem("selectedClass") || ""
).toUpperCase();

let totalSeats = 0;
let classAvailableSeats = 0;
let classFare = 0;

let bookedSeats = [];
let passengers = [];

const classNames = {
    "1A": "First AC",
    "2A": "Second AC",
    "3A": "Third AC",
    "3E": "AC 3 Economy",
    "SL": "Sleeper"
};


// GET SELECTED CLASS DETAILS
function getClassDetails(train) {

    const classMap = {
        "1A": {
            name: "First AC",
            total: train.firstAcSeats,
            available: train.firstAcAvailable,
            fare: train.firstAcFare
        },

        "2A": {
            name: "Second AC",
            total: train.secondAcSeats,
            available: train.secondAcAvailable,
            fare: train.secondAcFare
        },

        "3A": {
            name: "Third AC",
            total: train.thirdAcSeats,
            available: train.thirdAcAvailable,
            fare: train.thirdAcFare
        },

        "3E": {
            name: "AC 3 Economy",
            total: train.acEconomySeats,
            available: train.acEconomyAvailable,
            fare: train.acEconomyFare
        },

        "SL": {
            name: "Sleeper",
            total: train.sleeperSeats,
            available: train.sleeperAvailable,
            fare: train.sleeperFare
        }
    };

    return classMap[selectedClass];
}


// LOAD TRAIN DETAILS
async function loadTrainSeats() {

    if (!trainId) {
        formMessage.textContent = "Please select a train first.";
        addPassengerButton.disabled = true;
        return;
    }

    if (!classNames[selectedClass]) {
        formMessage.textContent = "Please select a valid travel class.";
        addPassengerButton.disabled = true;
        return;
    }

    try {

        const trainResponse = await fetch("/api/trains");

        if (!trainResponse.ok) {
            throw new Error("Unable to load train details.");
        }

        const trains = await trainResponse.json();

        const selectedTrain = trains.find(
            train => String(train.id) === String(trainId)
        );

        if (!selectedTrain) {
            throw new Error("Selected train not found.");
        }

        const classDetails = getClassDetails(selectedTrain);

        if (!classDetails) {
            throw new Error("Unable to find selected class.");
        }

        totalSeats = Number(classDetails.total) || 0;
        classAvailableSeats = Number(classDetails.available) || 0;
        classFare = Number(classDetails.fare) || 0;

        if (totalSeats <= 0) {
            throw new Error("Seat capacity is not configured for this class.");
        }

        displaySelectedClass();

        const seatResponse = await fetch(
            `/api/bookings/train/${trainId}/seats/${selectedClass}`
        );

        if (!seatResponse.ok) {
            throw new Error("Unable to load booked seats.");
        }

        bookedSeats = await seatResponse.json();

        if (!Array.isArray(bookedSeats)) {
            throw new Error("Invalid seat information.");
        }

        renderCoachOccupancy();

        if (classAvailableSeats <= 0) {

            formMessage.textContent =
                "No confirmed seats available. You can still try RAC or Waiting List booking.";

        } else {

            formMessage.textContent =
                "Select available seats. Additional passengers may receive RAC or Waiting List status.";
        }

        addPassenger();

    } catch (error) {

        formMessage.textContent = error.message;
        addPassengerButton.disabled = true;

        if (coachOccupancy) {
            coachOccupancy.innerHTML =
                "<p>Unable to load coach occupancy.</p>";
        }

        console.error(error);
    }
}


// DISPLAY SELECTED CLASS
function displaySelectedClass() {

    if (!selectedClassDetails) return;

    selectedClassDetails.innerHTML = `
        <div class="class-card">
            <h3>${selectedClass} - ${classNames[selectedClass]}</h3>
            <p>Total Seats: ${totalSeats}</p>
            <p>Available Confirmed Seats: ${classAvailableSeats}</p>
            <p>Fare per Passenger: ₹${classFare.toFixed(2)}</p>
            <p>Booking options: Confirmed / RAC / Waiting List</p>
        </div>
    `;
}


// COACH OCCUPANCY
function renderCoachOccupancy() {

    if (!coachOccupancy) return;

    const bookedCount = bookedSeats.length;

    const percentage = totalSeats > 0
        ? Math.min(100, Math.round((bookedCount / totalSeats) * 100))
        : 0;

    let status = "Seats Available";

    if (classAvailableSeats <= 0) {
        status = "RAC / Waiting List";
    } else if (percentage >= 75) {
        status = "Filling Fast";
    }

    coachOccupancy.innerHTML = `
        <div class="coach-card">
            <h3>${selectedClass} - ${classNames[selectedClass]} Occupancy</h3>

            <div class="occupancy-details">
                <span>Total Seats: ${totalSeats}</span>
                <span>Booked Seats: ${bookedCount}</span>
                <span>Available Seats: ${classAvailableSeats}</span>
            </div>

            <div class="occupancy-bar">
                <div class="occupancy-fill" style="width: ${percentage}%"></div>
            </div>

            <p>Occupancy: ${percentage}%</p>
            <strong>${status}</strong>
        </div>
    `;
}


// COUNT CURRENTLY SELECTED SEATS
function getSelectedSeatsCount() {

    return passengers.filter(
        passenger => passenger.seatNumber
    ).length;
}


// ADD PASSENGER
function addPassenger() {

    if (passengers.length >= 6) {

        formMessage.textContent =
            "Maximum 6 passengers are allowed.";

        return;
    }

    const passenger = {
        id: Date.now() + Math.random(),
        seatNumber: ""
    };

    passengers.push(passenger);

    createPassengerCard(passenger);

    updatePassengerCount();

    formMessage.textContent = "";
}


// CREATE PASSENGER FORM
function createPassengerCard(passenger) {

    const card = document.createElement("div");

    card.className = "passenger-card";
    card.dataset.id = passenger.id;

    const heading = document.createElement("h3");

    heading.textContent = "Passenger " + passengers.length;

    card.appendChild(heading);


    function createInput(id, placeholder, type = "text") {

        const input = document.createElement("input");

        input.id = id;
        input.type = type;
        input.placeholder = placeholder;
        input.required = true;

        if (type === "number") {
            input.min = "1";
            input.max = "120";
        }

        if (id.startsWith("phone")) {
            input.pattern = "[0-9]{10}";
            input.maxLength = 10;
        }

        card.appendChild(input);

        return input;
    }


    createInput("name-" + passenger.id, "Passenger Name");

    createInput("age-" + passenger.id, "Age", "number");


    const gender = document.createElement("select");

    gender.id = "gender-" + passenger.id;
    gender.required = true;

    gender.innerHTML = `
        <option value="">Select Gender</option>
        <option value="Male">Male</option>
        <option value="Female">Female</option>
        <option value="Other">Other</option>
    `;

    card.appendChild(gender);


    createInput("email-" + passenger.id, "Email", "email");

    createInput(
        "phone-" + passenger.id,
        "10-digit Phone Number",
        "tel"
    );


    const seatHeading = document.createElement("h4");

    seatHeading.textContent = "Seat Selection";

    card.appendChild(seatHeading);


    const selectedSeatText = document.createElement("p");

    selectedSeatText.id = "selected-" + passenger.id;

    selectedSeatText.textContent =
        "Selected Seat: None (RAC / Waiting List may be assigned)";

    card.appendChild(selectedSeatText);


    const suggestButton = document.createElement("button");

    suggestButton.type = "button";
    suggestButton.className = "action-button";
    suggestButton.textContent = "Suggest a Seat";

    suggestButton.addEventListener("click", function() {

        suggestSeat(passenger);

        renderSeats();
    });

    card.appendChild(suggestButton);


    const seatContainer = document.createElement("div");

    seatContainer.className = "seat-container";
    seatContainer.id = "seats-" + passenger.id;

    card.appendChild(seatContainer);


    const seatMessage = document.createElement("p");

    seatMessage.className = "seat-message";
    seatMessage.id = "seat-message-" + passenger.id;

    card.appendChild(seatMessage);


    if (passengers.length > 1) {

        const removeButton = document.createElement("button");

        removeButton.type = "button";
        removeButton.className = "action-button remove-button";
        removeButton.textContent = "Remove Passenger";

        removeButton.addEventListener("click", function() {
            removePassenger(passenger.id);
        });

        card.appendChild(removeButton);
    }

    passengerContainer.appendChild(card);

    renderSeats();
}


// SMART SEAT SUGGESTION
function suggestSeat(passenger) {

    if (classAvailableSeats <= 0) {

        passenger.seatNumber = "";

        formMessage.textContent =
            "No confirmed seats available. You can continue with RAC or Waiting List.";

        updateSelectedSeatText(passenger);

        return;
    }

    const selectedByOthers = passengers
        .filter(p => p.id !== passenger.id)
        .map(p => p.seatNumber)
        .filter(Boolean);

    const currentSelectedCount = getSelectedSeatsCount();

    if (!passenger.seatNumber &&
        currentSelectedCount >= classAvailableSeats) {

        formMessage.textContent =
            "Confirmed seats are full. This passenger can be booked under RAC or Waiting List.";

        return;
    }

    for (let i = 1; i <= totalSeats; i++) {

        const seatNumber = "S" + i;

        const unavailable =
            bookedSeats.includes(seatNumber) ||
            selectedByOthers.includes(seatNumber);

        if (!unavailable) {

            passenger.seatNumber = seatNumber;

            updateSelectedSeatText(passenger);

            formMessage.textContent = "";

            return;
        }
    }

    passenger.seatNumber = "";

    formMessage.textContent =
        "No confirmed seat is available for this passenger. RAC / Waiting List may be assigned.";

    updateSelectedSeatText(passenger);
}


// UPDATE SELECTED SEAT TEXT
function updateSelectedSeatText(passenger) {

    const selectedText = document.getElementById(
        "selected-" + passenger.id
    );

    if (!selectedText) return;

    selectedText.textContent = passenger.seatNumber
        ? "Selected Seat: " + passenger.seatNumber
        : "Selected Seat: None (RAC / Waiting List may be assigned)";
}


// RENDER SEATS
function renderSeats() {

    passengers.forEach(passenger => {

        const container = document.getElementById(
            "seats-" + passenger.id
        );

        if (!container) return;

        container.innerHTML = "";

        for (let i = 1; i <= totalSeats; i++) {

            const seatNumber = "S" + i;

            const seat = document.createElement("button");

            seat.type = "button";
            seat.className = "seat";
            seat.textContent = seatNumber;

            const selectedByOther = passengers.some(
                p => p.id !== passenger.id &&
                     p.seatNumber === seatNumber
            );

            const booked = bookedSeats.includes(seatNumber);

            const selectedCount = getSelectedSeatsCount();

            const hasSelectionCapacity =
                passenger.seatNumber === seatNumber ||
                selectedCount < classAvailableSeats;

            const unavailable =
                booked ||
                selectedByOther ||
                classAvailableSeats <= 0 ||
                !hasSelectionCapacity;

            if (unavailable) {

                seat.disabled = true;
                seat.classList.add("unavailable");

            } else {

                if (passenger.seatNumber === seatNumber) {
                    seat.classList.add("selected");
                }

                seat.addEventListener("click", function() {

                    passenger.seatNumber = seatNumber;

                    updateSelectedSeatText(passenger);

                    formMessage.textContent = "";

                    renderSeats();
                });
            }

            container.appendChild(seat);
        }
    });
}


// REMOVE PASSENGER
function removePassenger(id) {

    if (passengers.length <= 1) {

        formMessage.textContent =
            "At least one passenger is required.";

        return;
    }

    passengers = passengers.filter(
        passenger => passenger.id !== id
    );

    const card = document.querySelector(
        `.passenger-card[data-id="${id}"]`
    );

    if (card) {
        card.remove();
    }

    updatePassengerCount();

    updatePassengerHeadings();

    renderSeats();

    formMessage.textContent = "";
}


// UPDATE PASSENGER COUNT
function updatePassengerCount() {

    passengerCount.textContent =
        `Passengers: ${passengers.length} / 6`;

    addPassengerButton.disabled =
        passengers.length >= 6;
}


// UPDATE PASSENGER HEADINGS
function updatePassengerHeadings() {

    document.querySelectorAll(".passenger-card").forEach(
        (card, index) => {

            const heading = card.querySelector("h3");

            if (heading) {
                heading.textContent =
                    "Passenger " + (index + 1);
            }
        }
    );
}


// ADD PASSENGER BUTTON
addPassengerButton.addEventListener("click", addPassenger);


// SUBMIT PASSENGER DETAILS
passengerForm.addEventListener("submit", function(event) {

    event.preventDefault();

    formMessage.textContent = "";

    if (passengers.length === 0) {

        formMessage.textContent =
            "Please add at least one passenger.";

        return;
    }

    const passengerDetails = [];

    for (const passenger of passengers) {

        const name = document.getElementById(
            "name-" + passenger.id
        ).value.trim();

        const age = document.getElementById(
            "age-" + passenger.id
        ).value;

        const gender = document.getElementById(
            "gender-" + passenger.id
        ).value;

        const email = document.getElementById(
            "email-" + passenger.id
        ).value.trim();

        const phone = document.getElementById(
            "phone-" + passenger.id
        ).value.trim();


        if (!name || !age || !gender || !phone) {

            formMessage.textContent =
                "Please fill all required passenger details.";

            return;
        }

        if (Number(age) < 1 || Number(age) > 120) {

            formMessage.textContent =
                "Please enter a valid passenger age.";

            return;
        }

        if (!/^[0-9]{10}$/.test(phone)) {

            formMessage.textContent =
                "Please enter a valid 10-digit phone number.";

            return;
        }

        const details = {
            passengerName: name,
            age: Number(age),
            gender: gender,
            email: email,
            phone: phone,

            // Empty seat means backend can allocate
            // CONFIRMED, RAC or WAITING status.
            seatNumber: passenger.seatNumber || ""
        };

        passengerDetails.push(details);
    }


    sessionStorage.setItem(
        "passengerDetails",
        JSON.stringify(passengerDetails)
    );

    sessionStorage.setItem(
        "selectedClass",
        selectedClass
    );

    sessionStorage.setItem(
        "selectedTrainId",
        trainId
    );

    window.location.href = "confirmation.html";
});


// START
loadTrainSeats();
