<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Sign In - JobTrack" scope="request" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="auth-wrapper">
    <div class="auth-card">
        <div class="auth-header">
            <h1>Welcome Back</h1>
            <p class="auth-subtitle">Sign in to manage your career pipeline</p>
        </div>

        <%-- Flash Alert Notifications --%>
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

        <form action="${pageContext.request.contextPath}/login" method="post" novalidate>
            <%-- Mandatory Hidden CSRF Token --%>
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />

            <div class="form-group">
                <label for="email" class="form-label">Email Address <span class="required">*</span></label>
                <input type="email" id="email" name="email" class="form-control"
                       placeholder="name@example.com"
                       value="<c:out value="${email}" />" required autofocus />
            </div>

            <div class="form-group">
                <label for="password" class="form-label">Password <span class="required">*</span></label>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="Enter your password" required />
            </div>

            <button type="submit" class="btn btn-primary btn-block" style="margin-top: 1.5rem; font-size: 1rem;">
                Sign In
            </button>
        </form>

        <div class="auth-footer">
            Don't have an account yet?
            <a href="${pageContext.request.contextPath}/register" style="font-weight: 600;">Create one free</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
