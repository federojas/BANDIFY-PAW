<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html>
<head>
    <title id="title"><spring:message code="title.auditionform"/></title>
    <c:import url="../config/generalHead.jsp"/>
    <c:import url="../config/materializeHead.jsp"/>
    <link rel="stylesheet" href="<c:url value="/resources/css/welcome.css" />" />
    <link rel="stylesheet" href="<c:url value="/resources/css/forms.css" />">
    <link rel="stylesheet" href="<c:url value="/resources/css/alerts.css" />"/>
    <script type="text/javascript" src="<c:url value="/resources/js/alerts.js" />"></script>
    <script src="<c:url value="/resources/js/auditionForm.js" />"></script>
    <script src="<c:url value="/resources/js/matMultipleSelect.js"/>"></script>
</head>
<body onload="auditionFormCheck()">
<!-- Navbar -->
<jsp:include page="../components/navbar.jsp">
    <jsp:param name="navItem" value="${3}" />
    <jsp:param name="name" value="Bandify" />
</jsp:include>
<main>
    <!-- Formulario -->
    <div class="card-content-small" id="form-post">
        <h1><spring:message code="welcome.formSectionh1"/></h1>
        <br/>
        <h1><spring:message code="welcome.formSectionh2"/></h1>
        <c:url value="/newAudition" var="postPath" />
        <!-- Form box -->
        <div class="inner-box-form" id="form-post-title">
            <form:form
                    modelAttribute="auditionForm"
                    action="${postPath}"
                    method="post"
                    acceptCharset="utf-8"
                    id="auditionForm"
            >
                <div class="title-div">
                    <form:label class="form-label" path="title">
                        <spring:message code="welcome.form.title"/>
                    </form:label>
                    <spring:message code="audition.form.title.placeholder" arguments="50" var="titleplaceholder" />
                    <form:input type="text" id="titleForm"  maxlength="50" placeholder="${titleplaceholder}" class="form-input" path="title" />
                    <p id="emptyTitle" class="error" style="display: none"><spring:message code="NotBlank.auditionForm.title"/> </p>
                    <p id="longTitle" class="error" style="display: none"><spring:message code="audition.form.title.maxSize" arguments="50"/> </p>
                    <form:errors path="title" element="p" cssClass="error"> </form:errors>
                </div>

                <div>
                    <form:label class="form-label" path="description">
                        <spring:message code="welcome.form.description"/>
                    </form:label>
                    <spring:message code="audition.form.description.placeholder" arguments="300" var="descriptionplaceholder" />
                    <form:textarea
                            maxlength="300" placeholder="${descriptionplaceholder}"
                            class="form-input-application"
                            type="text"
                            id="description"
                            path="description"

                    />
                    <p id="emptyDescription" class="error" style="display: none"><spring:message code="NotBlank.auditionForm.description" arguments="0"/> </p>
                    <p id="longDescription" class="error" style="display: none"><spring:message code="audition.form.description.maxSize" arguments="300"/> </p>
                    <form:errors path="description" element="p" cssClass="error"> </form:errors>
                </div>
                <div class="select-div">
                    <form:label class="form-label" path="location">
                        <spring:message code="welcome.form.location"/>
                    </form:label>
                    <form:select
                            path="location"
                            multiple="false"
                            id="location"
                    >
                        <form:option value="" selected="true" disabled="true"><spring:message code="welcome.form.location.default"/></form:option>
                        <c:forEach
                                var="location"
                                items="${locationList}"
                                varStatus="loop"
                        >
                            <form:option value="${location.name}"><c:out value="${location.name}"/></form:option>
                        </c:forEach>
                    </form:select>
                    <form:errors path="location" element="p" cssClass="error">
                    </form:errors>
                </div>
                <div class="select-div">
                    <form:label for="musicGenres" path="musicGenres"> <spring:message code="welcome.form.musicGenres"/> </form:label>
                    <form:select
                            class="multiple-select"
                            path="musicGenres"
                            multiple="true"
                    >
                        <form:option value="" disabled="true" selected="true"><spring:message code="audition.form.musicGenres.maxSelect" arguments="5"/> </form:option>
                        <c:forEach var="genre" items="${genreList}" varStatus="loop">
                            <form:option value="${genre.name}"><c:out value="${genre.name}"/></form:option>
                        </c:forEach>
                    </form:select>
                    <form:errors path="musicGenres" element="p" cssClass="error">
                    </form:errors>
                </div>
                <div class="select-div">
                    <form:label for="lookingFor" path="lookingFor"> <spring:message code="welcome.form.lookingFor"/> </form:label>
                    <form:select
                            class="multiple-select "
                            path="lookingFor"
                            multiple="true"
                    >
                        <form:option value="" disabled="true" selected="true"><spring:message code="audition.form.lookingFor.maxSelect" arguments="5"/>  </form:option>
                        <c:forEach var="role" items="${roleList}" varStatus="loop">
                            <form:option value="${role.name}"><c:out value="${role.name}"/></form:option>
                        </c:forEach>
                    </form:select>
                    <form:errors path="lookingFor" element="p" cssClass="error">
                    </form:errors>
                </div>
                <div class="end-button-div">
                    <button
                            type="submit"
                            value="submit"
                            class="post-button"
                    >
                        <spring:message code="welcome.postButton"/>
                    </button>
                </div>
            </form:form>
        </div>
    </div>
    <div id="snackbar"><spring:message code="snackbar.message"/></div>

</main>
<jsp:include page="../components/footer.jsp">
    <jsp:param name="name" value="Bandify"/>
</jsp:include>
</body>
</html>
