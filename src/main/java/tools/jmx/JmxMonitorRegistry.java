package tools.jmx;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import tools.Factory;
import tools.Registry;

public class JmxMonitorRegistry implements JmxMonitorRegistryMBean {

    private static volatile int COUNTER;
    
    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;

    private final Registry registry;

    public JmxMonitorRegistry(Factory factory, Registry registry) {
        logger = factory.createLogger(this.getClass());
        this.registry = registry;
    }

    public void register() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(this, new ObjectName("tools.JmxMonitor:type=JmxMonitor" + COUNTER++));
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex) {
            logger.log(Level.SEVERE, "Could not initialize JMX", ex);
        }
    }

    @Override
    public int getRegistrySize() {
        return registry.getKeys().size();
    }
}
