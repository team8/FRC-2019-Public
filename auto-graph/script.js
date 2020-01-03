var waypoints = [];
var ctx;
var width = 1085; //pixels
var height = 575; //pixels
var kFieldWidth = 652; // in inches
var kFieldHeight = 324.0; // in inches
var robotWidth = 27.0; //inches
var robotHeight = 32.0; //inches
var pointRadius = 5;
var turnRadius = 30;
var kEpsilon = 1E-9;
var image;
var imageFlipped;
var wto;

var routines = [[]];

var onWhich = 0;

var maxSpeed = 120;
var maxSpeedColor = [0, 255, 0];
var minSpeed = 0;
var minSpeedColor = [255, 0, 0];
var pathFillColor = "rgba(150, 150, 150, 0.5)";

var counter = 0;

class Translation2d {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    norm() {
        return Math.sqrt(Translation2d.dot(this, this));
    }

    scale(s) {
        return new Translation2d(this.x * s, this.y * s);
    }

    translate(t) {
        return new Translation2d(this.x + t.x, this.y + t.y);
    }

    invert() {
        return new Translation2d(-this.x, -this.y);
    }

    perp() {
        return new Translation2d(-this.y, this.x);
    }

    draw(color) {
        ctx.beginPath();
        ctx.fillStyle = color;
        ctx.strokeStyle = color;
        ctx.fill();
        ctx.lineWidth = 0;
        ctx.stroke();
    }

    get drawX() {
        return this.x * (width / kFieldWidth);
    }

    get drawY() {
        return height - this.y * (height / kFieldHeight);
    }

    get angle() {
        return Math.atan2(-this.y, this.x);
    }

    static diff(a, b) {
        return new Translation2d(b.x - a.x, b.y - a.y);
    }

    static cross(a, b) {
        return a.x * b.y - a.y * b.x;
    }

    static dot(a, b) {
        return a.x * b.x + a.y * b.y;
    }

    static angle(a, b) {
        return Math.acos(Translation2d.dot(a, b) / (a.norm() * b.norm()));
    }
}

class DrivePathRoutine {
    constructor(name, paths, isReversed) {
        this.name = name;
        this.paths = paths;
        this.isReversed = isReversed;
    }
    getName() {
        return this.name;
    }
    getReversed() {
        return this.isReversed;
    }
    getPaths() {
        return this.paths;
    }
    addPath(newPath) {
        this.paths.push(newPath);
    }
    setPaths(newPaths) {
        this.paths = newPaths;
    }
}

class Waypoint {
    constructor(position, speed, comment) {
        this.position = position;
        this.speed = speed;
        this.comment = comment;
        this.radius = 0;
    }

    draw() {
        this.position.draw((this.radius > 0) ? "rgba(120,120,120,0.8)" : null);
    }

    toString() {
        var comment = (this.comment.length > 0) ? " //" + this.comment : "";
        return "Waypoints.add(new Translation2d(" + this.position.x + "," + this.position.y + "," + this.radius + "," + this.speed + "));" + comment;
    }
}

class Line {
    constructor(pointA, pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.slope = Translation2d.diff(pointA.position, pointB.position);
        this.start = pointA.position.translate(this.slope.scale(pointA.radius / this.slope.norm()));
        this.end = pointB.position.translate(this.slope.scale(pointB.radius / this.slope.norm()).invert());
    }

    draw() {
        ctx.beginPath();
        ctx.moveTo(this.start.drawX, this.start.drawY);
        ctx.lineTo(this.end.drawX, this.end.drawY);

        try {
            var grad = ctx.createLinearGradient(this.start.drawX, this.start.drawY, this.end.drawX, this.end.drawY);
            grad.addColorStop(0, getColorForSpeed(this.pointB.speed));
            grad.addColorStop(1, getColorForSpeed(getNextSpeed(this.pointB)));
            ctx.strokeStyle = grad;
        } catch (e) {
            ctx.strokeStyle = "#00ff00"
        }

        ctx.lineWidth = pointRadius * 2;
        ctx.stroke();
        this.pointA.draw();
        this.pointB.draw();
    }

    fill() {
        var start = this.start;
        var deltaEnd = Translation2d.diff(this.start, this.end);
        var angle = deltaEnd.angle;
        var length = deltaEnd.norm();
        for (var i = 0; i < length; i++) {
            drawRotatedRect(start.translate(deltaEnd.scale(i / length)), robotHeight, robotWidth, angle, null, pathFillColor, true);
        }
    }

    translation() {
        return new Translation2d(this.pointB.position.y - this.pointA.position.y, this.pointB.position.x - this.pointA.position.x)
    }

    slope() {
        if (this.pointB.position.x - this.pointA.position.x > kEpsilon)
            return (this.pointB.position.y - this.pointA.position.y) / (this.pointB.position.x - this.pointA.position.x);
        else
            return (this.pointB.position.y - this.pointA.position.y) / kEpsilon;
    }

    b() {
        return this.pointA.y - this.slope() * this.pointA.x;
    }

    static intersect(a, b, c, d) {
        var i = ((a.x - b.x) * (c.y - d.y) - (a.y - b.y) * (c.x - d.x));
        i = (Math.abs(i) < kEpsilon) ? kEpsilon : i;
        var x = (Translation2d.cross(a, b) * (c.x - d.x) - Translation2d.cross(c, d) * (a.x - b.x)) / i;
        var y = (Translation2d.cross(a, b) * (c.y - d.y) - Translation2d.cross(c, d) * (a.y - b.y)) / i;
        return new Translation2d(x, y);
    }

    static pointSlope(p, s) {
        return new Line(p, p.translate(s));
    }
}

class Arc {
    constructor(lineA, lineB) {
        this.lineA = lineA;
        this.lineB = lineB;
        this.center = Line.intersect(lineA.end, lineA.end.translate(lineA.slope.perp()), lineB.start, lineB.start.translate(lineB.slope.perp()));
        this.center.draw;
        this.radius = Translation2d.diff(lineA.end, this.center).norm();
    }

    draw() {
        var sTrans = Translation2d.diff(this.center, this.lineA.end);
        var eTrans = Translation2d.diff(this.center, this.lineB.start);
        console.log(sTrans);
        console.log(eTrans);
        var sAngle, eAngle;
        if (Translation2d.cross(sTrans, eTrans) > 0) {
            eAngle = -Math.atan2(sTrans.y, sTrans.x);
            sAngle = -Math.atan2(eTrans.y, eTrans.x);
        } else {
            sAngle = -Math.atan2(sTrans.y, sTrans.x);
            eAngle = -Math.atan2(eTrans.y, eTrans.x);
        }
        this.lineA.draw();
        this.lineB.draw();
        ctx.beginPath();
        ctx.arc(this.center.drawX, this.center.drawY, this.radius * (width / kFieldWidth), sAngle, eAngle);
        ctx.strokeStyle = getColorForSpeed(this.lineB.pointB.speed);
        ctx.stroke();
    }

    fill() {
        this.lineA.fill();
        this.lineB.fill();
        var sTrans = Translation2d.diff(this.center, this.lineA.end);
        var eTrans = Translation2d.diff(this.center, this.lineB.start);
        var sAngle = (Translation2d.cross(sTrans, eTrans) > 0) ? sTrans.angle : eTrans.angle;
        var angle = Translation2d.angle(sTrans, eTrans);
        var length = angle * this.radius;
        for (var i = 0; i < length; i += this.radius / 100) {
            drawRotatedRect(this.center.translate(new Translation2d(this.radius * Math.cos(sAngle - i / length * angle), -this.radius * Math.sin(sAngle - i / length * angle))), robotHeight, robotWidth, sAngle - i / length * angle + Math.PI / 2, null, pathFillColor, true);
        }


    }

    static fromPoints(a, b, c) {
        return new Arc(new Line(a, b), new Line(b, c));
    }
}
function addDrivePathRoutine() {
    var tables = document.getElementById("Routines");
    var newRow = tables.insertRow();
    var tableHTML = '<td><table class="path" id="' + counter + '">';
    tableHTML += `
        <thead>
            <tr>
                <td class='name'><input placeholder='Name' style=\"width: 150px;\"></td>
                <th>X</th>
                <th>Y</th>
                <th>Speed</th>
                <th>Comments</th>
                <td><button onclick='$(this).parent().parent().parent().parent().remove();update()'>Delete</button></td>
            </tr>
        </thead>
        <tbody>
            <tr class="points">
                <td></td>
                <td><input placeholder='X'></td>
                <td><input placeholder='Y'></td>
                <td><input placeholder='Speed'></td>
                <td class='comments'><input placeholder='Comments'></td>
                <td><button onclick='$(this).parent().parent().remove();update()'>Delete</button></td>
            </tr>
        </tbody>
        <tfoot>
<!--            <td colspan=\"6\"><div><button onclick=\"addPoint(Number(counter))\">Add Waypoint</button></div></td>-->
                <td colspan=\"6\"><div><button class="waypoint">Add Waypoint</button></div></td>
        </tfoot></td>`;
    newRow.innerHTML = tableHTML;
    addTableDnD();
}

function init() {
    $("#field").css("width", (width / 1.5) + "px");
    $("#field").css("height", (height / 1.5) + "px");
    ctx = document.getElementById('field').getContext('2d');
    ctx.canvas.width = width;
    ctx.canvas.height = height;
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = "#FFFFFF";
    image = new Image();
    image.src = 'field.png';
    image.onload = function () {
        ctx.drawImage(image, 0, 0, width, height);
        update();
    }
    imageFlipped = new Image();
    imageFlipped.src = 'fieldflipped.png';
    $('input').bind("change paste keyup", function () {
        console.log("change");
        clearTimeout(wto);
        wto = setTimeout(function () {
            update();
        }, 500);
    });
}

$(document).ready(function(){
    $(document).on('click', '.waypoint', function(){
        var newRow = `
        <tr class="points">
            <td></td>
            <td><input placeholder='X'></td>
            <td><input placeholder='Y'></td>
            <td><input placeholder='Speed'></td>
            <td class='comments'><input placeholder='Comments'></td>
            <td><button onclick='$(this).parent().parent().remove();update()'>Delete</button></td>
        </tr>`;
        $(this).closest("table").find(".points:last").after(newRow);
        addTableDnD();
    });
});

function update() {
    routines = [[]];
    var a;
    var routinesTable = document.getElementById("Routines");
    for (a = 1; a < routinesTable.rows.length; a++) {
        routines.push([]);
        var tableNum = a - 1;
        var tables = document.getElementById(tableNum.toString());
        var i;
        for (i = 1; i < tables.rows.length - 1; i++) {
            var x = tables.rows[i].cells[1].querySelector('input').value;
            var y = tables.rows[i].cells[2].querySelector('input').value;
            var speed = tables.rows[i].cells[3].querySelector('input').value;
            var comments = tables.rows[i].cells[4].querySelector('input').value;
            var waypoint = {x, y, speed, comments};
            routines[a - 1].push(waypoint);
        }
    }
    console.log(routines);
    drawLines();
}

function drawRobot() {
    if (waypoints.length > 1) {
        var deltaStart = Translation2d.diff(waypoints[0].position, waypoints[1].position);
        drawRotatedRect(waypoints[0].position, robotHeight, robotWidth, deltaStart.angle, getColorForSpeed(waypoints[1].speed));

        var deltaEnd = Translation2d.diff(waypoints[waypoints.length - 2].position, waypoints[waypoints.length - 1].position);
        drawRotatedRect(waypoints[waypoints.length - 1].position, robotHeight, robotWidth, deltaEnd.angle, getColorForSpeed(0));
    }
}

function drawRotatedRect(pos, w, h, angle, strokeColor, fillColor, noFill) {
    w = w * (width / kFieldWidth);
    h = h * (height / kFieldHeight);
    fillColor = fillColor || "rgba(0,0,0,0)";
    //ctx.save();
    if (noFill == null || !noFill)
        ctx.beginPath();
    ctx.translate(pos.drawX, pos.drawY);
    ctx.rotate(angle);
    ctx.rect(-w / 2, -h / 2, w, h);
    ctx.fillStyle = fillColor;
    if (noFill == null || !noFill)
        ctx.fill();
    if (strokeColor != null) {
        ctx.strokeStyle = strokeColor;
        ctx.lineWidth = 4;
        ctx.stroke();
    }
    ctx.rotate(-angle);
    ctx.translate(-pos.drawX, -pos.drawY);
    //ctx.restore();

}


function getPoint(i) {
    if (i >= waypoints.length)
        return waypoints[waypoints.length - 1];
    else
        return waypoints[i];
}

const depotFromLeftY = 71.5, depotFromRightY = 72.0, level2FromRightY = 97.5, level2FromLeftY = 97.75,
    level1FromLeftY = 86.75, level1FromRightY = 89.75, cargoOffsetX = 40.0, cargoOffsetY = 14.0,
    level1CargoX = 126.75, cargoLeftY = 133.0, cargoRightY = 134.75, midLineLeftRocketFarX = 70.5,
    midLineRightRocketFarX = 73.25, habLeftRocketCloseX = 117.0, habRightRocketCloseX = 113.0,
    habLeftRocketMidX = 133.5, habRightRocketMidX = 134.5, leftRocketFarY = 12.25, rightRocketFarY = 22.5,
    leftRocketMidY = 24.75, rightRocketMidY = 34.5, leftRocketCloseY = 13.25, rightRocketCloseY = 21.75,
    leftLoadingY = 26.0, rightLoadingY = 25.5, fieldWidth = 324.0, kUpperPlatformLength = 48.0, kLevel1Width = 150.0,
    kLevel2Width = 40.0, kLevel3Width = 48.0, kLowerPlatformLength = 48.0, kCargoLineGap = 21.5,
    kRobotWidthInches = 32.5, kRobotLengthInches = 40.25;

function importData() {
    $('#upl').click();
    let u = $('#upl')[0];
    $('#upl').change(() => {
        var file = u.files[0];
        console.log(file)
        var fr = new FileReader();
        fr.onload = function (e) {
            var c = fr.result;

            var name = file.name;

            // var x = NaN
            // var y = NaN
            //
            // var blue = NaN

            // console.log(name[name.length - 5])

            // if (name[name.length - 5] == "d") {
            // 	// red
            // 	blue = false
            // }
            // else {
            // 	blue = true
            // }

            // if (name[0] == "C" || name[0] == "B") {
            // 	x = 18
            // 	y = 175
            // }
            // else if (name[0] == "L") {
            // 	x = 18
            // 	y = 290
            // }
            // else {
            // 	x = 18
            // 	y = 56
            // }

            lines = c.split(/\r?\n/);
            // console.log(lines)
            const constants = new Map();
            $("tbody").empty();
            lines.forEach((wpd) => {
                data = wpd;

                if (data.includes("//")) {
                    // skip comments
                } else if (data.includes("final double ")) {
                    var v = data.replace("final double ", '');
                    v = v.split(" = ");
                    v[1] = v[1].replace(/sDistances./g, '');
                    v[1] = v[1].replace(/PhysicalConstants./g, '');
                    console.log(typeof(eval(v[1])));
                    v[0] = v[0].split('k');
                    v[0][1] = "k" + v[0][1];
                    constants.set(v[0][1], eval(v[1]));
                    console.log(constants)
                } else if (data.includes(".add(new Path.")) {
                    data.split("add.");
                    var wp = data;
                    wp = wp.replace("Path.", " ");
                    wp = wp.replace("StartToCargoShip.add", " ");
                    var keys = constants.keys();
                    var i;
                    var check = constants.keys();
                    console.log(constants.get("kHabLineX"));
                    console.log(constants);
                    for (i = 0; i < constants.size; i++) {
                        check = keys.next().value;
                        console.log(check);
                        if (wp.includes(String(check))) {
                            var a = constants.get(check);
                            wp = wp.replace(String(check), a);
                        }
                    }
                    console.log(wp);
                    wp = eval(wp);

                    // wp = eval("(new Waypoint(new Translation2d(0,0),0,));");
                    // data = data.split(".add(new Path.Waypoint(new Translation2d(");
                    // cordinate = data[1];
                    // cordinate = cordinate.split(", ");
                    // cordinate[1] = cordinate[1].replace(")", '');
                    // cordinate[2] = cordinate[2].replace("));", '');
                    // console.log("LOADED")
                    // console.log(data)
                    // // var wp = undefined
                    // // if (blue) {
                    // wp = new Waypoint(new Translation2d(parseFloat(cordinate[0]) + x, parseFloat(cordinate[1]) + y), cordinate[2], 0, "No Comment");
                    // }
                    // else {
                    // 	var x_off_red = 652
                    // 	wp = new Waypoint(new Translation2d(-1*parseFloat(data[0])-x + x_off_red, parseFloat(data[1])+y), data[2], 20, "No Comment");
                    // }

                    // console.log(wp);
                    //TODO: need fixing
                    $("tbody").append("<tr>"
                        + "<td><input value='" + wp.position.x + "'></td>"
                        + "<td><input value='" + wp.position.y + "'></td>"
                        + "<td><input value='" + wp.speed + "'></td>"
                        + "<td class='comments'><input placeholder='Comments' value='" + wp.comment + "'></td>"
                        + "<td><button onclick='$(this).parent().parent().remove();''>Delete</button></td></tr>"
                    );
                }
            })
            update();
            $('input').unbind("change paste keyup");
            $('input').bind("change paste keyup", function () {
                console.log("change");
                clearTimeout(wto);
                wto = setTimeout(function () {
                    update();
                }, 500);
            });
        }
        fr.readAsText(file);
    });
}

function getDataString() {
    var title = ($("#title").val().length > 0) ? $("#title").val() : "UntitledPath";
    var pathInit = "";
    for (var i = 0; i < waypoints.length; i++) {
        pathInit += "        " + waypoints[i].toString() + "\n";
    }
    var startPoint = "new Translation2d(" + waypoints[0].position.x + ", " + waypoints[0].position.y + ")";
    var importStr = "WAYPOINT_DATA: " + JSON.stringify(waypoints);
    var isReversed = $("#isReversed").is(':checked');
    var strStart = `package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.config.constants.PhysicalConstants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

public class ${title} extends AutoModeBase {

    @Override
    public String toString() {
        return sAlliance + this.getClass().toString();
    }

    @Override
    public void preStart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine();
    }
`;
    var strRoutines = `
    `;
    var strEnd = `@Override
    public String getKey() {
        return sAlliance.toString();
    }

	// ${importStr}
	// IS_REVERSED: ${isReversed}
	// FILE_NAME: ${title}
}`;
    return strStart + strRoutines + strEnd;
}

function exportData() {
    update();
    var title = ($("#title").val().length > 0) ? $("#title").val() : "UntitledPath";
    var blob = new Blob([getDataString()], {type: "text/plain;charset=utf-8"});
    saveAs(blob, title + ".java");
}

function showData() {
    update();
    var title = ($("#title").val().length > 0) ? $("#title").val() : "UntitledPath";
    $("#modalTitle").html(title + ".java");
    $(".modal > pre").text(getDataString());
    showModal();
}

function showModal() {
    $(".modal, .shade").removeClass("behind");
    $(".modal, .shade").removeClass("hide");
}

function closeModal() {
    $(".modal, .shade").addClass("hide");
    setTimeout(function () {
        $(".modal, .shade").addClass("behind");
    }, 500);
}

var flipped = false;

function flipField() {
    flipped = !flipped;
    if (flipped)
        ctx.drawImage(imageFlipped, 0, 0, width, height);
    else
        ctx.drawImage(image, 0, 0, width, height);
    update();
}

function lerpColor(color1, color2, factor) {
    var result = color1.slice();
    for (var i = 0; i < 3; i++) {
        result[i] = Math.round(result[i] + factor * (color2[i] - color1[i]));
    }
    return result;
}

function getColorForSpeed(speed) {
    var u = Math.max(0, Math.min(1, speed / maxSpeed));
    if (u < 0.5)
        return RGBToHex(lerpColor(minSpeedColor, [255, 255, 0], u * 2));
    return RGBToHex(lerpColor([255, 255, 0], maxSpeedColor, u * 2 - 1));

}

function hexToRGB(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? [
        parseInt(result[1], 16),
        parseInt(result[2], 16),
        parseInt(result[3], 16)
    ] : null;
}

function RGBToHex(rgb) {
    return "#" + ((1 << 24) + (rgb[0] << 16) + (rgb[1] << 8) + rgb[2]).toString(16).slice(1);
}

function getNextSpeed(prev) {
    for (var i = 0; i < waypoints.length - 1; i++) {
        if (waypoints[i] == prev)
            return waypoints[i + 1].speed;
    }
    return 0;
}
