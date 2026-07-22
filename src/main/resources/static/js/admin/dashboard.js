document.addEventListener("DOMContentLoaded", function () {

    const hospitalStatusChart =
        document.querySelector("#hospitalStatusChart");

    if (hospitalStatusChart === null) {
        return;
    }

    createHospitalStatusChart(hospitalStatusChart);
});


function createHospitalStatusChart(chartCanvas) {

    const openHospitalCount =
        Number(chartCanvas.dataset.open);

    const tempClosedHospitalCount =
        Number(chartCanvas.dataset.tempClosed);

    const closedHospitalCount =
        Number(chartCanvas.dataset.closed);

    const totalHospitalCount =
        Number(chartCanvas.dataset.total);

    const chartData = {

        labels: [
            "영업중",
            "휴업",
            "폐업"
        ],

        datasets: [
            {
                label: "병원 수",

                data: [
                    openHospitalCount,
                    tempClosedHospitalCount,
                    closedHospitalCount
                ],

                backgroundColor: [
                    "#35c8a0",
                    "#f5ad4b",
                    "#ef6b6b"
                ],

                borderColor: "#ffffff",
                borderWidth: 5,
                borderRadius: 5,
                hoverOffset: 10
            }
        ]
    };

    const centerTextPlugin = {

        id: "hospitalCenterText",

        beforeDraw: function (chart) {

            if (chart.chartArea === undefined) {
                return;
            }

            const context = chart.ctx;

            const centerX =
                chart.chartArea.left
                + chart.chartArea.width / 2;

            const centerY =
                chart.chartArea.top
                + chart.chartArea.height / 2;

            context.save();

            context.textAlign = "center";
            context.textBaseline = "middle";

            context.fillStyle = "#8a94a7";
            context.font =
                "600 12px Pretendard, Arial";

            context.fillText(
                "전체 병원",
                centerX,
                centerY - 14
            );

            context.fillStyle = "#172033";
            context.font =
                "800 26px Pretendard, Arial";

            context.fillText(
                totalHospitalCount + "개",
                centerX,
                centerY + 15
            );

            context.restore();
        }
    };

    const chartConfig = {

        type: "doughnut",

        data: chartData,

        options: {

            responsive: true,

            maintainAspectRatio: false,

            cutout: "68%",

            plugins: {

                legend: {

                    position: "top",

                    labels: {

                        usePointStyle: true,
                        pointStyle: "circle",
                        boxWidth: 9,
                        boxHeight: 9,
                        padding: 18,
                        color: "#667085",

                        font: {
                            size: 12,
                            weight: "600"
                        }
                    }
                },

                title: {

                    display: true,

                    text: "병원 운영 상태 비율",

                    color: "#172033",

                    padding: {
                        top: 4,
                        bottom: 14
                    },

                    font: {
                        size: 14,
                        weight: "700"
                    }
                },

                tooltip: {

                    backgroundColor: "#172033",
                    padding: 12,
                    displayColors: true,
                    boxWidth: 9,
                    boxHeight: 9,

                    callbacks: {

                        label: function (context) {

                            const hospitalCount =
                                context.parsed;

                            let percentage = 0;

                            if (totalHospitalCount > 0) {

                                percentage =
                                    hospitalCount
                                    / totalHospitalCount
                                    * 100;

                                percentage =
                                    Math.round(
                                        percentage * 10
                                    ) / 10;
                            }

                            return context.label
                                + " "
                                + hospitalCount
                                + "개 ("
                                + percentage
                                + "%)";
                        }
                    }
                }
            },

            animation: {
                animateRotate: true,
                animateScale: true,
                duration: 1000
            }
        },

        plugins: [
            centerTextPlugin
        ]
    };

    new Chart(
        chartCanvas,
        chartConfig
    );
}