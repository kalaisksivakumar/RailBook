
let allTrains = [];
let allBookings = [];

// ===============================
// LOAD ADMIN DASHBOARD
// ===============================

async function loadAdminDashboard() {
    try {
        await loadTrains();
        await loadBookings();
        loadDashboardStatistics();
        loadTrainAvailabilityReport();
    } catch (error) {
        console.error("Dashboard loading error:", error);
    }
}

// ===============================
// LOAD TRAINS
// ===============================

async function loadTrains() {
    const response = await fetch("/api/trains");

    if (!response.ok) {
        throw new Error("Unable to load trains");
    }

    allTrains = await response.json();
    displayTrains(allTrains);
}

// ===============================
// DISPLAY TRAINS
// ===============================

function displayTrains(trains) {
    const trainList = document.getElementById("trainList");

    if (!trainList) return;

    if (!trains || trains.length === 0) {
        trainList.innerHTML = "<p>No trains found.</p>";
        return;
    }

    trainList.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Train No.</th>
                    <th>Train Name</th>
                    <th>Source</th>
                    <th>Destination</th>
                    <th>Total Seats</th>
                    <th>Departure</th>
                    <th>Arrival</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                ${trains.map(train => `
                    <tr>
                        <td>${train.trainNumber ?? "-"}</td>
                        <td>${train.trainName ?? "-"}</td>
                        <td>${train.source ?? "-"}</td>
                        <td>${train.destination ?? "-"}</td>
                        <td>${train.totalSeats ?? 0}</td>
                        <td>${train.departureTime ?? "-"}</td>
                        <td>${train.arrivalTime ?? "-"}</td>
                        <td>
                            <button onclick="editTrain(${train.id})">
                                Edit
                            </button>
                            <button class="secondary-button"
                                onclick="deleteTrain(${train.id})">
                                Delete
                            </button>
                        </td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

// ===============================
// ADD TRAIN
// ===============================

document.getElementById("addTrainForm")
    ?.addEventListener("submit", async function(event) {

        event.preventDefault();

        const train = {
            trainNumber: document.getElementById("trainNumber").value.trim(),
            trainName: document.getElementById("trainName").value.trim(),
            source: document.getElementById("source").value.trim(),
            destination: document.getElementById("destination").value.trim(),
            totalSeats: Number(document.getElementById("totalSeats").value),
            departureTime: document.getElementById("departureTime").value + ":00",
            arrivalTime: document.getElementById("arrivalTime").value + ":00"
        };

        const message = document.getElementById("addTrainMessage");

        try {
            const response = await fetch("/api/admin/trains", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(train)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || "Unable to add train");
            }

            message.textContent = "Train added successfully!";
            message.style.color = "green";

            this.reset();

            await loadAdminDashboard();

        } catch (error) {
            console.error(error);
            message.textContent = error.message;
            message.style.color = "red";
        }
    });

// ===============================
// SEARCH TRAINS
// ===============================

function searchAdminTrains() {
    const search = document.getElementById("trainSearch")
        .value.toLowerCase().trim();

    const filteredTrains = allTrains.filter(train =>
        String(train.trainNumber).toLowerCase().includes(search) ||
        String(train.trainName).toLowerCase().includes(search) ||
        String(train.source).toLowerCase().includes(search) ||
        String(train.destination).toLowerCase().includes(search)
    );

    displayTrains(filteredTrains);
}

// ===============================
// CLEAR TRAIN SEARCH
// ===============================

function clearTrainSearch() {
    document.getElementById("trainSearch").value = "";
    displayTrains(allTrains);
}

// ===============================
// EDIT TRAIN
// ===============================

async function editTrain(id) {
    const train = allTrains.find(t => t.id === id);

    if (!train) {
        alert("Train not found!");
        return;
    }

    const trainNumber = prompt("Enter Train Number:", train.trainNumber);
    if (trainNumber === null) return;

    const trainName = prompt("Enter Train Name:", train.trainName);
    if (trainName === null) return;

    const source = prompt("Enter Source:", train.source);
    if (source === null) return;

    const destination = prompt("Enter Destination:", train.destination);
    if (destination === null) return;

    const totalSeats = prompt("Enter Total Seats:", train.totalSeats);
    if (totalSeats === null) return;

    const departureInput = prompt(
        "Enter Departure Time (HH:mm):",
        train.departureTime
            ? train.departureTime.substring(0, 5)
            : "20:10"
    );
    if (departureInput === null) return;

    const arrivalInput = prompt(
        "Enter Arrival Time (HH:mm):",
        train.arrivalTime
            ? train.arrivalTime.substring(0, 5)
            : "07:30"
    );
    if (arrivalInput === null) return;

    const formatTime = (time) => {
        const match = time.trim().match(/^(\d{1,2}):(\d{2})$/);

        if (!match) {
            throw new Error("Enter time in HH:mm format, for example 08:00");
        }

        const hour = Number(match[1]);
        const minute = Number(match[2]);

        if (hour > 23 || minute > 59) {
            throw new Error("Invalid time. Enter a valid 24-hour time.");
        }

        return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}:00`;
    };

    let departureTime;
    let arrivalTime;

    try {
        departureTime = formatTime(departureInput);
        arrivalTime = formatTime(arrivalInput);
    } catch (error) {
        alert(error.message);
        return;
    }

    const updatedTrain = {
        trainNumber: trainNumber.trim(),
        trainName: trainName.trim(),
        source: source.trim(),
        destination: destination.trim(),
        totalSeats: Number(totalSeats),
        departureTime: departureTime,
        arrivalTime: arrivalTime
    };

    try {
        const response = await fetch(`/api/admin/trains/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(updatedTrain)
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        alert("Train updated successfully!");
        await loadAdminDashboard();

    } catch (error) {
        console.error(error);
        alert("Error updating train: " + error.message);
    }
}

// ===============================
// DELETE TRAIN
// ===============================

async function deleteTrain(id) {
    if (!confirm("Are you sure you want to delete this train?")) {
        return;
    }

    try {
        const response = await fetch(`/api/admin/trains/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        alert("Train deleted successfully!");
        await loadAdminDashboard();

    } catch (error) {
        console.error(error);
        alert("Unable to delete train: " + error.message);
    }
}

// ===============================
// LOAD BOOKINGS
// ===============================

async function loadBookings() {
    const response = await fetch("/api/bookings");

    if (!response.ok) {
        throw new Error("Unable to load bookings");
    }

    allBookings = await response.json();
    displayBookings(allBookings);
}

// ===============================
// DISPLAY BOOKINGS
// ===============================

function displayBookings(bookings) {
    const bookingList = document.getElementById("adminBookingList");

    if (!bookingList) return;

    if (!bookings || bookings.length === 0) {
        bookingList.innerHTML = "<p>No bookings found.</p>";
        return;
    }

    bookingList.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>PNR</th>
                    <th>Passenger</th>
                    <th>Train</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                ${bookings.map(booking => `
                    <tr>
                        <td>${booking.pnr ?? "-"}</td>
                        <td>${booking.passengerName ?? booking.passenger?.name ?? "-"}</td>
                        <td>${booking.trainName ?? "-"}</td>
                        <td>${booking.status ?? "-"}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

// ===============================
// SEARCH BOOKINGS
// ===============================

function searchAdminBookings() {
    const search = document.getElementById("bookingSearch")
        .value.toLowerCase().trim();

    const status = document.getElementById("bookingStatusFilter").value;

    const filteredBookings = allBookings.filter(booking => {

        const matchesSearch =
            String(booking.pnr ?? "").toLowerCase().includes(search) ||
            String(booking.passengerName ?? booking.passenger?.name ?? "")
                .toLowerCase().includes(search) ||
            String(booking.trainName ?? "")
                .toLowerCase().includes(search);

        const matchesStatus =
            status === "ALL" ||
            String(booking.status).toUpperCase() === status;

        return matchesSearch && matchesStatus;
    });

    displayBookings(filteredBookings);
}

// ===============================
// RESET BOOKING FILTERS
// ===============================

function resetBookingFilters() {
    document.getElementById("bookingSearch").value = "";
    document.getElementById("bookingStatusFilter").value = "ALL";

    displayBookings(allBookings);
}

// ===============================
// DASHBOARD STATISTICS
// ===============================

function loadDashboardStatistics() {
    document.getElementById("totalTrains").textContent =
        allTrains.length;

    document.getElementById("totalBookings").textContent =
        allBookings.length;

    document.getElementById("confirmedBookings").textContent =
        allBookings.filter(b =>
            String(b.status).toUpperCase() === "CONFIRMED"
        ).length;

    document.getElementById("cancelledBookings").textContent =
        allBookings.filter(b =>
            String(b.status).toUpperCase() === "CANCELLED"
        ).length;
}

// ===============================
// TRAIN AVAILABILITY REPORT
// ===============================

function loadTrainAvailabilityReport() {
    const report = document.getElementById("trainAvailabilityReport");

    if (!report) return;

    if (allTrains.length === 0) {
        report.innerHTML = "<p>No train availability data.</p>";
        return;
    }

    report.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>Train Number</th>
                    <th>Train Name</th>
                    <th>Total Seats</th>
                    <th>Available Seats</th>
                </tr>
            </thead>
            <tbody>
                ${allTrains.map(train => `
                    <tr>
                        <td>${train.trainNumber ?? "-"}</td>
                        <td>${train.trainName ?? "-"}</td>
                        <td>${train.totalSeats ?? 0}</td>
                        <td>${train.availableSeats ?? "Check booking API"}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

// ===============================
// EXPORT BOOKINGS TO CSV
// ===============================

function exportBookingsToCSV() {
    if (allBookings.length === 0) {
        alert("No bookings available to export!");
        return;
    }

    const headers = [
        "PNR",
        "Passenger",
        "Train",
        "Status"
    ];

    const rows = allBookings.map(booking => [
        booking.pnr ?? "",
        booking.passengerName ?? booking.passenger?.name ?? "",
        booking.trainName ?? "",
        booking.status ?? ""
    ]);

    const csvContent = [
        headers,
        ...rows
    ].map(row =>
        row.map(value =>
            `"${String(value).replace(/"/g, '""')}"`
        ).join(",")
    ).join("\n");

    const blob = new Blob([csvContent], {
        type: "text/csv;charset=utf-8;"
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;
    link.download = "RailBook_Bookings.csv";
    link.click();

    URL.revokeObjectURL(url);
}

// ===============================
// INITIALIZE DASHBOARD
// ===============================

document.addEventListener("DOMContentLoaded", function() {
    loadAdminDashboard();
});
