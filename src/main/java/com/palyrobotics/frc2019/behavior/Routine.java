package com.palyrobotics.frc2019.behavior;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveTimeRoutine;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorCustomPositioningRoutine;
import com.palyrobotics.frc2019.behavior.routines.shooter.ShooterExpelRoutine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.subsystems.*;

/**
 * Abstract superclass for a routine, which specifies an autonomous series of
 * actions <br />
 * Each routine takes in Commands and returns modified set points Requires the
 * specific subsystems
 *
 * @author Nihar; Team 254 - stop baiting mate aight chill finna change this
 *         class for json
 */
@JsonPropertyOrder ({ "@type" })
@JsonIgnoreProperties (ignoreUnknown = true)
@JsonTypeInfo (use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes ({ @JsonSubTypes.Type (value = BBTurnAngleRoutine.class, name = "drive.BBTurnAngleRoutine"),

		@JsonSubTypes.Type (value = ElevatorCustomPositioningRoutine.class, name = "elevator.ElevatorCustomPositioningRoutine"),
		@JsonSubTypes.Type (value = SequentialRoutine.class, name = "SequentialRoutine"),
		@JsonSubTypes.Type (value = ShooterExpelRoutine.class, name = "shooter.ShooterExpelRoutine"),
		@JsonSubTypes.Type (value = DriveTimeRoutine.class, name = "drive.DriveTimeRoutine"),
		@JsonSubTypes.Type (value = DrivePathRoutine.class, name = "drive.DrivePathRoutine"),
        @JsonSubTypes.Type (value = ParallelRoutine.class, name = "ParallelRoutine"),}

)
public abstract class Routine {

	// Keeps access to all subsystems to modify their output and read their status
	protected final Drive mDrive = Drive.getInstance();
	protected final Shooter mShooter = Shooter.getInstance();
	protected final Pusher mPusher = Pusher.getsInstance();
	protected final Elevator mElevator = Elevator.getInstance();
	protected final Fingers mFingers = Fingers.getInstance();
	protected final Intake mIntake = Intake.getInstance();
	protected final RobotState mRobotState = RobotState.getInstance();

	// Called to start a routine
	public abstract void start();

	// Update method, returns modified commands
	public abstract Commands update(Commands commands);

	// Called to stop a routine, should return modified commands if needed
	public abstract Commands cancel(Commands commands);

	// Notifies routine manager when routine is complete
	public abstract boolean isFinished();

	// Store subsystems which are required by this routine, preventing routines from
	// overlapping
	public abstract Subsystem[] getRequiredSubsystems();

	// Force override of getName()
	public abstract String getName();

	@Override
	public String toString() {
		return getName();
	}

}
