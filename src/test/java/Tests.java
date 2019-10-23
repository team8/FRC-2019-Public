//import com.palyrobotics.frc2019.util.control.Gains;
//import com.palyrobotics.frc2019.util.control.LazySparkMax;
//import com.palyrobotics.frc2019.util.control.SmartGains;
//import com.revrobotics.ControlType;
//import org.junit.Test;
//
//public class Tests {
//
//    @Test
//    public void test() {
//        var commands = Commands.reset();
//        var manager = new RoutineManager();
//        var operatorInterface = new OperatorInterface();
//        manager.update(operatorInterface.updateCommands(commands));
//        manager.reset(commands);
//    }
//
//    @Test
//    public void testLazySpark() {
//        var spark = new LazySparkMax(0);
//        var gains = new Gains();
//        gains.p = 0.2;
//        spark.set(ControlType.kPosition, 0.0, 0.0, gains);
//        gains.p = 0.3;
//        spark.set(ControlType.kPosition, 0.0, 0.0, gains);
//        gains = new Gains();
//        spark.set(ControlType.kPosition, 0.0, 0.0, gains);
//        spark.set(ControlType.kPosition, 0.0, 0.0, gains);
//        spark.set(ControlType.kPosition, 0.1, 0.0, gains);
//        spark.set(ControlType.kPosition, 0.1, 0.0, gains);
//        spark.set(ControlType.kPosition, 0.1, 0.1, gains);
//        spark.set(ControlType.kPosition, 0.1, 0.1, gains);
//        spark.set(ControlType.kSmartMotion, 0.1, 0.1, new SmartGains());
//    }
//}
