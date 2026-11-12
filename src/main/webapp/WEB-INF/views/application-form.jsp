<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="isEdit" value="${formMode == 'edit'}" />
<c:set var="pageTitle" value="${isEdit ? 'Edit Application' : 'Add New Application'} - JobTrack" scope="request" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div style="max-width: 780px; margin: 0 auto;">
    <div style="margin-bottom: 1.5rem;">
        <a href="${pageContext.request.contextPath}/dashboard" style="font-size: 0.875rem; font-weight: 600;">
            &larr; Back to Dashboard
        </a>
    </div>

    <div class="card">
        <div class="card-header">
            <h1 class="card-title" style="font-size: 1.45rem;">
                <c:out value="${isEdit ? 'Edit Job Application' : 'Add New Job Application'}" />
            </h1>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <span>&#9888;</span>
                <div><c:out value="${errorMessage}" /></div>
            </div>
        </c:if>

        <form id="applicationForm" action="${pageContext.request.contextPath}/applications" method="post" novalidate>
            <%-- Mandatory Hidden CSRF Token --%>
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
            <input type="hidden" name="action" value="${isEdit ? 'update' : 'add'}" />

            <c:if test="${isEdit}">
                <input type="hidden" name="applicationId" value="${application.applicationId}" />
            </c:if>

            <div class="form-row">
                <div class="form-group">
                    <label for="companyName" class="form-label">Company Name <span class="required">*</span></label>
                    <input type="text" id="companyName" name="companyName" class="form-control"
                           placeholder="e.g. Google, Microsoft, TCS"
                           value="<c:out value="${isEdit ? application.companyName : companyName}" />" required autofocus />
                </div>

                <div class="form-group">
                    <label for="jobRole" class="form-label">Job Role / Title <span class="required">*</span></label>
                    <input type="text" id="jobRole" name="jobRole" class="form-control"
                           placeholder="e.g. Software Engineer"
                           value="<c:out value="${isEdit ? application.jobRole : jobRole}" />" required />
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="location" class="form-label">Location <span class="required">*</span></label>
                    <input type="text" id="location" name="location" class="form-control"
                           placeholder="e.g. Bengaluru, India (Remote or Hybrid)"
                           value="<c:out value="${isEdit ? application.location : location}" />" required />
                </div>

                <div class="form-group">
                    <label for="salary" class="form-label">Expected / Offered Salary (INR)</label>
                    <input type="number" step="0.01" min="0" id="salary" name="salary" class="form-control"
                           placeholder="e.g. 1200000.00"
                           value="<c:out value="${isEdit ? application.salary : salary}" />" />
                    <div class="form-hint">Optional. Annual CTC in INR (up to 2 decimal places).</div>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label for="appliedDate" class="form-label">Applied Date <span class="required">*</span></label>
                    <input type="date" id="appliedDate" name="appliedDate" class="form-control"
                           value="<c:out value="${isEdit ? application.appliedDate : appliedDate}" />" required />
                </div>

                <div class="form-group">
                    <label for="status" class="form-label">Current Pipeline Status <span class="required">*</span></label>
                    <c:set var="currentStatus" value="${isEdit ? application.status : (status != null ? status : 'APPLIED')}" />
                    <select id="status" name="status" class="form-control" required>
                        <option value="APPLIED" ${currentStatus == 'APPLIED' ? 'selected' : ''}>Applied</option>
                        <option value="ASSESSMENT" ${currentStatus == 'ASSESSMENT' ? 'selected' : ''}>Assessment</option>
                        <option value="INTERVIEW" ${currentStatus == 'INTERVIEW' ? 'selected' : ''}>Interview</option>
                        <option value="SELECTED" ${currentStatus == 'SELECTED' ? 'selected' : ''}>Selected</option>
                        <option value="REJECTED" ${currentStatus == 'REJECTED' ? 'selected' : ''}>Rejected</option>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label for="jobUrl" class="form-label">Job Posting URL</label>
                <input type="url" id="jobUrl" name="jobUrl" class="form-control"
                       placeholder="https://careers.company.com/job/12345"
                       value="<c:out value="${isEdit ? application.jobUrl : jobUrl}" />" />
                <div class="form-hint">Optional external link to the career listing.</div>
            </div>

            <div class="form-group">
                <label for="jobDescription" class="form-label">Job Description &amp; Notes</label>
                <textarea id="jobDescription" name="jobDescription" class="form-control" rows="4"
                          placeholder="Paste key qualifications, tech stack details, or referral notes..."><c:out value="${isEdit ? application.jobDescription : jobDescription}" /></textarea>
            </div>

            <div style="display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 1.5rem;">
                <a href="${pageContext.request.contextPath}${isEdit ? '/applications?action=view&id=' : '/dashboard'}${isEdit ? application.applicationId : ''}"
                   class="btn btn-secondary">
                    Cancel
                </a>
                <button type="submit" class="btn btn-primary">
                    <c:out value="${isEdit ? 'Save Changes' : 'Create Application'}" />
                </button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />