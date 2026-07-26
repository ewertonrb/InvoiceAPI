package com.invoice.invoice_api.payroll;

import com.invoice.invoice_api.model.ProjectRoleRate;
import com.invoice.invoice_api.model.WorkLog;

public interface PayrollEngine {
    PayrollCalculation calculate(WorkLog workLog, ProjectRoleRate projectRoleRate);
}
