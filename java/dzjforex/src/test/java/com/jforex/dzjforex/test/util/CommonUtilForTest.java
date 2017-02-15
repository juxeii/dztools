package com.jforex.dzjforex.test.util;

import static org.mockito.MockitoAnnotations.initMocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.BDDMockito;

public class CommonUtilForTest extends BDDMockito {

    protected static final Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    public CommonUtilForTest() {
        initMocks(this);
    }
}
