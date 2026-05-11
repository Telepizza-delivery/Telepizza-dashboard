package com.example.robot;

/**
 * Represents a delivery order: pickup block + delivery block.
 * Blocks are referenced by (row, col) grid coordinates.
 */
public class Order {

    public enum Status { PENDING, IN_PROGRESS, DELIVERED, FAILED }

    private final String id;
    private final int pickupRow, pickupCol;
    private final int deliveryRow, deliveryCol;
    private Status status;
    private double progress; // 0.0 - 1.0

    public Order(String id, int pickupRow, int pickupCol, int deliveryRow, int deliveryCol) {
        this.id          = id;
        this.pickupRow   = pickupRow;
        this.pickupCol   = pickupCol;
        this.deliveryRow = deliveryRow;
        this.deliveryCol = deliveryCol;
        this.status      = Status.PENDING;
        this.progress    = 0.0;
    }

    public String getId()            { return id; }
    public int getPickupRow()        { return pickupRow; }
    public int getPickupCol()        { return pickupCol; }
    public int getDeliveryRow()      { return deliveryRow; }
    public int getDeliveryCol()      { return deliveryCol; }
    public Status getStatus()        { return status; }
    public double getProgress()      { return progress; }

    public void setStatus(Status status)    { this.status = status; }
    public void setProgress(double p)       { this.progress = Math.max(0, Math.min(1, p)); }

    public String getPickupLabel()   { return "(" + pickupRow + "," + pickupCol + ")"; }
    public String getDeliveryLabel() { return "(" + deliveryRow + "," + deliveryCol + ")"; }

    @Override
    public String toString() {
        return id + " " + getPickupLabel() + " → " + getDeliveryLabel();
    }
}
