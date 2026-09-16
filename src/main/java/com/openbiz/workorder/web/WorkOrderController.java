package com.openbiz.workorder.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openbiz.saas.api.TenantService;
import com.openbiz.service.api.WorkOrderService;
import com.openbiz.service.domain.OpenbizWorkOrder;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Independent HTTP consumer of OpenBiz WorkOrderService (Maven artifact).
 * Independent HTTP consumer of OpenBiz WorkOrderService (Maven artifact).
 * No local Mapper / SQL / tenant filter / status machine / assignee check.
 */
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController
{
    private final WorkOrderService workOrderService;
    private final TenantService tenantService;

    public WorkOrderController(WorkOrderService workOrderService, TenantService tenantService)
    {
        this.workOrderService = workOrderService;
        this.tenantService = tenantService;
    }

    @GetMapping
    public AjaxResult list()
    {
        List<OpenbizWorkOrder> rows = workOrderService.list();
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("workOrders", rows.stream().map(this::toView).collect(Collectors.toList()));
        return AjaxResult.success(data);
    }

    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable("id") Long id)
    {
        OpenbizWorkOrder wo = workOrderService.get(id);
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("workOrder", toView(wo));
        return AjaxResult.success(data);
    }

    @PostMapping
    public AjaxResult create(@RequestBody Map<String, Object> body)
    {
        OpenbizWorkOrder wo = workOrderService.create(
                str(body, "title"),
                str(body, "content"),
                str(body, "contactName"),
                str(body, "contactPhone"),
                str(body, "idempotentKey"));
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("workOrder", toView(wo));
        return AjaxResult.success(data);
    }

    /**
 * Calls WorkOrderService.assign(workOrderId, assigneeUserId).
 * Does not accept client tenantId or set status locally.
 */
    @PostMapping("/{id}/assign")
    public AjaxResult assign(@PathVariable("id") Long id, @RequestBody Map<String, Object> body)
    {
        Long assigneeUserId = longVal(body, "assigneeUserId");
        OpenbizWorkOrder wo = workOrderService.assign(id, assigneeUserId);
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("workOrder", toView(wo));
        return AjaxResult.success(data);
    }

    /**
 * Calls WorkOrderService.accept(workOrderId).
 * Does not accept client tenantId, assignee, or set status locally.
 */
    @PostMapping("/{id}/accept")
    public AjaxResult accept(@PathVariable("id") Long id)
    {
        OpenbizWorkOrder wo = workOrderService.accept(id);
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("workOrder", toView(wo));
        return AjaxResult.success(data);
    }

    /**
 * Calls WorkOrderService.complete(workOrderId, completeNote).
 * Does not accept client tenantId, assignee, or set status locally.
 */
    @PostMapping("/{id}/complete")
    public AjaxResult complete(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, Object> body)
    {
        String note = str(body, "completeNote");
        OpenbizWorkOrder wo = workOrderService.complete(id, note);
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("workOrder", toView(wo));
        return AjaxResult.success(data);
    }

    /**
 * Calls WorkOrderService.cancel(workOrderId).
 * Does not accept client tenantId or set status locally.
 */
    @PostMapping("/{id}/cancel")
    public AjaxResult cancel(@PathVariable("id") Long id)
    {
        OpenbizWorkOrder wo = workOrderService.cancel(id);
        Map<String, Object> data = new HashMap<>(4);
        data.put("tenantId", tenantService.currentTenantId());
        data.put("workOrder", toView(wo));
        return AjaxResult.success(data);
    }

    private Map<String, Object> toView(OpenbizWorkOrder wo)
    {
        Map<String, Object> m = new HashMap<>(12);
        m.put("id", wo.getId());
        m.put("tenantId", wo.getTenantId());
        m.put("title", wo.getTitle());
        m.put("content", wo.getContent());
        m.put("status", wo.getStatus());
        m.put("assigneeUserId", wo.getAssigneeUserId());
        m.put("createTime", wo.getCreateTime());
        return m;
    }

    private static String str(Map<String, Object> body, String key)
    {
        if (body == null || body.get(key) == null)
        {
            return null;
        }
        return String.valueOf(body.get(key));
    }

    private static Long longVal(Map<String, Object> body, String key)
    {
        if (body == null || body.get(key) == null)
        {
            return null;
        }
        return Long.valueOf(String.valueOf(body.get(key)));
    }
}

