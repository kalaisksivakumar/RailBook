
const API_URL = "/api/charts";

// Prepare train chart
async function prepareChart() {
    const trainId = document.getElementById("trainId").value;
    const message = document.getElementById("message");
    const button = document.getElementById("prepareButton");

    if (!trainId || Number(trainId) <= 0) {
        message.textContent = "Please enter a valid Train ID.";
        return;
    }

    button.disabled = true;
    message.textContent = "Preparing chart...";

    try {
        const response = await fetch(
            `${API_URL}/prepare/${trainId}`,
            { method: "POST" }
        );

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || data.error || "Unable to prepare chart.");
        }

        message.textContent =
            `Chart prepared successfully for Train ${data.trainNumber}.`;

        loadAllCharts();

    } catch (error) {
        message.textContent = error.message;
    } finally {
        button.disabled = false;
    }
}


// Check chart status
async function checkChartStatus() {
    const trainId = document.getElementById("statusTrainId").value;
    const statusMessage = document.getElementById("statusMessage");

    if (!trainId || Number(trainId) <= 0) {
        statusMessage.textContent = "Please enter a valid Train ID.";
        return;
    }

    try {
        const response = await fetch(
            `${API_URL}/status/${trainId}`
        );

        if (!response.ok) {
            throw new Error("Unable to check chart status.");
        }

        const prepared = await response.json();

        statusMessage.textContent = prepared
            ? "Chart has been prepared."
            : "Chart has not been prepared yet.";

    } catch (error) {
        statusMessage.textContent = error.message;
    }
}


// View passenger-wise chart
async function viewPassengerChart() {
    const trainId = document.getElementById("passengerTrainId").value;
    const result = document.getElementById("passengerResult");

    if (!trainId || Number(trainId) <= 0) {
        result.textContent = "Please enter a valid Train ID.";
        return;
    }

    result.textContent = "Loading passenger details...";

    try {
        const response = await fetch(
            `${API_URL}/${trainId}/passengers`
        );

        const passengers = await response.json();

        if (!response.ok) {
            throw new Error(
                passengers.message ||
                passengers.error ||
                "Unable to load passenger chart."
            );
        }

        if (!Array.isArray(passengers) || passengers.length === 0) {
            result.textContent = "No passenger bookings found.";
            return;
        }

        let html = `
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Booking ID</th>
                            <th>PNR</th>
                            <th>Passenger</th>
                            <th>Class</th>
                            <th>Seat</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
        `;

        passengers.forEach(booking => {
            const passengerName =
                booking.passenger?.name || "N/A";

            html += `
                <tr>
                    <td>${booking.id ?? "N/A"}</td>
                    <td>${booking.pnr ?? "N/A"}</td>
                    <td>${passengerName}</td>
                    <td>${booking.travelClass ?? "N/A"}</td>
                    <td>${booking.seatNumber ?? booking.racNumber ?? booking.waitingNumber ?? "N/A"}</td>
                    <td>${booking.status ?? "N/A"}</td>
                </tr>
            `;
        });

        html += `
                    </tbody>
                </table>
            </div>
        `;

        result.innerHTML = html;

    } catch (error) {
        result.textContent = error.message;
    }
}


// Load all prepared charts
async function loadAllCharts() {
    const chartList = document.getElementById("chartList");

    chartList.textContent = "Loading charts...";

    try {
        const response = await fetch(API_URL);

        const charts = await response.json();

        if (!response.ok) {
            throw new Error("Unable to load charts.");
        }

        if (!Array.isArray(charts) || charts.length === 0) {
            chartList.textContent = "No charts have been prepared yet.";
            return;
        }

        let html = `
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Train ID</th>
                            <th>Train Number</th>
                            <th>Source</th>
                            <th>Destination</th>
                            <th>Prepared At</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
        `;

        charts.forEach(chart => {
            html += `
                <tr>
                    <td>${chart.trainId ?? "N/A"}</td>
                    <td>${chart.trainNumber ?? "N/A"}</td>
                    <td>${chart.source ?? "N/A"}</td>
                    <td>${chart.destination ?? "N/A"}</td>
                    <td>${chart.chartPreparedAt
                        ? new Date(chart.chartPreparedAt).toLocaleString()
                        : "N/A"}</td>
                    <td>${chart.status ?? "N/A"}</td>
                </tr>
            `;
        });

        html += `
                    </tbody>
                </table>
            </div>
        `;

        chartList.innerHTML = html;

    } catch (error) {
        chartList.textContent = error.message;
    }
}


// Load charts when page opens
document.addEventListener("DOMContentLoaded", loadAllCharts);
