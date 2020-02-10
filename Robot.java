/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Encoder;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.cameraserver.*;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

//CTRE junk

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
//import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 * pp
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  //DRIVE TRAIN junk

  private final WPI_VictorSPX m_leftMotor1 = new WPI_VictorSPX(Map.m_leftMotor1);
  private final WPI_VictorSPX m_leftMotor2 = new WPI_VictorSPX(Map.m_leftMotor2);
  private final WPI_VictorSPX m_rightMotor1 = new WPI_VictorSPX(Map.m_rightMotor1);
  private final WPI_VictorSPX m_rightMotor2 = new WPI_VictorSPX(Map.m_rightMotor2);

  private final WPI_TalonFX m_shooterMotor1 = new WPI_TalonFX(0);

  private final WPI_VictorSPX m_ArmMotor1 = new WPI_VictorSPX(0);

  private final SpeedController m_leftMotors = new SpeedControllerGroup(m_leftMotor1, m_leftMotor2);
  private final SpeedController m_rightMotors = new SpeedControllerGroup(m_rightMotor1, m_rightMotor2);

  private final DifferentialDrive m_driveTrain = new DifferentialDrive(m_leftMotors, m_rightMotors);

  private final Encoder m_leftEncoder = new Encoder(Map.m_leftEnc1, Map.m_leftEnc2);
  private final Encoder m_rightEncoder = new Encoder(Map.m_rightEnc1, Map.m_rightEnc2);

  //CONTROLLER junk

  private final XboxController m_driverController = new XboxController(Map.DRIVER_CONTROLLER);
  private final XboxController m_operatorController = new XboxController(Map.OPERATOR_CONTROLLER);

  //GAME TIMER

  private final Timer m_timer = new Timer();

  

  
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    CameraServer.getInstance()
      .startAutomaticCapture();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        if (m_timer.get() < 2.0){
          m_driveTrain.arcadeDrive(0.5, 0); //drive forawrd at half-speed
        } else {
          m_driveTrain.stopMotor(); //stops motors once 2 seconds have elapsed 
        }
        break;
    }
  }

  
  @Override
  public void teleopPeriodic() {
    final double triggerVal = (m_driverController.getTriggerAxis(Hand.kRight)
        - m_driverController.getTriggerAxis(Hand.kLeft)) * Map.DRIVING_SPEED;

    final double stick = (m_driverController.getX(Hand.kLeft)) * Map.TURNING_RATE;

    double left_command = (triggerVal + stick) * Map.DRIVING_SPEED;
    double right_command = (triggerVal - stick) * Map.DRIVING_SPEED;

    m_driveTrain.tankDrive(left_command, right_command);

    //arm junk 
    while(m_driverController.getAButton()); {
      if(m_driverController.getAButton()); {
        m_ArmMotor1.set(0.5); 
    } 
    
      }
  
      
    // tune for smooth robot movement junk
    final double kP = -0.05;
    final double min_command = 0.01;
    double steering_adjust = 0.0;
    final double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
    final double tv = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);


    if (m_driverController.getYButton()) {
      final double heading_error = -tx;
      if(tx>1.0){
        steering_adjust = kP * heading_error - min_command; 
      }
      else if(tx<1.0){
      steering_adjust = kP * heading_error + min_command; 
      }
    }
    left_command += steering_adjust;
    right_command -= steering_adjust; 
    m_driveTrain.tankDrive(left_command, right_command);

    if (m_driverController.getBButton()) { 
      final double heading_error = -tx;
      if(tv == 0.0)
      {
        //we dont seee the target, seek for the target by spinning in place at a safe speed
        steering_adjust = 0.5;
      }
      else 
      {
        if(tx>1.0){
          steering_adjust = kP * heading_error - min_command;
        }
        else if(tx<1.0){
          steering_adjust = kP * heading_error + min_command;
        }
        //we do see the target, excute aiming code
        //steering_adjust = kP * tx;
      }

      left_command+=steering_adjust;
      right_command-=steering_adjust;
      m_driveTrain.tankDrive(left_command, right_command);
    }

    if (m_driverController.getXButton()) { 
      final double heading_error = -tx;
      if(tv == 0.0)
      {
        //we dont seee the target, seek for the target by spinning in place at a safe speed
        steering_adjust = -0.5;
      }
      else 
      {
        if(tx<1.0){
          steering_adjust = kP * heading_error + min_command;
        }
        else if(tx>1.0){
          steering_adjust = kP * heading_error - min_command;
        }
        //we do see the target, excute aiming code
        //steering_adjust = kP * tx;
      }

      left_command+=steering_adjust;
      right_command-=steering_adjust;
      m_driveTrain.tankDrive(left_command, right_command);
    }

    if (m_driverController.getAButton()){
        m_shooterMotor1.set(1.0);
    }
    else{
        m_shooterMotor1.set(0.0);
    }

  }
  
  
  @Override
  public void testPeriodic() {
  }
}
