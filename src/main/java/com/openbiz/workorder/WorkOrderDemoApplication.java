package com.openbiz.workorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import com.openbiz.saas.OpenBizSaasAutoConfiguration;
import com.openbiz.service.OpenBizServiceAutoConfiguration;

/**
 * Independent Spring Boot consumer of OpenBiz Service (Maven artifact).
 * Does not live inside open-biz-platform reactor.
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class }, scanBasePackages = {
        "com.ruoyi",
        "com.openbiz.workorder"
})
@Import({
        OpenBizSaasAutoConfiguration.class,
        OpenBizServiceAutoConfiguration.class
})
public class WorkOrderDemoApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(WorkOrderDemoApplication.class, args);
        System.out.println("OpenBiz WorkOrder Demo started on port 18081 (independent process)");
    }
}
