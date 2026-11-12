<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- If user is already authenticated, redirect straight to dashboard --%>
<c:if test="${not empty sessionScope.userId}">
    <c:redirect url="/dashboard" />
</c:if>

<c:set var="pageTitle" value="JobTrack - Smart Application & Interview Platform" scope="request" />
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<section class="hero-section">
    <span class="hero-pill">&#10024; Production-Grade Career Management</span>
    <h1 class="hero-title">Track every job application with <span class="hero-highlight">confidence</span>.</h1>
    <p class="hero-desc">
        Streamline your career search. Track status workflows, schedule interview rounds,
        and gain instant visibility into your application pipeline with enterprise-grade security.
    </p>

    <div class="hero-actions">
        <a href="${pageContext.request.contextPath}/register" class="btn btn-primary" style="padding: 0.85rem 1.75rem; font-size: 1.05rem;">
            Create Free Account &rarr;
        </a>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary" style="padding: 0.85rem 1.75rem; font-size: 1.05rem;">
            Sign In to Dashboard
        </a>
    </div>

    <div class="features-grid">
        <div class="feature-card">
            <div class="feature-icon">&#128202;</div>
            <h3>Pipeline Analytics</h3>
            <p>Live status distribution cards covering Applied, Assessment, Interview, Selected, and Rejected stages.</p>
        </div>

        <div class="feature-card">
            <div class="feature-icon">&#128197;</div>
            <h3>Interview Timeline</h3>
            <p>Schedule multi-round technical and HR screens with specific dates, timestamps, and actionable notes.</p>
        </div>

        <div class="feature-card">
            <div class="feature-icon">&#128274;</div>
            <h3>Enterprise Security</h3>
            <p>Salted BCrypt password hashing, constant-time CSRF token validation, and complete multi-tenant IDOR protection.</p>
        </div>
    </div>
</section>

<jsp:include page="/WEB-INF/views/includes/footer.jsp" />
