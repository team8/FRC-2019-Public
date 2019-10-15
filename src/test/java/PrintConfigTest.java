import com.palyrobotics.frc2019.config.DriveConfig;
import com.palyrobotics.frc2019.config.VisionConfig;
import com.palyrobotics.frc2019.util.config.Configs;
import org.junit.Test;

public class PrintConfigTest {

    @Test
    public void testPrintConfig() {
        System.out.println(Configs.get(DriveConfig.class));
        System.out.println(Configs.get(VisionConfig.class));
    }
}
