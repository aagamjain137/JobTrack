<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="${application.companyName} - Details - JobTrack" scope="request" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div style="max-width: 960px; margin: 0 auto;">
    <div style="margin-bottom: 1.5rem; display: flex; align-items: center; justify-content: space-between;">
        <a href="${pageContext.request.contextPath}/dashboard" style="font-size: 0.875rem; font-weight: 600;">
            &larr; Back to Dashboard
        </a>
        <div style="display: flex; gap: 0.5rem;">
            <a href="${pageContext.request.contextPath}/applications?action=edit&id=${application.applicationId}" class="btn btn-secondary btn-sm">
                &#9998; Edit Application
            </a>
            <form action="${pageContext.request.contextPath}/applications" method="post" style="display:inline; margin:0;">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                <input type="hidden" name="action" value="delete" />
                <input type="hidden" name="id" value="${application.applicationId}" />
                <button type="submit" class="btn btn-sm btn-outline-danger"
                        data-confirm="Are you sure you want to permanently delete this application and all associated interview rounds?">
                    Delete
                </button>
            </form>
        </div>
    </div>

    <%-- Flash Alerts --%>
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

    <%-- Application Overview Card --%>
    <div class="card">
        <div class="card-header" style="align-items: flex-start;">
            <div>
                <h1 style="font-size: 1.75rem; margin-bottom: 0.25rem;">
                    <c:out value="${application.companyName}" />
                </h1>
                <div style="font-size: 1.1rem; color: var(--text-muted); font-weight: 500;">
                    <c:out value="${application.jobRole}" />
                </div>
            </div>
            <span class="badge badge-${application.status}" style="font-size: 0.85rem; padding: 0.35rem 0.9rem;">
                <span class="badge-dot"></span>
                <c:out value="${application.status}" />
            </span>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.25rem; margin-bottom: 1.5rem;">
            <div>
                <div style="font-size: 0.8rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Location</div>
                <div style="font-size: 1rem; font-weight: 600; color: var(--text-dark); margin-top: 0.2rem;">
                    <c:out value="${application.location}" />
                </div>
            </div>

            <div>
                <div style="font-size: 0.8rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Applied Date</div>
                <div style="font-size: 1rem; font-weight: 600; color: var(--text-dark); margin-top: 0.2rem;">
                    <c:out value="${application.appliedDate}" />
                </div>
            </div>

            <div>
                <div style="font-size: 0.8rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Compensation</div>
                <div style="font-size: 1rem; font-weight: 600; color: var(--text-dark); margin-top: 0.2rem;">
                    <c:choose>
                        <c:when test="${not empty application.salary}">
                            &#8377;<fmt:formatNumber value="${application.salary}" type="number" minFractionDigits="2" maxFractionDigits="2" />
                        </c:when>
                        <c:otherwise>
                            <span style="color: var(--text-light); font-weight: normal;">Not specified</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div>
                <div style="font-size: 0.8rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Posting URL</div>
                <div style="font-size: 0.95rem; margin-top: 0.2rem;">
                    <c:choose>
                        <c:when test="${not empty application.jobUrl}">
                            <a href="<c:out value="${application.jobUrl}" />" target="_blank" rel="noopener noreferrer" style="font-weight: 600;">
                                View Listing &nearr;
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span style="color: var(--text-light);">None</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <div style="margin-top: 1rem; border-top: 1px solid var(--border-color); padding-top: 1.25rem;">
            <h3 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.5rem;">Job Description &amp; Notes</h3>
            <c:choose>
                <c:when test="${not empty application.jobDescription}">
                    <div style="white-space: pre-wrap; background: var(--bg-page); padding: 1rem 1.25rem; border-radius: var(--radius-md); font-size: 0.925rem; line-height: 1.6; border: 1px solid var(--border-color);"><c:out value="${application.jobDescription}" /></div>
                </c:when>
                <c:otherwise>
                    <p style="color: var(--text-muted); font-style: italic; font-size: 0.9rem;">No job description or notes added.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Interview Rounds Tracker Section --%>
    <div class="card">
        <div class="card-header">
            <div>
                <h2 class="card-title">&#128197; Interview Rounds Tracker</h2>
                <p style="color: var(--text-muted); font-size: 0.875rem; margin: 0.25rem 0 0;">
                    Manage upcoming and completed interview stages for this application.
                </p>
            </div>
        </div>

        <%-- Existing Interview Rounds List --%>
        <c:choose>
            <c:when test="${empty interviews}">
                <div class="empty-state" style="padding: 2rem 1rem; margin-bottom: 1.5rem;">
                    <div style="font-size: 2rem; margin-bottom: 0.5rem; color: var(--text-light);">&#128197;</div>
                    <div style="font-weight: 600; color: var(--text-dark);">No interview rounds scheduled yet</div>
                    <div style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.25rem;">
                        Use the form below to add technical screens, coding tests, or HR interviews.
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive" style="margin-bottom: 1.75rem;">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Round / Stage</th>
                                <th>Scheduled Date &amp; Time</th>
                                <th>Remarks &amp; Feedback</th>
                                <th style="text-align: right;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${interviews}" var="interview">
                                <tr>
                                    <td style="font-weight: 700; color: var(--text-dark);">
                                        <c:out value="${interview.interviewRound}" />
                                    </td>
                                    <td>
                                        <span class="interview-date-badge">
                                            &#9200; <c:out value="${interview.interviewDate}" />
                                        </span>
                                    </td>
                                    <td style="font-size: 0.875rem; color: var(--text-body);">
                                        <c:choose>
                                            <c:when test="${not empty interview.remarks}">
                                                <c:out value="${interview.remarks}" />
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--text-light); font-style: italic;">&mdash;</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right;">
                                        <%-- Delete Interview Form with CSRF Token --%>
                                        <form action="${pageContext.request.contextPath}/interviews" method="post" style="display:inline; margin:0;">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                            <input type="hidden" name="action" value="delete" />
                                            <input type="hidden" name="id" value="${interview.interviewId}" />
                                            <input type="hidden" name="applicationId" value="${application.applicationId}" />
                                            <button type="submit" class="btn btn-sm btn-outline-danger"
                                                    data-confirm="Delete interview round '${interview.interviewRound}'?">
                                                Delete
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <%-- Inline Form: Schedule New Interview Round --%>
        <div style="background: var(--bg-page); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem;">
            <h3 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 1rem; color: var(--text-dark);">
                + Schedule New Interview Round
            </h3>

            <form action="${pageContext.request.contextPath}/interviews" method="post">
                <%-- Mandatory Hidden CSRF Token --%>
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                <input type="hidden" name="action" value="add" />
                <input type="hidden" name="applicationId" value="${application.applicationId}" />

                <div class="form-row">
                    <div class="form-group" style="flex: 2 1 300px;">
                        <label for="interviewRound" class="form-label">Round Name / Stage <span class="required">*</span></label>
                        <input type="text" id="interviewRound" name="interviewRound" class="form-control"
                               placeholder="e.g. Technical Round 1 - Live Coding" required />
                    </div>

                    <div class="form-group" style="flex: 1 1 240px;">
                        <label for="interviewDate" class="form-label">Date &amp; Time <span class="required">*</span></label>
                        <input type="datetime-local" id="interviewDate" name="interviewDate" class="form-control" required />
                    </div>
                </div>

                <div class="form-group">
                    <label for="remarks" class="form-label">Remarks / Preparation Notes</label>
                    <input type="text" id="remarks" name="remarks" class="form-control"
                           placeholder="e.g. Zoom link in email, focus on concurrency and system architecture" />
                </div>

                <div style="text-align: right; margin-top: 0.5rem;">
                    <button type="submit" class="btn btn-primary btn-sm">
                        Schedule Round
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />