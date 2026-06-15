package com.centroweg.iot.time_trial_api;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityVerificationTest {

    private final ApplicationModules modules =
            ApplicationModules.of(TimeTrialApiApplication.class);

    @Test
    void verifyModuleBoundaries() {
        modules.verify();
    }
}
