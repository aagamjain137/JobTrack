<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Create Account - JobTrack" scope="request" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="auth-header">
            <h1>Create Account</h1>
            <p class="auth-subtitle">Start tracking your career opportunities today</p>
        </div>

        <%-- Server-side Error Display --%>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <span>&#9888;</span>
                <div><c:out value="${errorMessage}" /></div>
            </div>
        </c:if>

        <form id="registerForm" action="${pageContext.request.contextPath}/register" method="post" novalidate>
            <%-- Mandatory Hidden CSRF Token --%>
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />

            <div class="form-group">
                <label for="name" class="form-label">Full Name <span class="required">*</span></label>
                <input type="text" id="name" name="name" class="form-control"
                       placeholder="e.g. Jane Doe"
                       value="<c:out value="${name}" />" required autofocus />
            </div>

            <div class="form-group">
                <label for="email" class="form-label">Email Address <span class="required">*</span></label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="jane@example.com"
                       value="<c:out value="${email}" />" required />
            </div>

            <div class="form-group">
                <label for="password" class="form-label">Password <span class="required">*</span></label>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="Create a strong password" required />
                <div class="form-hint">Must be &ge;8 chars with uppercase, lowercase, digit, and special symbol.</div>
            </div>

            <div class="form-group">
                <label for="confirmPassword" class="form-label">Confirm Password <span class="required">*</span></label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control"
                       placeholder="Re-type your password" required />
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="margin-top: 1.5rem; font-size: 1rem;">
                Create Account
            </button>
        </form>

        <div class="auth-footer">
            Already have an account?
            <a href="${pageContext.request.contextPath}/login" style="font-weight: 600;">Sign In</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
