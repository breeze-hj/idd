package eastwind.idd.support;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jan.huang on 2017/10/18.
 */
public interface DelayedExecutor {

    Logger LOGGER = LoggerFactory.getLogger(DelayedExecutor.class);

    default void delayExecute(long delay, Consumer<DelayedExecutor> task) {
    	DelayedTask delayedTask = DelayedTask.newDelayedTask(delay, task);
    	delayExecute(delayedTask);
    }
    
    void delayExecute(DelayedTask delayedTask);

    void cancel(DelayedTask delayedTask);

    default void execute(DelayedTask delayedTask) {
        try {
            if (!delayedTask.isCancel()) {
                delayedTask.getConsumer().accept(this);
            }
        } catch (Exception e) {
            LOGGER.error("execute delayed task error", e);
        }
    }

}
