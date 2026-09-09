package com.patricia.paymentSimulator;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
public class SimulatorState {

    private volatile boolean autoConsume = true;

}

