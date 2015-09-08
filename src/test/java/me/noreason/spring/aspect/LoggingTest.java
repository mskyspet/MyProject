package me.noreason.spring.aspect;

import me.noreason.spring.bean.Target;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by MSK on 2015/6/25.
 */
public class LoggingTest {
    private ApplicationContext context;
    @Before
    public void setUp(){
        context = new ClassPathXmlApplicationContext("bean.xml");
    }

    @Test
    public void execute(){
        Target target = context.getBean(Target.class);
        target.execute("hello");
    }
}
