
function drawLines() {
    var canvas = document.getElementById("field");
    const offsetX = 70;
    const offsetY = 165;
    clear();
    ctx.beginPath();
    ctx.moveTo(offsetX, offsetY);
    var a;
    for (a = 0; a < routines.length; a++) {
        var i;
        ctx.strokeStyle = randomColor();
        for (i = 0; i < routines[a].length; i++) {
            ctx.lineWidth = 20;
            var x = routines[a][i].x + offsetX;
            var y = routines[a][i].y + offsetY;
            ctx.lineTo(x, y);
            ctx.stroke();
            drawPoint(x, y);
        }
    }
}
function clear() {
    ctx.clearRect(0, 0, width, height);
    ctx.fillStyle = "#FF0000";
    if (flipped)
        ctx.drawImage(imageFlipped, 0, 0, width, height);
    else
        ctx.drawImage(image, 0, 0, width, height);
}