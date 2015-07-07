<%-- 
    Document   : circles
    Created on : Jul 1, 2015, 12:29:49 PM
    Author     : anu
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link href="css/mycss.css" rel="stylesheet" type="text/css"/>
    </head>
    <body>

        <canvas id="myCanvas" width="300" height="400"></canvas>
        <div id="chartContainer" style="height:100%; width: 1000px;">
        </div>
        <script>
            var radius1 = <%= request.getAttribute("radius")%>;
            var radius2 = <%= request.getAttribute("radius1")%>;
            var radius3 = <%= request.getAttribute("radius2")%>;
            var belongs = <%= request.getAttribute("belongs")%>;
            var cobelongs = <%= request.getAttribute("cobelongs")%>;
            var pobelongs = <%= request.getAttribute("pobelongs")%>;
            var categorydominated =<%=request.getAttribute("categoryDominators")%>;
            var thispolicy =<%=request.getAttribute("mypolicy")%>;
            var mypolicy=thispolicy[0];
            var canvas = document.getElementById('myCanvas');
            var context = canvas.getContext('2d');
            var centerX = canvas.width / 2;
            var centerY = canvas.height / 2;
            var positionX = centerX;
            if (belongs === false) {
                positionX = centerX + radius1 + 20;
                positionY = centerY + radius1 + 20;
            } else
            if (cobelongs === false) {
                positionX = centerX + radius2 + (radius1 - radius2) * 0.5 + 5;
                positionY = centerY + radius2 + (radius1 - radius2) * 0.5 + 5;
            } else
            if (pobelongs === false) {
                positionX = centerX + radius3 + 5;
                positionY = centerY + radius3 + 5;
            }
            context.globalAlpha = 0.6;
            context.beginPath();
            context.arc(centerX, centerY, radius1, 0, 2 * Math.PI, false);
            context.fillStyle = 'Chartreuse';
            context.fill();
            context.lineWidth = 1;
            context.strokeStyle = '#003300';
            context.stroke();
            context.globalAlpha = 0.6;
            context.beginPath();
            context.arc(centerX, centerY, radius2, 0, 2 * Math.PI, false);
            context.fillStyle = 'green';
            context.fill();
            context.lineWidth = 1;
            context.strokeStyle = '#003300';
            context.stroke();
            context.globalAlpha = 0.8;
            context.beginPath();
            context.arc(centerX, centerY, radius3, 0, 2 * Math.PI, false);
            context.fillStyle = 'Purple';
            context.fill();
            context.lineWidth = 3;
            context.strokeStyle = '#003300';
            context.stroke();
            context.beginPath();
            context.rect(positionX, centerY, 10, 10);
            context.fillStyle = 'yellow';
            context.fill();
            context.lineWidth = 3;
            context.strokeStyle = '#003300';
            context.stroke();
            window.onload = function () {
                var chart = new CanvasJS.Chart("chartContainer");
                chart.options.title = {text: "Better category solutions"};
                chart.options.axisX = {text: "Objectives"};
                chart.options.zoomEnabled = true;
                chart.options.panEnabled = false;
                chart.options.data = [];

                var series1 = {//dataSeries 
                    type: "line",
                    name: mypolicy['policy'],
                    markerType: "cross",
                    showInLegend: true
                };
                var test1 = [{label: 'BIODIV_LUCALL', y: mypolicy['BIODIV_LUCALL']}, {label: 'CROPCALOR', y: mypolicy['CROPCALOR']}, {label: 'CROPEMIS', y: mypolicy['CROPEMIS']}, {label: 'DeforestationAREA', y: mypolicy['DeforestationAREA']}];
                chart.options.data.push(series1);
                series1.dataPoints = test1;
                for (var key in categorydominated) {
                    var obj = categorydominated[key];
                    var policyname = obj['policy'];
                    var series2 = {//dataSeries 
                        type: "line",
                        name: policyname,
                        markerType: "no marker",
                        showInLegend: true
                    };
                    var test = [{label: 'BIODIV_LUCALL', y: obj['BIODIV_LUCALL']}, {label: 'CROPCALOR', y: obj['CROPCALOR']}, {label: 'CROPEMIS', y: obj['CROPEMIS']}, {label: 'DeforestationAREA', y: obj['DeforestationAREA']}];
                    chart.options.data.push(series2);
                    series2.dataPoints = test;
                }

                chart.render();
            }
        </script>
        <script src="js/libs/canvasjs.min.js" type="text/javascript"></script>
    </body>
</html>
