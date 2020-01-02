// canvas related variables
// references to canvas and its context and its position on the page
var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");
var $canvas = $("#canvas");
var canvasOffset = $canvas.offset();
var offsetX = canvasOffset.left;
var offsetY = canvasOffset.top;
var scrollX = $canvas.scrollLeft();
var scrollY = $canvas.scrollTop();
var cw = canvas.width;
var ch = canvas.height;

// flag to indicate a drag is in process
// and the last XY position that has already been processed
var isDown = false;
var lastX;
var lastY;

// the radian value of a full circle is used often, cache it
var PI2 = Math.PI * 2;

// variables relating to existing circles
var circles = [];
var stdRadius = 10;
var draggingCircle = -1;

// clear the canvas and redraw all existing circles
function drawAll() {
    ctx.clearRect(0, 0, cw, ch);
    for (var i = 0; i < circles.length; i++) {
        var circle = circles[i];
        ctx.beginPath();
        ctx.arc(circle.x, circle.y, circle.radius, 0, PI2);
        ctx.closePath();
        ctx.fillStyle = circle.color;
        ctx.fill();
    }
}

function drawPoint(x, y) {
    ctx.beginPath();
    ctx.strokeStyle = 'red';
    ctx.arc(x, y, 5, 0, 2 * Math.PI);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
    // circles.push({
    //     x: x,
    //     y: y,
    //     radius: 5,
    //     color: randomColor()
    // });
}
function handleMouseDown(e) {
    // tell the browser we'll handle this event
    e.preventDefault();
    e.stopPropagation();

    // save the mouse position
    // in case this becomes a drag operation
    lastX = parseInt(e.clientX - offsetX);
    lastY = parseInt(e.clientY - offsetY);

    // hit test all existing circles
    var hit = -1;
    for (var i = 0; i < circles.length; i++) {
        var circle = circles[i];
        var dx = lastX - circle.x;
        var dy = lastY - circle.y;
        if (dx * dx + dy * dy < circle.radius * circle.radius) {
            hit = i;
        }
    }

    // if no hits then add a circle
    // if hit then set the isDown flag to start a drag
    if (hit < 0) {
        circles.push({
            x: lastX,
            y: lastY,
            radius: stdRadius,
            color: randomColor()
        });
        drawAll();
    } else {
        draggingCircle = circles[hit];
        isDown = true;
    }

}

function handleMouseUp(e) {
    // tell the browser we'll handle this event
    e.preventDefault();
    e.stopPropagation();

    // stop the drag
    isDown = false;
}

function handleMouseMove(e) {

    // if we're not dragging, just exit
    if (!isDown) {
        return;
    }

    // tell the browser we'll handle this event
    e.preventDefault();
    e.stopPropagation();

    // get the current mouse position
    mouseX = parseInt(e.clientX - offsetX);
    mouseY = parseInt(e.clientY - offsetY);

    // calculate how far the mouse has moved
    // since the last mousemove event was processed
    var dx = mouseX - lastX;
    var dy = mouseY - lastY;

    // reset the lastX/Y to the current mouse position
    lastX = mouseX;
    lastY = mouseY;

    // change the target circles position by the
    // distance the mouse has moved since the last
    // mousemove event
    draggingCircle.x += dx;
    draggingCircle.y += dy;

    // redraw all the circles
    drawAll();
}

// listen for mouse events
$("#canvas").mousedown(function (e) {
    console.log("hi");
    handleMouseDown(e);
});
$("#canvas").mousemove(function (e) {
    handleMouseMove(e);
});
$("#canvas").mouseup(function (e) {
    handleMouseUp(e);
});
$("#canvas").mouseout(function (e) {
    handleMouseUp(e);
});

//////////////////////
// Utility functions

function randomColor() {
    return ('#' + Math.floor(Math.random() * 16777215).toString(16));
}