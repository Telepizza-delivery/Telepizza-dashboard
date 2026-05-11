package com.example.robot;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Renders the CityMap onto a JavaFX Canvas.
 * Call redraw() whenever the map or robot position changes.
 */
public class MapCanvas extends Canvas {

    private static final Color COL_BUILDING   = Color.web("#fff0f0");
    private static final Color COL_BUILDING_R = Color.web("#e24b4a");
    private static final Color COL_ROAD       = Color.web("#f0f8fd");
    private static final Color COL_PICKUP     = Color.web("#f0f8e8");
    private static final Color COL_GRID       = Color.web("#dddddd");
    private static final Color COL_STREET_G   = Color.web("#0e6e56");   // green lane line
    private static final Color COL_STREET_B   = Color.web("#1a5fa5");   // blue lane line
    private static final Color COL_SQUARE     = Color.web("#222222");   // corner black square
    private static final Color COL_ROBOT      = Color.web("#185FA5cc");
    private static final Color COL_ROBOT_TXT  = Color.WHITE;
    private static final Color COL_TILE_ID    = Color.web("#aaaaaa");

    private CityMap map;
    private int robotRow = -1;
    private int robotCol = -1;

    public MapCanvas(double width, double height) {
        super(width, height);
    }

    public void setMap(CityMap map) {
        this.map = map;
        redraw();
    }

    public void setRobotPosition(int row, int col) {
        this.robotRow = row;
        this.robotCol = col;
        redraw();
    }

    public void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        gc.clearRect(0, 0, w, h);

        if (map == null) {
            gc.setFill(Color.web("#f5f5f0"));
            gc.fillRect(0, 0, w, h);
            gc.setFill(Color.web("#888888"));
            gc.setFont(Font.font(14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Esperando mapa MQTT...", w / 2, h / 2);
            return;
        }

        double cellW = w / map.getCols();
        double cellH = h / map.getRows();

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                drawTile(gc, map.getTile(row, col), col * cellW, row * cellH, cellW, cellH, row, col);
            }
        }

        if (robotRow >= 0 && robotCol >= 0) {
            drawRobot(gc, robotRow, robotCol, cellW, cellH);
        }
    }

    private void drawTile(GraphicsContext gc, Tile tile, double x, double y, double w, double h,
                          int row, int col) {
        boolean pickup = tile.isPickupOrDelivery();

        if (tile.isBuilding()) {
            gc.setFill(COL_BUILDING);
            gc.fillRect(x, y, w, h);
            gc.setStroke(COL_GRID);
            gc.setLineWidth(0.5);
            gc.strokeRect(x, y, w, h);
            // Red circle
            gc.setStroke(COL_BUILDING_R);
            gc.setLineWidth(3);
            double r = Math.min(w, h) * 0.28;
            gc.strokeOval(x + w/2 - r, y + h/2 - r, r*2, r*2);
            return;
        }

        // Road cell background
        gc.setFill(pickup ? COL_PICKUP : COL_ROAD);
        gc.fillRect(x, y, w, h);
        gc.setStroke(COL_GRID);
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y, w, h);

        // Road fill bands (light blue)
        double roadFrac = 0.30;
        double rw = w * roadFrac, rh = h * roadFrac;
        double cx = x + w/2, cy = y + h/2;

        gc.setFill(Color.web("#e0f0ff"));
        if (tile.connectsLeft())  gc.fillRect(x, cy - rh/2, cx - x, rh);
        if (tile.connectsRight()) gc.fillRect(cx, cy - rh/2, x + w - cx, rh);
        if (tile.connectsUp())    gc.fillRect(cx - rw/2, y, rw, cy - y);
        if (tile.connectsDown())  gc.fillRect(cx - rw/2, cy, rw, y + h - cy);
        // Center square
        double cs = Math.max(rw, rh);
        gc.fillRect(cx - cs/2, cy - cs/2, cs, cs);

        // Green lane lines
        drawLaneLines(gc, tile, x, y, w, h, cx, cy, rw, rh, COL_STREET_G, 0.18, 1.8);
        // Blue lane lines (outer)
        drawLaneLines(gc, tile, x, y, w, h, cx, cy, rw, rh, COL_STREET_B, 0.36, 1.4);

        // Black corner squares
        double sq = rh * 0.28;
        gc.setFill(COL_SQUARE);
        gc.fillRect(cx - sq/2, cy - sq/2, sq, sq);

        // Pickup marker
        if (pickup) {
            gc.setFill(Color.web("#3B6D11"));
            gc.setFont(Font.font("Arial", 10));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText("P", x + w - 3, y + 12);
        }

        // Tile ID label (small, bottom-left)
        gc.setFill(COL_TILE_ID);
        gc.setFont(Font.font("Monospaced", 9));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(String.format("%02d", tile.getCode()), x + 2, y + h - 2);
    }

    private void drawLaneLines(GraphicsContext gc, Tile tile,
                               double x, double y, double w, double h,
                               double cx, double cy, double rw, double rh,
                               Color color, double offsetFrac, double lineWidth) {
        double off = rh * offsetFrac;
        gc.setStroke(color);
        gc.setLineWidth(lineWidth);

        if (tile.connectsLeft()) {
            gc.strokeLine(x, cy - off, cx - rw/2, cy - off);
            gc.strokeLine(x, cy + off, cx - rw/2, cy + off);
        }
        if (tile.connectsRight()) {
            gc.strokeLine(cx + rw/2, cy - off, x + w, cy - off);
            gc.strokeLine(cx + rw/2, cy + off, x + w, cy + off);
        }
        if (tile.connectsUp()) {
            gc.strokeLine(cx - off, y, cx - off, cy - rh/2);
            gc.strokeLine(cx + off, y, cx + off, cy - rh/2);
        }
        if (tile.connectsDown()) {
            gc.strokeLine(cx - off, cy + rh/2, cx - off, y + h);
            gc.strokeLine(cx + off, cy + rh/2, cx + off, y + h);
        }
    }

    private void drawRobot(GraphicsContext gc, int row, int col, double cellW, double cellH) {
        double cx = col * cellW + cellW / 2;
        double cy = row * cellH + cellH / 2;
        double r  = Math.min(cellW, cellH) * 0.20;

        gc.setFill(COL_ROBOT);
        gc.fillOval(cx - r, cy - r, r*2, r*2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(cx - r, cy - r, r*2, r*2);

        gc.setFill(COL_ROBOT_TXT);
        gc.setFont(Font.font("Arial", Font.getDefault().getSize() * 0.7));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("R", cx, cy + 4);
    }
}
