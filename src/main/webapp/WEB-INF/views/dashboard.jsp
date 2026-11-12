<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Dashboard - JobTrack" scope="request" />
<c:set var="pageActive" value="dashboard" scope="request" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<%-- Flash Messages --%>
<c:if test="${not empty successMessage}">
    <div class="alert alert-success">
        <span>&#10003;</span>
        <div><c:out value="${successMessage}" /></div>
    </div>
</c:if>

<c:if test="${not empty errorMessage}">
    <div class="alert alert-danger">
        <span>&#9888;</span>
        <div><c:out value="${errorMessage}" /></div>
    </div>
</c:if>

<%-- 1. KPI Metric Summary Cards --%>
<div class="stats-grid">
    <div class="stat-card total">
        <div class="stat-label">Total Applications</div>
        <div class="stat-value"><c:out value="${stats['TOTAL'] != null ? stats['TOTAL'] : 0}" /></div>
    </div>
    <div class="stat-card applied">
        <div class="stat-label">Applied</div>
        <div class="stat-value"><c:out value="${stats['APPLIED'] != null ? stats['APPLIED'] : 0}" /></div>
    </div>
    <div class="stat-card assessment">
        <div class="stat-label">Assessment</div>
        <div class="stat-value"><c:out value="${stats['ASSESSMENT'] != null ? stats['ASSESSMENT'] : 0}" /></div>
    </div>
    <div class="stat-card interview">
        <div class="stat-label">Interview</div>
        <div class="stat-value"><c:out value="${stats['INTERVIEW'] != null ? stats['INTERVIEW'] : 0}" /></div>
    </div>
    <div class="stat-card selected">
        <div class="stat-label">Selected</div>
        <div class="stat-value"><c:out value="${stats['SELECTED'] != null ? stats['SELECTED'] : 0}" /></div>
    </div>
    <div class="stat-card rejected">
        <div class="stat-label">Rejected</div>
        <div class="stat-value"><c:out value="${stats['REJECTED'] != null ? stats['REJECTED'] : 0}" /></div>
    </div>
</div>

<%-- 2. Search & Filter Bar --%>
<div class="filter-bar">
    <form action="${pageContext.request.contextPath}/dashboard" method="get" class="filter-form">
        <div class="search-input-wrapper">
            <input type="text" name="keyword" class="form-control"
                   placeholder="Search by company name or job role..."
                   value="<c:out value="${keyword}" />" />
        </div>

        <div class="filter-select">
            <select name="status" class="form-control">
                <option value="ALL" ${selectedStatus == 'ALL' ? 'selected' : ''}>All Statuses</option>
                <option value="APPLIED" ${selectedStatus == 'APPLIED' ? 'selected' : ''}>Applied</option>
                <option value="ASSESSMENT" ${selectedStatus == 'ASSESSMENT' ? 'selected' : ''}>Assessment</option>
                <option value="INTERVIEW" ${selectedStatus == 'INTERVIEW' ? 'selected' : ''}>Interview</option>
                <option value="SELECTED" ${selectedStatus == 'SELECTED' ? 'selected' : ''}>Selected</option>
                <option value="REJECTED" ${selectedStatus == 'REJECTED' ? 'selected' : ''}>Rejected</option>
            </select>
        </div>

        <button type="submit" class="btn btn-primary">
            Filter
        </button>

        <c:if test="${not empty keyword || (not empty selectedStatus && selectedStatus != 'ALL')}">
            <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary">
                Reset
            </a>
        </c:if>
    </form>
</div>

<%-- 3. Two-Column Dashboard Content --%>
<div class="dashboard-layout">

    <%-- Main Applications Table --%>
    <div>
        <div class="card" style="padding: 0; overflow: hidden;">
            <div style="padding: 1.25rem 1.5rem; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-color);">
                <h2 class="card-title" style="margin: 0;">Job Applications</h2>
                <a href="${pageContext.request.contextPath}/applications?action=new" class="btn btn-primary btn-sm">
                    + New Application
                </a>
            </div>

            <c:choose>
                <c:when test="${empty applications}">
                    <div class="empty-state">
                        <div class="empty-state-icon">&#128196;</div>
                        <h3 class="empty-state-title">No applications found</h3>
                        <p class="empty-state-desc">
                            <c:choose>
                                <c:when test="${not empty keyword || (not empty selectedStatus && selectedStatus != 'ALL')}">
                                    No records match your active search filter. Try clearing filters or searching for another term.
                                </c:when>
                                <c:otherwise>
                                    You haven't tracked any job applications yet. Click below to add your first application!
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <a href="${pageContext.request.contextPath}/applications?action=new" class="btn btn-primary">
                            + Add Job Application
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive" style="border: none; border-radius: 0;">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Company &amp; Role</th>
                                    <th>Location</th>
                                    <th>Salary</th>
                                    <th>Applied Date</th>
                                    <th>Status</th>
                                    <th style="text-align: right;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${applications}" var="app">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/applications?action=view&id=${app.applicationId}" class="company-title">
                                                <c:out value="${app.companyName}" />
                                            </a>
                                            <div class="role-sub"><c:out value="${app.jobRole}" /></div>
                                        </td>
                                        <td><c:out value="${app.location}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty app.salary}">
                                                    &#8377;<fmt:formatNumber value="${app.salary}" type="number" minFractionDigits="2" maxFractionDigits="2" />
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: var(--text-light);">&mdash;</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><c:out value="${app.appliedDate}" /></td>
                                        <td>
                                            <span class="badge badge-${app.status}">
                                                <span class="badge-dot"></span>
                                                <c:out value="${app.status}" />
                                            </span>
                                        </td>
                                        <td style="text-align: right;">
                                            <div class="actions-cell" style="justify-content: flex-end;">
                                                <a href="${pageContext.request.contextPath}/applications?action=view&id=${app.applicationId}" class="btn btn-secondary btn-sm" title="View details and interviews">
                                                    View
                                                </a>
                                                <a href="${pageContext.request.contextPath}/applications?action=edit&id=${app.applicationId}" class="btn btn-secondary btn-sm" title="Edit application">
                                                    Edit
                                                </a>
                                                <%-- POST Form for secure CSRF-protected deletion --%>
                                                <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline; margin:0;">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                                    <input type="hidden" name="action" value="delete" />
                                                    <input type="hidden" name="id" value="${app.applicationId}" />
                                                    <button type="submit" class="btn btn-sm btn-outline-danger"
                                                            data-confirm="Are you sure you want to delete '${app.companyName}'? All associated interviews will also be permanently deleted.">
                                                        Delete
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Sidebar: Upcoming Interviews --%>
    <div>
        <div class="card">
            <div class="card-header">
                <h3 class="card-title">&#128197; Upcoming Interviews</h3>
            </div>

            <c:choose>
                <c:when test="${empty upcomingInterviews}">
                    <div style="text-align: center; padding: 1.5rem 0; color: var(--text-muted); font-size: 0.9rem;">
                        <p>No upcoming interviews scheduled.</p>
                        <p class="form-hint">View an application and schedule rounds to see them here.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${upcomingInterviews}" var="interview">
                        <div class="interview-item">
                            <span class="interview-date-badge">
                                &#9200; <c:out value="${interview.interviewDate}" />
                            </span>
                            <div class="interview-round-name">
                                <c:out value="${interview.interviewRound}" />
                            </div>
                            <div style="font-size: 0.825rem; font-weight: 600; color: var(--text-body); margin-top: 0.2rem;">
                                <c:out value="${interview.companyName}" /> &bull; <c:out value="${interview.jobRole}" />
                            </div>
                            <c:if test="${not empty interview.remarks}">
                                <div class="interview-remarks">
                                    &ldquo;<c:out value="${interview.remarks}" />&rdquo;
                                </div>
                            </c:if>
                            <div style="margin-top: 0.5rem;">
                                <a href="${pageContext.request.contextPath}/applications?action=view&id=${interview.applicationId}" style="font-size: 0.8rem; font-weight: 600;">
                                    View Application &rarr;
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />