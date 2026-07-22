document.addEventListener("DOMContentLoaded", function () {

    const chartContainer = document.getElementById("visitorTrendChart");
    const chartSection = document.getElementById("visitorChartSection");
    const chartWheelZone = document.getElementById("visitorChartWheelZone");
    const chartModeButtons = document.querySelectorAll("[data-chart-mode-button]");
    const chartStatus = document.getElementById("visitorChartStatus");

    if (chartContainer === null || chartSection === null || chartWheelZone === null) {
        return;
    }

    const chartLabels =
        parseTextList(chartContainer.dataset.labels);

    const chartLoginData =
        parseNumberList(chartContainer.dataset.loginValues);

    const chartGuestData =
        parseNumberList(chartContainer.dataset.guestValues);

    const chartData = createChartData(chartLabels, chartLoginData, chartGuestData);

    const visitorChart = createVisitorTrendChart(chartContainer, chartData);

    bindChartModeEvents(chartSection, chartWheelZone, chartModeButtons, chartStatus, visitorChart);

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

function createChartData(chartLabels, chartLoginData, chartGuestData) {
    return chartLabels.map(function (label, index) {
        const loginCount = chartLoginData[index] || 0;
        const guestCount = chartGuestData[index] || 0;
        return {
            date: label,
            total: loginCount + guestCount,
            loginCount: loginCount,
            guestCount: guestCount
        };
    });
}


function createVisitorTrendChart(chartContainer, chartData) {
    const root = am5.Root.new(chartContainer.id);
    root.setThemes([am5themes_Animated.new(root)]);
    const chart = root.container.children.push(
        am5xy.XYChart.new(root, {
            panX: false,
            panY: false,
            paddingLeft: 0,
            paddingRight: 20
        })
    );
    const xRenderer = am5xy.AxisRendererX.new(root, {minGridDistance: 40});
    xRenderer.grid.template.setAll({visible: false});
    xRenderer.labels.template.setAll({
        fill: am5.color(0x7b8497),
        fontSize: 12,
        paddingTop: 10
    });

    const xAxis = chart.xAxes.push(
        am5xy.CategoryAxis.new(root, {
            categoryField: "date",
            renderer: xRenderer
        })
    );
    xAxis.data.setAll(chartData);
    const yRenderer = am5xy.AxisRendererY.new(root, {});
    yRenderer.labels.template.setAll({
        fill: am5.color(0x7b8497),
        fontSize: 11
    });
    yRenderer.grid.template.setAll({
        stroke: am5.color(0xd7dfeb),
        strokeOpacity: 0.6
    });
    const yAxis = chart.yAxes.push(
        am5xy.ValueAxis.new(root, {
            min: 0,
            extraMax: 0.15,
            calculateTotals: true,
            renderer: yRenderer
        })
    );

    const totalSeries = createColumnSeries(
        root, chart, xAxis, yAxis, chartData,
        "전체 방문자", "total", false, true, 0x347fe8
    );
    const loginSeries = createColumnSeries(
        root, chart, xAxis, yAxis, chartData,
        "로그인", "loginCount", true, false, 0x2367c5
    );

    const guestSeries = createColumnSeries(
        root, chart, xAxis, yAxis, chartData,
        "게스트", "guestCount", true, false, 0x93c5fd
    );
    addTopValueLabel(root, totalSeries, "total", 0x172033);
    addInsideValueLabel(root, loginSeries, "loginCount", 0xffffff);
    addInsideValueLabel(root, guestSeries, "guestCount", 0x172033);
    addStackTotalLabel(root, guestSeries);


    chart.appear(800, 100);
    totalSeries.appear(800);
    return {
        root: root,
        chart: chart,
        totalSeries: totalSeries,
        loginSeries: loginSeries,
        guestSeries: guestSeries
    };
}

function createColumnSeries(root, chart, xAxis, yAxis, chartData,
                            seriesName, valueField, stacked, visible, color) {
    const series =
        chart.series.push(
            am5xy.ColumnSeries.new(root, {
                name: seriesName,
                xAxis: xAxis,
                yAxis: yAxis,
                categoryXField: "date",
                valueYField: valueField,
                stacked: stacked,
                visible: visible,
                maskBullets: false,
                sequencedInterpolation: true,
                tooltip: am5.Tooltip.new(root, {
                    labelText: "{name}: {valueY}명"
                })
            })
        );
    series.columns.template.setAll({
        width: am5.percent(58),
        fill: am5.color(color),
        stroke: am5.color(color),
        cornerRadiusTL: 7,
        cornerRadiusTR: 7,
        cornerRadiusBL: 7,
        cornerRadiusBR: 7
    });
    series.data.setAll(chartData);
    return series;
}

function bindChartModeEvents(chartSection, chartWheelZone, chartModeButtons,
                             chartStatus, visitorChart) {
    let isAnimating = false;

    chartWheelZone.addEventListener("wheel", function (event) {
        const currentMode = chartSection.dataset.chartMode;
        if (isAnimating) {
            event.preventDefault();
            return;
        }
        if (event.deltaY < 0 && currentMode === "summary") {
            event.preventDefault();
            changeChartMode("detail");
            return;
        }
        if (event.deltaY > 0 && currentMode === "detail") {
            event.preventDefault();
            changeChartMode("summary");
        }
    }, {
        passive: false
    });

    chartModeButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const nextMode = button.dataset.chartModeButton;
            changeChartMode(nextMode);
        });
    });

    function changeChartMode(nextMode) {
        const currentMode=chartSection.dataset.chartMode;
        if (currentMode === nextMode || isAnimating) {
            return;
        }
        isAnimating = true;
        chartSection.dataset.chartMode = nextMode;
        updateModeButtons(chartModeButtons, nextMode);
        if (nextMode === "detail") {
            Promise.all([
                visitorChart.totalSeries.hide(350),
                visitorChart.loginSeries.show(500),
                visitorChart.guestSeries.show(500)
            ]).finally(function () {
                isAnimating = false;
            });
            if (chartStatus !== null) {
                chartStatus.textContent = "로그인과 게스트 상세 보기";
            }
            return;
        }

        Promise.all([
            visitorChart.loginSeries.hide(350),
            visitorChart.guestSeries.hide(350),
            visitorChart.totalSeries.show(500)
        ]).finally(function () {
            isAnimating = false;
        });

        if (chartStatus !== null) {
            chartStatus.textContent = "전체 방문자 보기";
        }
    }
}

function updateModeButtons(chartModeButtons, currentMode) {
    chartModeButtons.forEach(function (button) {
        const buttonMode = button.dataset.chartModeButton;
        if (buttonMode === currentMode) {
            button.classList.add("is-active");
            return;
        }
        button.classList.remove("is-active");
    });
}

function addTopValueLabel(root, series, valueField, textColor) {
    series.bullets.push(function (root, series, dataItem) {
        const data = dataItem.dataContext;
        const value = Number(data[valueField]);
        if (value === 0) {
            return;
        }
        return am5.Bullet.new(root, {
            locationX: 0.5,
            locationY: 1,
            sprite: am5.Label.new(root, {
                text: value + "명",
                centerX: am5.percent(50),
                centerY: am5.percent(100),
                dy: -7,
                fill: am5.color(textColor),
                fontSize: 12,
                fontWeight: "700"
            })
        });
    });
}

function addInsideValueLabel(root, series, valueField, textColor) {

    series.bullets.push(function (root, series, dataItem) {

        const data = dataItem.dataContext;
        const value = Number(data[valueField]);

        if (value === 0) {
            return;
        }

        return am5.Bullet.new(root, {
            locationX: 0.5,
            locationY: 0.5,

            sprite: am5.Label.new(root, {
                text: value + "명",
                centerX: am5.percent(50),
                centerY: am5.percent(50),
                fill: am5.color(textColor),
                fontSize: 11,
                fontWeight: "700"
            })
        });
    });
}

function addStackTotalLabel(root, series) {
    series.bullets.push(function (root,series,dataItem) {
        const data=dataItem.dataContext;
        const total = Number(data.total);
        if (total === 0) {
            return;
        }
        return am5.Bullet.new(root, {
            locationX: 0.5,
            locationY: 1,
            sprite: am5.Label.new(root, {
                text: "전체 " + total + "명",
                centerX: am5.percent(50),
                centerY: am5.percent(100),
                dy: -8,
                fill: am5.color(0x172033),
                fontSize: 11,
                fontWeight: "700"
            })
        });
    });
}
