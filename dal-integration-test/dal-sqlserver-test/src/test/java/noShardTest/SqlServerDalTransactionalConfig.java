package noShardTest;

import com.ctrip.platform.dal.dao.annotation.EnableDalTransaction;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by lilj on 2017/9/29.
 */
@Configuration
@EnableDalTransaction
@ComponentScan(basePackages = "noShardTest")
public class SqlServerDalTransactionalConfig {

}
