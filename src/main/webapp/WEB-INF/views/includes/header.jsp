<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'JobTrack - Application Tracking Platform'}" /></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<header class="app-header">
    <div class="container navbar">
        <a href="${pageContext.request.contextPath}${not empty sessionScope.userId ? '/dashboard' : '/'}" class="nav-brand">
            <span class="brand-icon">&#128188;</span>
            <span>JobTrack</span>
        </a>

        <nav>
            <ul class="nav-links">
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <li>
                            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link ${pageActive == 'dashboard' ? 'active' : ''}">
                                Dashboard
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/applications?action=new" class="btn btn-primary btn-sm">
                                + Add Application
                            </a>
                        </li>
                        <li class="user-badge">
                            <span class="user-avatar">
                                <c:out value="${fn:toUpperCase(fn:substring(sessionScope.userName, 0, 1))}" default="U" />
                            </span>
                            <span><c:out value="${sessionScope.userName}" /></span>
                        </li>
                        <li>
                            <!-- Strict POST form with CSRF token for secure logout -->
                            <form action="${pageContext.request.contextPath}/logout" method="post" style="display:inline; margin:0;">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                                <button type="submit" class="btn btn-secondary btn-sm" title="End your session">
                                    Log out
                                </button>
                            </form>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <a href="${pageContext.request.contextPath}/login" class="nav-link">Log in</a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">Sign up</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </nav>
    </div>
</header>

<main class="main-content">
    <div class="container">
