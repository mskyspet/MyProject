package me.noreason.spring.bean;

import me.noreason.spring.aspect.Loggable;
import org.springframework.stereotype.Component;

/**
 * Created by MSK on 2015/6/25.
 */
@Component
public class Target {

    public String execute(String value){
        System.out.println("inner method");
        return value.toLowerCase();
    }
}
