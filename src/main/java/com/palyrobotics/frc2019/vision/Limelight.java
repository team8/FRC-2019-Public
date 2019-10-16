package com.palyrobotics.frc2019.vision;

import com.palyrobotics.frc2019.config.Constants.OtherConstants;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;
import com.palyrobotics.frc2019.vision.LimelightControlMode.*;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Notifier;

/**
 * Wrapper around the Limelight's network tables
 */
public class Limelight {

    private static Limelight sInstance = new Limelight();

    public static Limelight getInstance() {
        return sInstance;
    }

    private NetworkTable mTable;

    public Limelight() {
        mTable = NetworkTableInstance.getDefault().getTable("limelight");
    }

    /**
     * @return tv Whether the limelight has any valid targets (0 or 1)
     */
    public boolean isTargetFound() {
        return mTable.getEntry("tv").getDouble(0.0) != 0.0;
    }

    /**
     * @return tx Horizontal Offset From Crosshair To Target (-27 degrees to 27 degrees)
     */
    public double getYawToTarget() {
        return mTable.getEntry("tx").getDouble(0.0);
    }

    /**
     * @return ty Vertical Offset From Crosshair To Target (-20.5 degrees to 20.5 degrees)
     */
    public double getPitchToTarget() {
        NetworkTableEntry ty = mTable.getEntry("ty");
        return ty.getDouble(0.0);
    }

    /**
     * @return tshort Side length of shortest side of the fitted bounding box (pixels)
     */
    public double getTargetWidth() {
        return mTable.getEntry("tshort").getDouble(0.0);
    }

    /**
     * @return tshort Side length of longest side of the fitted bounding box (pixels)
     */
    public double getTargetLength() {
        return mTable.getEntry("tlong").getDouble(0.0);
    }

    /**
     * @return aspect Ratio of width to height of the fitted bounding box
     */
    public double getTargetAspectRatio() {
        return getTargetWidth() / getTargetLength();
    }

    /**
     * @return ta Target Area (0% of image to 100% of image)
     */
    public double getTargetArea() {
        return mTable.getEntry("ta").getDouble(0.0);
    }

    /**
     * @return ts Skew or rotation (-90 degrees to 0 degrees)
     */
    public double getSkew() {
        return mTable.getEntry("ts").getDouble(0.0);
    }

    /**
     * @return tl The pipeline’s latency contribution (ms) Add at least 11ms for image capture latency.
     */
    public double getPipelineLatency() {
        return mTable.getEntry("tl").getDouble(0.0);
    }

    private void resetPipelineLatency() {
        mTable.getEntry("tl").setValue(0.0);
    }
    //Setters

    /**
     * LedMode  Sets limelight’s LED state
     *
     * @param ledMode
     */
    public void setLEDMode(LedMode ledMode) {
        mTable.getEntry("ledMode").setValue(ledMode.getValue());
    }

    /**
     * @return LedMode current LED mode of the Limelight
     */
    public LedMode getLEDMode() {
        return LedMode.getByValue(mTable.getEntry("ledMode").getDouble(0.0));
    }

    /**
     * camMode  Sets Limelight’s operation mode
     * <p>
     * VISION
     * DRIVER (Increases exposure, disables vision processing)
     *
     * @param camMode
     */
    public void setCamMode(CamMode camMode) {
        mTable.getEntry("camMode").setValue(camMode.getValue());
    }

    /**
     * @return CamMode current camera mode of the Limelight
     */
    public CamMode getCamMode() {
        return CamMode.getByValue(mTable.getEntry("camMode").getDouble(0.0));
    }

    /**
     * pipeline Sets Limelight’s current pipeline
     * <p>
     * 0 . 9	Select pipeline 0.9
     *
     * @param pipeline
     */
    public void setPipeline(int pipeline) {
        if (pipeline < 0) {
            throw new IllegalArgumentException("Pipeline can not be less than zero");
        } else if (pipeline > 9) {
            throw new IllegalArgumentException("Pipeline can not be greater than nine");
        }
        mTable.getEntry("pipeline").setValue(pipeline);
    }

    /**
     * Returns
     *
     * @return pipeline current pipeline of the Lime Light
     */
    public int getPipeline() {
        return (int) mTable.getEntry("pipeline").getDouble(0.0);
    }

    /**
     * stream   Sets limelight’s streaming mode
     * <p>
     * kStandard - Side-by-side streams if a webcam is attached to Limelight
     * kPiPMain - The secondary camera stream is placed in the lower-right corner of the primary camera stream
     * kPiPSecondary - The primary camera stream is placed in the lower-right corner of the secondary camera stream
     *
     * @param stream
     */
    public void setStream(StreamType stream) {
        mTable.getEntry("stream").setValue(stream.getValue());
    }

    public StreamType getStream() {
        return StreamType.getByValue(mTable.getEntry("stream").getDouble(0.0));
    }


    /**
     * snapshot Allows users to take snapshots during a match
     * <p>
     * kon - Stop taking snapshots
     * koff - Take two snapshots per second
     *
     * @param snapshot
     */
    public void setSnapshot(Snapshot snapshot) {
        mTable.getEntry("snapshot").setValue(snapshot.getValue());
    }

    public Snapshot getSnapshot() {
        return Snapshot.getByValue(mTable.getEntry("snapshot").getDouble(0.0));
    }

    // *************** Advanced Usage with Raw Contours *********************

    /**
     * Limelight posts three raw contours to NetworkTables that are not influenced by your grouping mode.
     * That is, they are filtered with your pipeline parameters, but never grouped. X and Y are returned
     * in normalized screen space (-1 to 1) rather than degrees.	 *
     */

    public double getYawToTargetAdvanced(AdvancedTarget raw) {
        return mTable.getEntry("tx" + raw.getValue()).getDouble(0.0);
    }

    public double getPitchToTargetAdvanced(AdvancedTarget raw) {
        return mTable.getEntry("ty" + raw.getValue()).getDouble(0.0);
    }

    public double getTargetAreaAdvanced(AdvancedTarget raw) {
        return mTable.getEntry("ta" + raw.getValue()).getDouble(0.0);
    }

    public double getSkewRotationAdvanced(AdvancedTarget raw) {
        return mTable.getEntry("ts" + raw.getValue()).getDouble(0.0);
    }

    public double[] getCornerX() {
        return mTable.getEntry("tcornx").getDoubleArray(new double[0]);
    }

    public double[] getCornerY() {
        return mTable.getEntry("tcorny").getDoubleArray(new double[0]);
    }

    //Raw Crosshairs:
    //If you are using raw targeting data, you can still utilize your calibrated crosshairs:

    public double[] getRawCrosshair(AdvancedCrosshair raw) {
        double[] crosshairs = new double[2];
        crosshairs[0] = getRawCrosshairX(raw);
        crosshairs[1] = getRawCrosshairY(raw);
        return crosshairs;
    }

    public double getRawCrosshairX(AdvancedCrosshair raw) {
        return mTable.getEntry("cx" + raw.getValue()).getDouble(0.0);
    }

    public double getRawCrosshairY(AdvancedCrosshair raw) {
        return mTable.getEntry("cy" + raw.getValue()).getDouble(0.0);
    }

    /**
     * Estimate z distance from camera to distance as seen at http://docs.limelightvision.io/en/latest/cs_estimating_distance.html
     *
     * @return dist - the estimated distance
     */
    public double getEstimatedDistanceZ() {
        double a1 = OtherConstants.kLimelightElevationAngleDegrees;
        double a2 = this.getPitchToTarget();
        double h1 = OtherConstants.kLimelightHeightInches;
        double h2 = OtherConstants.kRocketHatchTargetHeight;
        double tx = this.getYawToTarget();
        //Logger.getInstance().logRobotThread(Level.INFO, "a1: " + a1 + " a2: " + a2 + " h1: " + h1 + " h2: " + h2);
        return ((h2 - h1) / Math.tan(Math.toRadians(a1 + a2))) - 10; // 10 = limelight's offset from front of robot
    }

    /**
     * Estimate z distance from camera to distance as seen at http://docs.limelightvision.io/en/latest/cs_estimating_distance.html
     * but with divide by cos(tx) to make up for difference in the distance prediction when the robot is rotated
     * (Experimental)
     *
     * @return dist - the estimated distance
     */
    public double getCorrectedEstimatedDistanceZ() {
        double a1 = OtherConstants.kLimelightElevationAngleDegrees;
        double a2 = this.getPitchToTarget();
        double h1 = OtherConstants.kLimelightHeightInches;
        double h2 = OtherConstants.kRocketHatchTargetHeight;
        double tx = this.getYawToTarget();
        //Logger.getInstance().logRobotThread(Level.INFO, "a1: " + a1 + " a2: " + a2 + " h1: " + h1 + " h2: " + h2);
        // Avoid divide by zero
        return Math.max(OtherConstants.kLimelightMinDistance, ((h2 - h1) / Math.tan(Math.toRadians(a1 + a2))));
    }

    /**
     * Estimate z distance using a rational function determined with experimental data
     */
    public double getRegressionDistanceZ() {
        return 24.6 * Math.pow(this.getTargetArea(), -0.64);
    }

    // SolvePnP outputs

    public double getPnPTranslationX() {
        return mTable.getEntry("camtran").getDoubleArray(new double[]{0, 0, 0, 0, 0, 0})[0];
    }

    public double getPnPTranslationY() {
        return mTable.getEntry("camtran").getDoubleArray(new double[]{0, 0, 0, 0, 0, 0})[1];
    }

    public double getPnPPitch() {
        return mTable.getEntry("camtran").getDoubleArray(new double[]{0, 0, 0, 0, 0, 0})[3];
    }

    public double getPnPYaw() {
        return mTable.getEntry("camtran").getDoubleArray(new double[]{0, 0, 0, 0, 0, 0})[4];
    }

    /**
     * Describes a vision target (position and angle)
     */
    public static class VisionTarget {
        public Translation2d position;
        public double angle;

        public VisionTarget(Translation2d position, double angle) {
            this.position = position;
            this.angle = angle;
        }
    }
}