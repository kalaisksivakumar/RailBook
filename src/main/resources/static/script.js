
// FORMAT TRAIN TIME
function formatTrainTime(time) {
    if (!time) {
        return "Not available";
    }

    return time.substring(0, 5);
}

// FORMAT TICKET FARE
function formatFare(fare) {
    if (fare === null || fare === undefined) {
        return "Fare not available";
    }

    return "₹" + Number(fare).toFixed(2);
}

// DISPLAY CLASS AVAILABILITY
function getClassDetails(train) {
    return [
        {
            code: "1A",
            name: "First AC",
            available: train.firstAcAvailable,
            total: train.firstAcSeats,
            fare: train.firstAcFare
        },
        {
            code: "2A",
            name: "Second AC",
            available: train.secondAcAvailable,
            total: train.secondAcSeats,
            fare: train.secondAcFare
        },
        {
            code: "3A",
            name: "Third AC",
            available: train.thirdAcAvailable,
            total: train.thirdAcSeats,
            fare: train.thirdAcFare
        },
        {
            code: "3E",
            name: "AC 3 Economy",
            available: train.acEconomyAvailable,
            total: train.acEconomySeats,
            fare: train.acEconomyFare
        },
        {
            code: "SL",
            name: "Sleeper",
            available: train.sleeperAvailable,
            total: train.sleeperSeats,
            fare: train.sleeperFare
        }
    ];
}

// SEARCH TRAINS
async function searchTrains() {

    const source = document.getElementById("source").value.trim();
    const destination = document.getElementById("destination").value.trim();

    const results = document.getElementById("results");

    if (source === "" || destination === "") {
        results.innerHTML = "<p>Please enter source and destination!</p>";
        return;
    }

    results.innerHTML = "<p>Searching trains...</p>";

    try {

        const response = await fetch("/api/trains");

        if (!response.ok) {
            throw new Error("Unable to fetch trains");
        }

        const trains = await response.json();

        const matchingTrains = trains.filter(train =>
            train.source.toLowerCase() === source.toLowerCase() &&
            train.destination.toLowerCase() === destination.toLowerCase()
        );

        if (matchingTrains.length === 0) {
            results.innerHTML = "<p>No trains found for this route.</p>";
            return;
        }

        results.innerHTML = "";

        matchingTrains.forEach(train => {

            const trainCard = document.createElement("div");
            trainCard.className = "train-card";

            const classes = getClassDetails(train);

            const classHTML = classes.map(item => {

                const available = Number(item.available) || 0;
                const total = Number(item.total) || 0;

                let status = "";

                if (available > 0) {
                    status = `Available ${available}`;
                } else {
                    status = "Not Available";
                }

                return `
                    <div class="class-card">
                        <h4>${item.code} - ${item.name}</h4>
                        <p>${status}</p>
                        <p>Capacity: ${total}</p>
                        <p>Fare: ${formatFare(item.fare)}</p>

                        <button
                            type="button"
                            onclick="bookTicket(${train.id}, '${item.code}')"
                            ${available <= 0 ? "disabled" : ""}
                        >
                            Select Class
                        </button>
                    </div>
                `;
            }).join("");

            trainCard.innerHTML = `
                <h3>${train.trainName}</h3>

                <p><strong>Train Number:</strong> ${train.trainNumber}</p>

                <p><strong>Route:</strong>
                    ${train.source} → ${train.destination}
                </p>

                <p><strong>Departure:</strong>
                    ${formatTrainTime(train.departureTime)}
                </p>

                <p><strong>Arrival:</strong>
                    ${formatTrainTime(train.arrivalTime)}
                </p>

                <p><strong>Total Seats:</strong> ${train.totalSeats}</p>

                <p><strong>Available Seats:</strong> ${train.availableSeats}</p>

                <p><strong>General Ticket Fare:</strong>
                    ${formatFare(train.fare)}
                </p>

                <h3>Available Travel Classes</h3>

                <div class="class-container">
                    ${classHTML}
                </div>
            `;

            results.appendChild(trainCard);
        });

    } catch (error) {

        results.innerHTML = "<p>Error loading trains. Please try again.</p>";

        console.error(error);
    }
}

// BOOK TICKET
function bookTicket(trainId, classCode) {

    sessionStorage.setItem("selectedTrainId", trainId);

    sessionStorage.setItem("selectedClass", classCode);

    sessionStorage.removeItem("passengerDetails");

    window.location.href = "passenger.html";
}
