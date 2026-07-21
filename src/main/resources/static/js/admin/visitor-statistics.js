document.addEventListener("DOMContentLoaded", function () {

    const chartCanvas =
        document.getElementById("visitorTrendChart");

    if (chartCanvas === null) {
        return;
    }

    const chartLabels =
        parseTextList(chartCanvas.dataset.labels);

    const chartLoginData =
        parseNumberList(chartCanvas.dataset.loginValues);

    const chartGuestData =
        parseNumberList(chartCanvas.dataset.guestValues);

    createVisitorTrendChart(
        chartCanvas,
        chartLabels,
        chartLoginData,
        chartGuestData
    );
});


function parseTextList(value) {

    if (value === undefined || value === "") {
        return [];
    }

    return value.split("|");
}


function parseNumberList(value) {

    if (value === undefined || value === "") {
        return [];
    }

    return value
        .split("|")
        .map(function (item) {
            return Number(item);
        });
}


function createVisitorTrendChart(
    chartCanvas,
    chartLabels,
    chartLoginData,
    chartGuestData
) {

    new Chart(chartCanvas, {

        type: "bar",

        data: {

            labels: chartLabels,

            datasets: [

                {
                    label: "로그인",
                    data: chartLoginData,
                    backgroundColor: "rgba(37, 99, 235, 0.92)",
                    borderRadius: 8,
                    borderSkipped: false,
                    maxBarThickness: 36
                },

                {
                    label: "비로그인",
                    data: chartGuestData,
                    backgroundColor: "rgba(147, 197, 253, 0.95)",
                    borderRadius: 8,
                    borderSkipped: false,
                    maxBarThickness: 36
                }

            ]
        },

        options: {

            responsive: true,

            maintainAspectRatio: false,

            interaction: {
                mode: "index",
                intersect: false
            },

            plugins: {

                legend: {
                    display: false
                },

                tooltip: {

                    backgroundColor: "#172033",

                    padding: 14,

                    titleFont: {
                        size: 13,
                        weight: "600"
                    },

                    bodyFont: {
                        size: 13
                    },

                    callbacks: {

                        label: function (context) {

                            return context.dataset.label
                                + " "
                                + context.parsed.y
                                + "명";
                        },

                        footer: function (items) {

                            let totalVisitorCount = 0;

                            items.forEach(function (item) {
                                totalVisitorCount += item.parsed.y;
                            });

                            return "전체 "
                                + totalVisitorCount
                                + "명";
                        }
                    }
                }
            },

            scales: {

                x: {

                    stacked: true,

                    grid: {
                        display: false
                    },

                    border: {
                        display: false
                    },

                    ticks: {
                        color: "#7b8497",
                        font: {
                            size: 12
                        }
                    }
                },

                y: {

                    stacked: true,

                    beginAtZero: true,

                    border: {
                        display: false
                    },

                    grid: {
                        color: "rgba(148, 163, 184, 0.18)"
                    },

                    ticks: {

                        precision: 0,

                        color: "#7b8497",

                        callback: function (value) {
                            return value + "명";
                        }
                    }
                }
            }
        }
    });
}